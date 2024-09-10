package acquire.core.trans.steps;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.R;
import acquire.core.bean.field.Field63_Settle;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransType;
import acquire.core.tools.TransUtils;
import acquire.core.trans.BaseStep;
import acquire.core.trans.impl.settle.Settle;
import acquire.database.model.Merchant;
import acquire.sdk.FlyReceiptHelper;

/**
 * Fly receipt for settlement
 *
 * @author Janson
 * @date 2023/5/15 10:49
 */
public class FlySettlementReceiptStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_TOMS_FLY_RECEIPT)) {
            callback.onResult(true);
            return;
        }
        FlyReceiptHelper.SettleTicketBean ticketBean = new FlyReceiptHelper.SettleTicketBean();
        long debitAmount = 0;
        long debitNumber = 0;
        long creditAmount = 0;
        long creditNumber = 0;
        String tid = null, mid = null;
        for (Merchant merchant : pubBean.getSettleMerchants()) {
            if (merchant.getSettleStep() > Settle.STEP_PRINT) {
                continue;
            }
            //send Fly Settlement Receipt
            Field63_Settle field63Settle = new Field63_Settle(merchant.getMid(),merchant.getTid());
            debitAmount +=field63Settle.getDebitAmount();
            debitNumber += field63Settle.getDebitNum();
            creditAmount +=field63Settle.getCreditAmount();
            creditNumber += field63Settle.getCreditNum();
            if (field63Settle.getTotalAmount() != 0){
                mid = merchant.getMid();
                tid = merchant.getTid();
            }

        }
        if (mid == null || tid == null){
            callback.onResult(true);
            return;
        }
        LoggerUtils.d("FlyReceipt settlement request");
        ticketBean.setDebitAmount(debitAmount);
        ticketBean.setDebitNumber(debitNumber);
        ticketBean.setCreditAmount(creditAmount);
        ticketBean.setCreditNumber(creditNumber);
        ticketBean.setMerchantName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
        ticketBean.setMid(mid);
        ticketBean.setTid(tid);
        ticketBean.setTransCode(TransType.TRANS_SETTLE);
        ticketBean.setTransName(TransUtils.getName(TransType.TRANS_SETTLE));
        mActivity.runOnUiThread(()->{
            ProgressDialog progressDialog = new ProgressDialog.Builder(mActivity)
                    .setContent(R.string.core_fly_receipt_communicate_prompt)
                    .show();
            ThreadPool.execute(()->
                    FlyReceiptHelper.getInstance().sendSettle(mActivity, ticketBean, new FlyReceiptHelper.FlyReceiptCallback() {
                        @Override
                        public void onSuccess(String result) {
                            LoggerUtils.d("FlyReceipt settlement result:"+result);
                            mActivity.runOnUiThread(progressDialog::dismiss);
                            callback.onResult(true);
                        }

                        @Override
                        public void onFailed(int code, String msg) {
                            LoggerUtils.e("FlyReceipt settlement error code:"+code+", error msg:"+msg);
                            mActivity.runOnUiThread(progressDialog::dismiss);
                            ToastUtils.showToast(mActivity.getString(R.string.core_fly_receipt_communicate_error,msg));
                            callback.onResult(true);
                        }
                    })
            );
        });
    }



}
