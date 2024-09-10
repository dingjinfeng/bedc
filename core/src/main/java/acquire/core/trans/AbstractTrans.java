package acquire.core.trans;

import android.text.TextUtils;

import acquire.base.activity.BaseActivity;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.chain.Chain;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.bean.PubBean;
import acquire.core.bean.StepBean;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.display2.BasePresentation;
import acquire.core.display2.ResultPresentation;
import acquire.core.fragment.result.ResultBillFragment;
import acquire.core.fragment.result.ResultSimpleFragment;
import acquire.database.model.Record;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * A base transaction class
 *
 * @author Janson
 * @date 2019/10/8 9:29
 */
public abstract class AbstractTrans {
    protected BaseActivity mActivity;
    protected PubBean pubBean;
    protected Chain<StepBean> chain;
    private StepBean stepBean;

    public void init(BaseActivity activity, PubBean pubBean) {
        this.mActivity = activity;
        this.pubBean = pubBean;
        stepBean = new StepBean(activity, pubBean);
        chain = new Chain<>(stepBean);
    }

    /**
     * transact the transaction
     *
     * @param listener result listener
     */
    public abstract void transact(TransResultListener listener);

    /**
     * Show result
     */
    protected void showResult(boolean success, TransResultListener listener) {
        if (pubBean.getResultCode() == null) {
            pubBean.setResultCode(success ? ResultCode.OK : ResultCode.FL);
        }
        if (pubBean.getMessage() == null) {
            pubBean.setMessage(success ? R.string.core_transaction_result_success : R.string.core_transaction_result_fail);
        }

        if (pubBean.isThirdCall() && ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_OTHER_THRID_BILL_SHOW, false)) {
            //No show result and return
            listener.onTransResult(success);
            return;
        }
        if (ResultCode.UC.equals(pubBean.getResultCode())) {
            //user cancel
            listener.onTransResult(false);
        } else {
            // show result
            if (Model.X800.equals(BDevice.getDeviceModel()) && !(BasePresentation.getTopPresentation() instanceof ResultPresentation)) {
                //X800
                mActivity.runOnUiThread(() -> {
                    ResultPresentation presentation = new ResultPresentation(mActivity, success, pubBean.getMessage());
                    presentation.show();
                });
            }
            Record record = stepBean.getRecord();
            if (success && record != null) {
                //result bill runnable
                 mActivity.mSupportDelegate.switchContent(ResultBillFragment.newInstance(record, new SimpleCallback() {
                    @Override
                    public void result() {
                        listener.onTransResult(true);
                    }
                }));
            } else {
                //show result
                String message;
                if (!TextUtils.isEmpty(pubBean.getResultCode()) && !ResultCode.isCustomCode(pubBean.getResultCode())) {
                    message = mActivity.getString(R.string.core_answer_code_prompt_format, pubBean.getResultCode(), pubBean.getMessage());
                } else {
                    message = pubBean.getMessage();
                }
                mActivity.mSupportDelegate.switchContent(ResultSimpleFragment.newInstance(success, message, new SimpleCallback() {
                    @Override
                    public void result() {
                        listener.onTransResult(success);
                    }
                }));
            }
        }
    }
}
