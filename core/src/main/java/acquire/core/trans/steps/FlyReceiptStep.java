package acquire.core.trans.steps;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.fragment.print.PrintViewModel;
import acquire.core.tools.TransUtils;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.sdk.FlyReceiptHelper;

/**
 * Fly receipt
 *
 * @author Janson
 * @date 2023/4/27 9:06
 */
public class FlyReceiptStep extends BaseStep {


    @Override
    public void intercept(Callback callback) {
        Record record = getRecord();
        if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_TOMS_FLY_RECEIPT) || record == null) {
            callback.onResult(true);
            return;
        }
        LoggerUtils.d("FlyReceipt request");
        //send Fly Receipt
        FlyReceiptHelper.ReceiptBean receiptBean = new FlyReceiptHelper.ReceiptBean();
        receiptBean.setAmount(record.getAmount());
        receiptBean.setMerchantName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
        receiptBean.setMid(record.getMid());
        receiptBean.setTid(record.getTid());
        receiptBean.setTransCode(record.getTransType());
        receiptBean.setTransName(TransUtils.getName(record.getTransType()));
        receiptBean.setReceipt(PrintViewModel.getReceipt(record,false,0));
        mActivity.runOnUiThread(()->{
            ProgressDialog progressDialog = new ProgressDialog.Builder(mActivity)
                    .setContent(R.string.core_fly_receipt_communicate_prompt)
                    .show();
            ThreadPool.execute(()->
                    FlyReceiptHelper.getInstance().sendReceipt(mActivity, receiptBean, new FlyReceiptHelper.FlyReceiptCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    LoggerUtils.d("FlyReceipt result:"+result);
                                    mActivity.runOnUiThread(progressDialog::dismiss);
                                    callback.onResult(true);
                                }

                                @Override
                                public void onFailed(int code, String msg) {
                                    LoggerUtils.e("FlyReceipt error code:"+code+", error msg:"+msg);
                                    mActivity.runOnUiThread(progressDialog::dismiss);
                                    ToastUtils.showToast(mActivity.getString(R.string.core_fly_receipt_communicate_error,msg));
                                    callback.onResult(true);
                                }
                            })
            );
        });

    }



}
