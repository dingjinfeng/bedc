package acquire.core.trans.impl.reversal;

import android.text.TextUtils;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.tools.DataConverter;
import acquire.core.tools.PinpadHelper;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.iso.Caller;
import acquire.core.trans.pack.iso.Packet8583;
import acquire.database.model.Merchant;
import acquire.database.model.ReversalData;
import acquire.database.service.MerchantService;
import acquire.database.service.RecordService;
import acquire.database.service.ReversalDataService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.RecordServiceImpl;
import acquire.database.service.impl.ReversalDataServiceImpl;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The {@link BaseStep} that packs {@link Reversal} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2021/6/30 8:58
 */
class PackReversalStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        ReversalDataService reversalDataService = new ReversalDataServiceImpl();
        ReversalData reversalData = reversalDataService.getReverseRecord();
        if (reversalData == null) {
            LoggerUtils.d("No reversal data.");
            callback.onResult(true);
            return;
        }
        int failTimes = reversalData.getHasSend();
        final int maxTimes = 3;

        int connectTimes = 0;
        boolean success = false;
        while (!success && failTimes < maxTimes) {
            initPubBean();
            DataConverter.reversalToPubBean(reversalData, pubBean);
            LoggerUtils.d("send transaction reversal:"+(failTimes+1));
            MerchantService merchantService = new MerchantServiceImpl();
            Merchant merchant = merchantService.find(reversalData.getMid(), reversalData.getTid());
            pubBean.setBatchNo(merchant.getBatchNo());
            pubBean.setMessageId("0400");
            iso8583.initPack();
            try {
                iso8583.setField(0, pubBean.getMessageId());
                if (pubBean.getEntryMode() == EntryMode.MANUAL) {
                    iso8583.setField(2, pubBean.getCardNo());
                }
                iso8583.setField(3, pubBean.getProcessCode());
                iso8583.setField(4,  pubBean.getAmountField());
                iso8583.setField(11, pubBean.getTraceNo());
                if (!TextUtils.isEmpty(pubBean.getExpDate())) {
                    iso8583.setField(14, pubBean.getExpDate());
                }
                iso8583.setField(22, pubBean.getField22());
                if (!TextUtils.isEmpty(pubBean.getCardSn())) {
                    iso8583.setField(23, pubBean.getCardSn());
                }
                if (!TextUtils.isEmpty(pubBean.getNii())) {
                    iso8583.setField(24, pubBean.getNii());
                }
                iso8583.setField(25, pubBean.getServerCode());
                if (!TextUtils.isEmpty(pubBean.getOrigAuthCode())) {
                    iso8583.setField(38, pubBean.getOrigAuthCode());
                }
                pubBean.setTid("50250015");
                iso8583.setField(41, pubBean.getTid());
                iso8583.setField(42, pubBean.getMid());
                iso8583.setField(49, pubBean.getCurrencyCode());
                if (!TextUtils.isEmpty(pubBean.getField55())) {
                    iso8583.setField(55, pubBean.getField55());
                }
                iso8583.setField(57, new PinpadHelper().getKsn());
                iso8583.setField( 60, "22"+pubBean.getBatchNo());
//                iso8583.setField(62, pubBean.getBatchNo());
                iso8583.setField(64, Packet8583.getMac(iso8583));
            } catch (Exception e) {
                e.printStackTrace();
                pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error)+e.getMessage());
                pubBean.setResultCode(ResultCode.FL);
                ToastUtils.showToast(R.string.core_reversal_pack_fail);
                callback.onResult(false);
                return;
            }

            //send to the server
            int result = new Caller.Builder(mActivity, pubBean, iso8583)
                    .withPrompts(R.string.core_reversal_sending)
                    .packComm();
            switch (result) {
                case CallerResult.OK:
                    //check response code
                    String responseCode = iso8583.getField(39);
                    LoggerUtils.d("Reversal response code: "+responseCode);
                    if (ResultCode.OK.equals(responseCode)) {
                        //success
                        success = true;
                    }else {
                        //fail,toast error
                        ToastUtils.showToast(pubBean.getMessage());
                        failTimes++;
                        reversalData.setHasSend(failTimes);
                        LoggerUtils.e("Failed times is "+failTimes);
                    }
                    break;
                case CallerResult.FAIL_NET_CONNECT:
                case CallerResult.FAIL_NET_RECV:
                    //connect failed
                    LoggerUtils.e("Connect failed: "+connectTimes);
                    ToastUtils.showToast(pubBean.getMessage());
                    connectTimes++;
                    if (connectTimes > maxTimes) {
                        //connectTimes > maxTimes,network doesn't work. Try again later
                        callback.onResult(false);
                        return;
                    }
                    break;
                case CallerResult.FAIL_REQUEST_DATA_ERROR:
                case CallerResult.FAIL_RESPONSE_DATA_ERROR:
                default:
                    //continue to resend
                    ToastUtils.showToast(pubBean.getMessage());
                    failTimes++;
                    reversalData.setHasSend(failTimes);
                    LoggerUtils.e("Failed times is "+failTimes);
                    break;
            }

        }
        //delete reversal record
        reversalDataService.delete();
        RecordService recordService = new RecordServiceImpl();
        if (recordService.findByTrace(reversalData.getTraceNo()) != null) {
            //  This step will not be performed normally,
            //  mainly to check whether there are associated records in the record database table.
            recordService.delete(reversalData.getTraceNo());
        }
        // No matter success or failure, this means that the implementation is successful
        if (success) {
            ToastUtils.showToast(R.string.core_reversal_success);
        } else {
            ToastUtils.showToast(R.string.core_reversal_fail);
        }
        //recover pubbean
        callback.onResult(true);
    }

}
