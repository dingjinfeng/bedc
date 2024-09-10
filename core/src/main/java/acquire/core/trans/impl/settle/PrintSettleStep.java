package acquire.core.trans.impl.settle;

import java.util.Collections;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.thread.Locker;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.trans.BaseStep;
import acquire.database.model.Merchant;
import acquire.database.service.MerchantService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.sdk.device.BDevice;

/**
 * The {@link BaseStep} that prints settlement data
 *
 * @author Janson
 * @date 2019/7/29 15:08
 */
class PrintSettleStep extends BaseStep {
    private final MerchantService merchantService = new MerchantServiceImpl();

    @Override
    public void intercept(Callback callback)  {
        if (!BDevice.supportPrint()&& !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            //This device doesn't support print
            for (Merchant merchant : pubBean.getSettleMerchants()) {
                //set the halt of this merchant to HALT_PRINT_SETTLE+1
                merchant.setSettleStep(Settle.STEP_PRINT +1);
                merchantService.update(merchant);
            }
            callback.onResult(true);
        } else {
            for (Merchant merchant : pubBean.getSettleMerchants()) {
                if (merchant.getSettleStep() > Settle.STEP_PRINT){
                    continue;
                }
                Locker<Boolean> locker = new Locker<>();
                ThreadPool.execute(()->{
                    //print data
                    mActivity.mSupportDelegate.switchContent(PrintFragment.newSettlementInstance(Collections.singletonList(merchant),false, new FragmentCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            merchant.setSettleStep(Settle.STEP_PRINT +1);
                            merchantService.update(merchant);
                            locker.setResult(true);
                            locker.wakeUp();
                        }

                        @Override
                        public void onFail(int errorType, String errorMsg) {
                            //print failed
                            LoggerUtils.e("Print merchant[mid="+merchant.getMid()+",tid="+merchant.getTid()+"] failed.");
                            pubBean.setResultCode(ResultCode.FL);
                            pubBean.setMessage(errorMsg);
                            locker.setResult(false);
                            locker.wakeUp();
                        }
                    }));
                });
                locker.waiting();
                if (!locker.getResult()){
                    callback.onResult(false);
                    return;
                }
            }
            callback.onResult(true);
        }
    }
}
