package acquire.core.trans.impl.settle;

import android.text.TextUtils;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.R;
import acquire.core.bean.field.Field63_Settle;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.tools.DataConverter;
import acquire.core.tools.PinpadHelper;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.iso.Caller;
import acquire.core.trans.pack.iso.Packet8583;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.service.MerchantService;
import acquire.database.service.RecordService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.RecordServiceImpl;

/**
 * The {@link BaseStep} that packs and sends settlement data to the server
 *
 * @author Janson
 * @date 2019/7/29 14:57
 */
class PackSettleStep extends BaseStep {
    private final MerchantService merchantService = new MerchantServiceImpl();


    @Override
    public void intercept(Callback callback) {
        if (!doReversal()){
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }
        for (Merchant merchant : pubBean.getSettleMerchants()) {
            //merchant's settleHalt indicates this merchant is ready to this step.
            if (merchant.getSettleStep() <= Settle.STEP_SETTLEMENT_SENT) {
                if (!sendTotal(merchant)) {
                    callback.onResult(false);
                    return;
                }
                merchant.setSettleStep(Settle.STEP_BATCH_UP);
                merchantService.update(merchant);
            }
            if (merchant.getSettleStep() <= Settle.STEP_BATCH_UP){
                if (!merchant.isSettleEqual()){
                    boolean result = sendEveryRecord(merchant);
                    if (!result) {
                        closeProgressUi();
                        callback.onResult(false);
                        return;
                    }
                }
                closeProgressUi();
                merchant.setSettleStep(Settle.STEP_BATCH_UP + 1);
                merchantService.update(merchant);
            }
        }
        callback.onResult(true);
    }

    /**
     * send a merchant total amount to the server
     */
    private boolean sendTotal(Merchant merchant) {
        initPubBean(merchant);
        pubBean.setProcessCode("920000");
        pubBean.setMessageId("0500");
        Field63_Settle field63Settle = new Field63_Settle(pubBean.getMid(),pubBean.getTid());

        //pack 8583
        iso8583.initPack();
        try {
            iso8583.setField(0, pubBean.getMessageId());
            iso8583.setField(3, pubBean.getProcessCode());
            iso8583.setField(11, pubBean.getTraceNo());
            if (!TextUtils.isEmpty(pubBean.getNii())) {
                iso8583.setField(24, pubBean.getNii());
            }
            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid());
            iso8583.setField(49, pubBean.getCurrencyCode());
            iso8583.setField(57, new PinpadHelper().getKsn());
            iso8583.setField(62, pubBean.getBatchNo());
            iso8583.setField(63, field63Settle.getString());
            iso8583.setField(64, Packet8583.getMac(iso8583));
        } catch (Exception e) {
            e.printStackTrace();
            pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error)+e.getMessage());
            pubBean.setResultCode(ResultCode.FL);
            return false;
        }

        //send to the server
        int ret = new Caller.Builder(mActivity, pubBean, iso8583)
                .packComm();
        if (ret != CallerResult.OK) {
            return false;
        }
        String responseCode = iso8583.getField(39);
        merchant.setSettleEqual(ResultCode.OK.equals(responseCode));
        /* Save settlement date */
        merchant.setSettleDate(pubBean.getDate());
        merchant.setSettleTime(pubBean.getTime());
        //set the halt of this merchant to HALT_SETTLE_UP+1
        merchant.setSettleStep(Settle.STEP_SETTLEMENT_SENT + 1);
        return true;
    }
    /**
     * send every record of a merchant.
     */
    private boolean sendEveryRecord(Merchant merchant) {
        RecordService recordService = new RecordServiceImpl();
        int count = recordService.getCount(merchant.getMid(),merchant.getTid());
        LoggerUtils.i(merchant.getCardOrg()+" record sum: "+ count);
        if (count <= 0) {
            return true;
        }
        //upload all records of this merchant
        int index = 0;
        for (int i = 0; i < count; i++) {
            Record record = recordService.findByIndex(merchant.getMid(),merchant.getTid(),i);
            LoggerUtils.d("batch up -> "+ record.getTraceNo());
            updateProgressUi(index);
            for (int j = 0; j < 3; j++) {
                if (record.isBatchUpFlag()) {
                    LoggerUtils.e("batch uploaded.");
                    break;
                }
                DataConverter.recordToPubBean(record, pubBean);
                initPubBean(merchant);
                pubBean.setMessageId("0320");
                pubBean.setProcessCode("000000");
                //pack 8583
                iso8583.initPack();
                try {
                    iso8583.setField(0, pubBean.getMessageId());
                    iso8583.setField(2, pubBean.getCardNo());
                    iso8583.setField(3, pubBean.getProcessCode());
                    iso8583.setField(4, pubBean.getAmountField());
                    iso8583.setField(11, pubBean.getTraceNo());
                    if (!TextUtils.isEmpty(pubBean.getExpDate())) {
                        iso8583.setField(14, pubBean.getExpDate());
                    }
                    iso8583.setField(22, pubBean.getField22());
                    iso8583.setField(23, pubBean.getCardSn());
                    iso8583.setField(41, pubBean.getTid());
                    iso8583.setField(42, pubBean.getMid());
                    iso8583.setField(49, pubBean.getCurrencyCode());
                    if (!TextUtils.isEmpty(pubBean.getField55())) {
                        iso8583.setField(55, pubBean.getField55());
                    }
                    iso8583.setField(57, new PinpadHelper().getKsn());
                    iso8583.setField(64, Packet8583.getMac(iso8583));
                } catch (Exception e) {
                    e.printStackTrace();
                    pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error)+e.getMessage());
                    pubBean.setResultCode(ResultCode.FL);
                    ToastUtils.showToast(R.string.core_batch_upload_pack_error);
                    continue;
                }

                //send to the server
                int ret = new Caller.Builder(mActivity, pubBean, iso8583)
                        .withoutPrompts()
                        .checkResp(true)
                        .packComm();
                if (ret == CallerResult.FAIL_NET_CONNECT) {
                    //net  error,exit
                    return false;
                }
                if (ret == CallerResult.OK){
                    //update batch up flag to true
                    record.setBatchUpFlag(true);
                    recordService.update(record);
                    //response code :Success.
                    ToastUtils.showToast(mActivity.getString(R.string.core_batch_upload_success) + index);
                    LoggerUtils.d("batch up success ["+ record.getTraceNo()+"]");
                    break;
                } else {
                    //response code :Failed.
                    ToastUtils.showToast(pubBean.getMessage());
                    LoggerUtils.d("batch up failed ["+ record.getTraceNo()+"]");
                }
            }
            if (!record.isBatchUpFlag()){
                // record upload failed
                pubBean.setResultCode(ResultCode.FL);
                pubBean.setMessage(R.string.core_batch_upload_failed);
                return false;
            }
        }
        return true;
    }
    private ProgressDialog progressDialog;
    private void updateProgressUi(int index){
        mActivity.runOnUiThread(()->{
            if (progressDialog != null){
                progressDialog.getContent().setText(mActivity.getString(R.string.core_batch_uploading_fomat,index));
                progressDialog.show();
            }else{
                progressDialog = new ProgressDialog.Builder(mActivity)
                        .setContent(mActivity.getString(R.string.core_batch_uploading_fomat,index))
                        .show();
            }
        });
    }
    private void closeProgressUi(){
        mActivity.runOnUiThread(()->{
            if (progressDialog != null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        });
    }
}
