package acquire.core.trans.impl.reprintreceipt;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.fragment.record.RecordDetailFragment;
import acquire.core.fragment.record.RecordFragment;
import acquire.core.tools.DataConverter;
import acquire.core.trans.AbstractTrans;
import acquire.database.model.Record;
import acquire.database.service.impl.RecordServiceImpl;
import acquire.sdk.device.BDevice;

/**
 * print the any record
 *
 * @author Janson
 * @date 2021/6/30 10:48
 */
public class ReprintReceipt extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        if (pubBean.getOrigTraceNo() != null){
            Record record = new RecordServiceImpl().findByTrace(pubBean.getOrigTraceNo());
            if (record == null){
                new MessageDialog.Builder(mActivity)
                        .setMessage(R.string.core_record_no_records_found)
                        .setConfirmButton(dialog -> {
                            pubBean.setMessage(R.string.core_record_no_records_found);
                            pubBean.setResultCode(ResultCode.FL);
                            listener.onTransResult(false);
                        })
                        .show();
                return;
            }
            printRecord(record,listener);
            return;
        }
        //show all records
        mActivity.mSupportDelegate.switchContent(RecordFragment.newInstance(new FragmentCallback<Record>() {
            @Override
            public void onSuccess(Record record) {
                //show the record details
                mActivity.mSupportDelegate.switchContent(RecordDetailFragment.newInstance(mActivity.getString(R.string.core_record_detail_print_button), record, new FragmentCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        printRecord(record,null);
                    }

                    @Override
                    public void onFail(int errorType, String errorMsg) {
                        mActivity.mSupportDelegate.popBackFragment(1);
                    }
                }));

            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_transaction_result_success);
                listener.onTransResult(true);
            }
        }));
    }

    private void printRecord(Record record, TransResultListener listener){
        if (!BDevice.supportPrint()&& !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            pubBean.setResultCode(ResultCode.OK);
            pubBean.setMessage(R.string.core_print_unsupport);
            ToastUtils.showToast(pubBean.getMessage());
            if (listener != null){
                listener.onTransResult(true);
            }
            return;
        }
        mActivity.mSupportDelegate.switchContent(PrintFragment.newReceiptInstance(record,true, new FragmentCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mActivity.mSupportDelegate.popBackFragment(1);
                DataConverter.recordToPubBean(record,pubBean);
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_print_success);
                if (listener != null){
                    listener.onTransResult(true);
                }
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                mActivity.mSupportDelegate.popBackFragment(1);
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_print_success);
                if (listener != null){
                    listener.onTransResult(true);
                }
            }
        }));
    }
}
