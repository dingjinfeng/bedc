package acquire.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import acquire.base.activity.BaseActivity;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.core.bean.PubBean;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.core.databinding.CoreActivityTransBinding;
import acquire.core.display2.BasePresentation;
import acquire.core.tools.DataConverter;
import acquire.core.tools.TransUtils;
import acquire.core.trans.AbstractTrans;

/**
 * A Transaction {@link Activity}.
 * <p><hr><b>start {@link TransActivity} to execute a transaction</b></p>
 * <p>e.g.</p>
 * <pre>
 *     Intent intent = new Intent();
 *     intent.putExtra({@link TransTag#TRANS_TYPE}, {@link TransType#TRANS_SALE});
 *     ActivityCompat.startActivity(mActivity, intent, null);
 * </pre>
 * <p>For more parameters, please see {@link TransTag}</p>
 *
 * @author Janson
 * @date 2020/2/12 13:58
 */
public class TransActivity extends BaseActivity {

    public final static String THIRD_ACTION = "android.intent.action.third";

    @Override
    public int attachFragmentResId() {
        return R.id.fragment_trans_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CoreActivityTransBinding binding = CoreActivityTransBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        //catch back event
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });

        if (isThirdCall()) {
            //This is to avoid the blank status bar when third app calls this app
            getSupportFragmentManager().addFragmentOnAttachListener(new FragmentOnAttachListener() {
                private boolean first;

                @Override
                public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
                    if (!first) {
                        first = true;
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(TransActivity.this, R.color.base_background));
                    }
                }
            });
        }
        //set immersed status bar.
        DisplayUtils.immersedStatusBar(getWindow());
        transact();
    }

    private void transact() {
        //intent to pubbean
        Intent intent = getIntent();
        //unparcel intent bundle
        intent.getStringExtra("");
        LoggerUtils.d("Transaction params: " + intent.getExtras().toString());
        PubBean pubBean = new PubBean();
        //intent to pubBean
        DataConverter.intentToPubBean(intent, pubBean);
        pubBean.setThirdCall(isThirdCall());
        //trans instance
        AbstractTrans trans;
        try {
            Class<? extends AbstractTrans> clz = TransUtils.getTrans(pubBean.getTransType());
            if (clz == null) {
                LoggerUtils.e("Transaction class is null.");
                intent.putExtra(TransTag.MESSAGE, getString(R.string.core_transaction_result_no_such_trans));
                intent.putExtra(TransTag.RESULT_CODE, ResultCode.FL);
                setResult(RESULT_CANCELED, intent);
                finish();
                return;
            }
            LoggerUtils.d("Instantiate transaction: " + clz);
            trans = clz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            LoggerUtils.e("Instantiate transaction failed!", e);
            intent.putExtra(TransTag.MESSAGE, getString(R.string.core_transaction_result_no_such_trans));
            intent.putExtra(TransTag.RESULT_CODE, ResultCode.FL);
            setResult(RESULT_CANCELED, intent);
            finish();
            return;
        }
        String transName = TransUtils.getName(pubBean.getTransType());
        LoggerUtils.i("Execute transaction: " + transName);
        setTitle(transName);
        pubBean.setTransName(transName);
        //trans init
        trans.init(this, pubBean);
        //transaction execute
        trans.transact((success) -> {
            //output transaction result
            DataConverter.pubBeanToIntent(pubBean, intent);
            if (success) {
                LoggerUtils.i(transName + "--success.");
                setResult(RESULT_OK, intent);
            } else {
                LoggerUtils.e(transName + "--failed[" + pubBean.getResultCode() + "]: " + pubBean.getMessage());
                setResult(RESULT_CANCELED, intent);
            }
            finish();
        });
    }

    @Override
    public void finish() {
        super.finish();
        BasePresentation.removeAllPresentations(this);
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_stay);
    }

    public boolean isThirdCall() {
        return THIRD_ACTION.equals(getIntent().getAction());
    }
}
