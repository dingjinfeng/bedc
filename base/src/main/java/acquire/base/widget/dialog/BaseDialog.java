package acquire.base.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;

import acquire.base.R;
import acquire.base.lifecycle.CloseDialogLife;
import acquire.base.utils.TimerHandler;


/**
 * A basic class of dialog.
 * <p>create a dialog extends {@link BaseDialog}:</p>
 * <pre>
 *     class ADialog extends BaseDialog{
 *         protected View bindView(LayoutInflater inflater) {
 *              binding = ADialogBinding.inflate(inflater);
 *              return binding.getRoot();
 *         }
 *         protected void init() {
 *              //Handle tasks
 *              //such as: setCancelable(false);
 *         }
 *         protected Button getButton(int which) {
 *              switch (which) {
 *                  case BUTTON_CONFIRM:
 *                      return binding.btnConfirm;
 *                  case BUTTON_CANCEL:
 *                      return binding.btnCancel;
 *                  default:
 *                      return binding.btnConfirm;
 *              }
 *          }
 *     }
 * </pre>
 *
 * @author Janson
 * @date 2019/2/16 15:36
 */
public abstract class BaseDialog extends Dialog {

    private long timeoutMillis = 0;
    private TimeoutListener timeoutListener;

    private final TimerHandler timerHandler = new TimerHandler();
    private final View root;
    private LifecycleObserver observer;
    private ComponentActivity activity;

    public BaseDialog(@NonNull Context context) {
        this(context, R.style.BaseDialog);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        root = bindView(LayoutInflater.from(context));
        if (context instanceof ComponentActivity) {
            activity = (ComponentActivity) context;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(root);
        init();
        if (timeoutMillis > 0 && timeoutListener != null) {
            //start timeout
            timerHandler.startTimeout(timeoutMillis, () -> timeoutListener.onTimeout(this));
        }
        if (activity != null) {
            observer = new CloseDialogLife(this);
            activity.getLifecycle().addObserver(observer);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //close timer
        timerHandler.stopTimeout();
        if (observer != null && activity != null) {
            activity.getLifecycle().removeObserver(observer);
            observer = null;
        }
    }

    /**
     * Set timeout
     */
    public void setTimeOut(long timeoutMillis, TimeoutListener listener) {
        this.timeoutMillis = timeoutMillis;
        this.timeoutListener = listener;
    }

    /**
     * {@link #setContentView} this dialog layout
     *
     * @param inflater Layout inflater form context
     * @return root view
     */
    protected abstract View bindView(LayoutInflater inflater);

    /**
     * Init data and handles
     */
    protected abstract void init();

    /**
     * Dialog time out listener
     *
     * @author Janson
     * @date 2022/5/30 20:28
     */
    public interface TimeoutListener {
        void onTimeout(Dialog dialog);
    }

    /**
     * Dialog click listener
     *
     * @author Janson
     * @date 2020/5/25 14:02
     */
    public interface OnClickListener {
        void onClick(Dialog dialog);
    }
}
