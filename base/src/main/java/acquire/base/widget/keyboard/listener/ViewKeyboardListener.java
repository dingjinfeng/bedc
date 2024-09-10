package acquire.base.widget.keyboard.listener;

import android.app.Instrumentation;
import android.view.KeyEvent;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.ThreadPool;


/**
 * A subclass of {@link KeyboardListener}
 *
 * @author Janson
 * @date 2018/3/2
 */
public abstract class ViewKeyboardListener implements KeyboardListener {
    private int mMaxLength = 999;

    public ViewKeyboardListener() {}

    public ViewKeyboardListener(int maxLength) {
        this.mMaxLength = maxLength;
    }

    /**
     * Get current text
     *
     * @return current text
     */
    public abstract String getText();

    /**
     * Set the text after inputing
     *
     * @param text text after inputting
     */
    public abstract void setText(String text);

    @Override
    public void onText(int code) {
        String text = getText();
        if (text == null) {
            text = "";
        }
        if (text.length() < mMaxLength) {
            text += (char) code;
            setText(text);
        }
    }

    @Override
    public void onBackspace() {
        String text = getText();
        if (text == null) {
            LoggerUtils.d("value:null");
            return;
        }
        if (text.length() > 0) {
            int index = text.length();
            text = text.substring(0, index - 1);
        } else {
            text = "";
        }
        setText(text);
    }

    @Override
    public void onEnter() {
    }

    @Override
    public void onClear() {
        setText("");
    }

    @Override
    public void onCancel() {
        ThreadPool.execute(()->{
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        });
    }

    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
    }
}
