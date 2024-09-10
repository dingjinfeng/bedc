package acquire.base.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import acquire.base.lifecycle.LogLife;
import acquire.base.utils.LoggerUtils;

/**
 * A basic class extended {@link AppCompatActivity}. Its Features:
 * <ul>
 *     <li>
 *         <p><b>manage the fragment</b></p>
 *         <pre>
 *          //open fragment
 *          mSupportDelegate.switchContent(fragment);
 *          //Back to the first fragment in the back stack.
 *          mSupportDelegate.switchFirstContent(fragment);
 *          //pop count fragments
 *          mSupportDelegate.popBackFragment(count);
 *         </pre>
 *     </li>
 *     <li>
 *         <p><b>start an Activity with a callback</b></p>
 *         <pre>
 *          Intent intent = new Intent(this, TransActivity.class);
 *          intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SALE);
 *          mSupportDelegate.startActivityForResult(intent, null, result -> {
 *              //activity result
 *              if (result.getResultCode() == Activity.RESULT_OK){
 *              }
 *              else{
 *              }
 *          });
 *         </pre>
 *     </li>
 * </ul>
 *
 * @author Janson
 * @date 2018/9/12 23:11
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Support Delegate.
     */
    public SupportDelegate mSupportDelegate;
    /**
     * Print {@link Activity} life log
     */
    private LogLife lifecycleObserver;


    /**
     * Get a layout id that show {@link Fragment}。If 0,it means to not use {@link Fragment}
     *
     * @return fragment layout {@link android.R.id}
     */
    public abstract @IdRes int attachFragmentResId();


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                LoggerUtils.d("onKeyDown Home");
                break;
            case KeyEvent.KEYCODE_BACK:
                LoggerUtils.d("onKeyDown BACK");
                break;
            case KeyEvent.KEYCODE_MENU:
                LoggerUtils.d("onKeyDown MENU");
                break;
            case KeyEvent.KEYCODE_DEL:
                LoggerUtils.d("onKeyDown DEL");
                break;
            case KeyEvent.KEYCODE_ENTER:
                LoggerUtils.d("onKeyDown ENTER");
                break;
            default:
                LoggerUtils.d("onKeyDown " + keyCode);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //print activity life log
        if (lifecycleObserver == null) {
            lifecycleObserver = new LogLife();
            getLifecycle().addObserver(lifecycleObserver);
        }
        //Support Delegate
        mSupportDelegate = new SupportDelegate(getSupportFragmentManager(), attachFragmentResId());
    }

}
