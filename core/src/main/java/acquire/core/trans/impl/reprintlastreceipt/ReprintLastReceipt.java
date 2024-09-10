package acquire.core.trans.impl.reprintlastreceipt;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.tools.DataConverter;
import acquire.core.trans.AbstractTrans;
import acquire.database.model.Record;
import acquire.database.service.impl.RecordServiceImpl;
import acquire.sdk.device.BDevice;

/**
 * print the last record
 *
 * @author Janson
 * @date 2021/6/30 10:48
 */
public class ReprintLastReceipt extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        if (!BDevice.supportPrint() && !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_print_unsupport);
            listener.onTransResult(false);
            return;
        }
        Record record = new RecordServiceImpl().findLast();
        if (record == null){
            ToastUtils.showToast(R.string.core_record_no_records_found);
            pubBean.setMessage(R.string.core_record_no_records_found);
            pubBean.setResultCode(ResultCode.FL);
            listener.onTransResult(false);
            return;
        }
        mActivity.mSupportDelegate.switchContent(PrintFragment.newReceiptInstance(record, true,new FragmentCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DataConverter.recordToPubBean(record,pubBean);
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_print_success);
                listener.onTransResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                switch (errorType) {
                    case FragmentCallback.CANCEL:
                        pubBean.setResultCode(ResultCode.UC);
                        break;
                    case FragmentCallback.TIMEOUT:
                    case FragmentCallback.FAIL:
                    default:
                        pubBean.setResultCode(ResultCode.FL);
                        break;
                }
                pubBean.setMessage(errorMsg);
                listener.onTransResult(false);
            }
        }));
    }
}
