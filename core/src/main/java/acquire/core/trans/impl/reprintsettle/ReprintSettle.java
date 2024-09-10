package acquire.core.trans.impl.reprintsettle;

import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.trans.AbstractTrans;
import acquire.database.model.Merchant;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.sdk.device.BDevice;

/**
 * reprint settle data
 *
 * @author Janson
 * @date 2021/6/30 10:48
 */
public class ReprintSettle extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        if (!BDevice.supportPrint() && !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_print_unsupport);
            listener.onTransResult(false);
            return;
        }
        List<Merchant> merchants = new MerchantServiceImpl().findAll();
        merchants.removeIf(merchant -> merchant.getLastReceipt() == null);
        if (merchants.size() == 0) {
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_print_no_such_data);
            ToastUtils.showToast(R.string.core_print_no_such_data);
            listener.onTransResult(false);
            return;
        }
        mActivity.mSupportDelegate.switchContent(PrintFragment.newSettlementInstance(merchants, true, new FragmentCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
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
