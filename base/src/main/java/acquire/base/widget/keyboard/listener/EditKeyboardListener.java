package acquire.base.widget.keyboard.listener;

import android.app.Instrumentation;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import acquire.base.utils.InputUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.ThreadPool;


/**
 * A subclass of {@link KeyboardListener} for {@link EditText}
 *
 * @author Janson
 * @date 2018/3/2
 */
public abstract class EditKeyboardListener implements KeyboardListener {

    private int mMaxLength;
    protected EditText mEditText;

    public EditKeyboardListener(EditText editText) {
        this(editText,  999);
    }

    public EditKeyboardListener(EditText editText, int maxLength) {
        this.mEditText = editText;
        this.mMaxLength = maxLength;
        InputUtils.hideKeyboardByEditText(editText);
    }

    @Override
    public void onText(int code) {
        Editable editable= mEditText.getText();
        int start = mEditText.getSelectionStart();
        if (editable.length() < mMaxLength) {
            if (mEditText.isFocused()) {
                editable.insert(start, Character.toString((char) code));
            } else {
                editable.append((char) code);
            }
        }
        LoggerUtils.d("value:"+mEditText.getText().toString() );
    }

    @Override
    public void onBackspace() {
        Editable editable= mEditText.getText();
        int start = mEditText.getSelectionStart();
        if (editable != null && editable.length() > 0) {
            if (start > 0) {
                editable.delete(start - 1, start);
            }
        }
        LoggerUtils.d("value: "+mEditText.getText().toString() );
    }

    @Override
    public void onClear() {
        mEditText.setText("");
    }

    @Override
    public void onCancel() {
        ThreadPool.execute(()->{
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        });
    }

    /**
     * Set max input length
     */
    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
    }

    /**
     * Set attached {@link EditText}
     */
    public void setTargetView(EditText editText) {
        this.mEditText = editText;
        InputUtils.hideKeyboardByEditText(editText);
    }
    public View getTargetView() {
        return mEditText;
    }

}
