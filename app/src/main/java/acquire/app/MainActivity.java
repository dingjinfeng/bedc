package acquire.app;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.IdRes;

import java.util.concurrent.CountDownLatch;

import acquire.app.databinding.AppActivityMainBinding;
import acquire.app.fragment.main.MainFragment;
import acquire.app.fragment.splash.SplashFragment;
import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.DisplayUtils;
import acquire.core.tools.SelfCheckHelper;
import acquire.sdk.ServiceHelper;
import acquire.sdk.system.BSystem;

/**
 * The main Activity
 *
 * @author Janson
 * @date 2018/10/4 18:13
 */
public class MainActivity extends BaseActivity {

    @Override
    public @IdRes
    int attachFragmentResId() {
        return R.id.fragment_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppActivityMainBinding binding = AppActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        //set immersed status bar.
        DisplayUtils.immersedStatusBar(getWindow());
        splashAnimation();
        /*
         * must be executed after SelfCheckHelper.initAppConfig(context) in App.class,
         * so use BaseApplication.SINGLE_EXECUTOR
         */
        BaseApplication.SINGLE_EXECUTOR.execute(() -> {
            SelfCheckHelper.initDevice(this);
            //disable task and home button
            BSystem.setTaskButton(false);
            BSystem.setHomeButton(false);
            //Wait for the splash animation to end
            splashWaitFinish();
            //Enter the main fragment
            mSupportDelegate.switchContent(MainFragment.newInstance());

        });
    }

    @Override
    protected void onDestroy() {
        if (ServiceHelper.getInstance().isInit()) {
            //restore task and home button
            BSystem.setTaskButton(true);
            BSystem.setHomeButton(true);
        }
        super.onDestroy();
    }

    private static CountDownLatch splashLatch;

    private void splashAnimation() {
        if (splashLatch == null) {
            splashLatch = new CountDownLatch(1);
            mSupportDelegate.switchContent(SplashFragment.newInstance(new SimpleCallback() {
                @Override
                public void result() {
                    splashLatch.countDown();
                }
            }));
        } else {
            splashLatch.countDown();
        }
    }

    private void splashWaitFinish() {
        if (splashLatch != null) {
            try {
                splashLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
