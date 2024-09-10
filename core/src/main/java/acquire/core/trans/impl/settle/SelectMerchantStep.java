package acquire.core.trans.impl.settle;

import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.settle.SettleFragment;
import acquire.core.trans.BaseStep;
import acquire.database.model.Merchant;
import acquire.database.service.RecordService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.RecordServiceImpl;

/**
 * Select the merchants to be settled.
 *
 * @author Janson
 * @date 2021/7/20 10:25
 */
public class SelectMerchantStep extends BaseStep {


    @Override
    public void intercept(Callback callback) {
        if (pubBean.getSettleMerchants() != null && pubBean.getSettleMerchants().size() > 0) {
            callback.onResult(true);
            return;
        }
        if (pubBean.isSettleAll()) {
            //settle all merchants
            pubBean.setSettleMerchants(new MerchantServiceImpl().findAll());
            callback.onResult(true);
            return;
        }
        mActivity.mSupportDelegate.switchContent(SettleFragment.newInstance(new FragmentCallback<List<Merchant>>() {
            @Override
            public void onSuccess(List<Merchant> merchants) {
                if (!existRecord(merchants)) {
                    ToastUtils.showToast(R.string.core_settle_no_record);
                    return;
                }
                pubBean.setSettleMerchants(merchants);
                callback.onResult(true);
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
                callback.onResult(false);
            }
        }));
    }

    private boolean existRecord(List<Merchant> merchants) {
        RecordService recordService = new RecordServiceImpl();
        for (Merchant merchant : merchants) {
            if (recordService.getCount(merchant.getMid(), merchant.getTid()) > 0) {
                return true;
            }
        }
        return false;
    }
}
