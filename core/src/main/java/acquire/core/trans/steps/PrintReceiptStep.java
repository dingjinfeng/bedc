package acquire.core.trans.steps;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.display2.ResultPresentation;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * Print receipt
 *
 * @author Janson
 * @date 2023/4/26 14:09
 */
public class PrintReceiptStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!BDevice.supportPrint()&& !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            callback.onResult(true);
            return;
        }
        if (Model.X800.equals(BDevice.getDeviceModel())) {
            mActivity.runOnUiThread(() -> {
                ResultPresentation presentation = new ResultPresentation(mActivity, true, pubBean.getMessage());
                presentation.show();
            });
        }
        Record record = getRecord();
        mActivity.mSupportDelegate.switchContent(PrintFragment.newReceiptInstance(record, false, new FragmentCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                //always success
                callback.onResult(true);
            }
        }));
    }
}
