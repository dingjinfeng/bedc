package acquire.core.trans;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import acquire.base.activity.BaseActivity;
import acquire.base.chain.Interceptor;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.iso8583.ISO8583;
import acquire.base.utils.thread.Locker;
import acquire.core.TransActivity;
import acquire.core.bean.PubBean;
import acquire.core.bean.StepBean;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.service.MerchantService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.ReversalDataServiceImpl;

/**
 * The base step implements {@link Interceptor}
 *
 * @author Janson
 * @date 2019/12/24 14:57
 */
public abstract class BaseStep implements Interceptor<StepBean> {
    protected PubBean pubBean;
    protected BaseActivity mActivity;
    protected final ISO8583 iso8583 = ISO8583.getDefault();
    protected StepBean stepBean;

    @Override
    public void init(@NonNull StepBean stepBean) {
        this.pubBean = stepBean.getPubBean();
        this.mActivity = stepBean.getActivity();
        this.stepBean = stepBean;
    }

    /**
     * Set current record
     */
    protected void setRecord(Record record) {
        stepBean.setRecord(record);
    }

    /**
     * Get original record
     */
    protected void setOrigRecord(Record record) {
        stepBean.setOrigRecord(record);
    }

    protected Record getRecord() {
        return stepBean.getRecord();
    }

    protected Record getOrigRecord() {
        return stepBean.getOrigRecord();
    }

    /**
     * check reversal
     */
    protected boolean doReversal(){
        if (new ReversalDataServiceImpl().getReverseRecord() == null) {
            //no reversal data
            return true;
        }
        //start a reversal transaction
        Intent intent = new Intent(mActivity, TransActivity.class);
        intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_REVERSAL);
        Locker<Boolean> locker = new Locker<>(false);
        mActivity.mSupportDelegate.startActivityForResult(intent, null, result -> {
            if (result.getResultCode() == Activity.RESULT_OK){
                locker.setResult(true);
                locker.wakeUp();
            }else{
                locker.setResult(false);
                locker.wakeUp();
            }
        });
        locker.waiting();
        return locker.getResult();
    }

    /**
     * Init PubBean
     */
    public void initPubBean() {
        MerchantService merchantService = new MerchantServiceImpl();
        Merchant merchant =  merchantService.find(pubBean.getCardOrg());
        if (merchant == null){
            //default merchant
            merchant = merchantService.findAll().get(0);
        }
        initPubBean(merchant);
    }
    /**
     * Init PubBean of a merchant
     */
    public void initPubBean(Merchant merchant) {
        pubBean.setMerchantName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
        pubBean.setTraceNo(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_TRACE_NO));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        pubBean.setDate(dateFormat.format(new Date()));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        pubBean.setTime(timeFormat.format(new Date()));
        pubBean.setTid(merchant.getTid());
        pubBean.setMid(merchant.getMid());
        pubBean.setBatchNo(merchant.getBatchNo());
        pubBean.setNii(ParamsUtils.getString(ParamsConst.PARAMS_KEY_COMM_NII));
    }
}
