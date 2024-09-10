package acquire.base.widget;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import acquire.base.ActivityStackManager;
import acquire.base.R;
import acquire.base.databinding.BasePrimaryToolbarBinding;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.thread.ThreadPool;


/**
 * A custom toolbar with a left navigation and title automatically filled according to {@link Activity#getTitle()}.
 *
 * @author Janson
 * @date 2019/2/1 15:54
 */
public class PrimaryToolbar extends Toolbar {
    private BasePrimaryToolbarBinding binding;

    public PrimaryToolbar(Context context) {
        this(context, null);
    }

    public PrimaryToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrimaryToolbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    /**
     * Init views
     */
    protected void initView(Context context, AttributeSet attrs, int defStyle) {
        binding = BasePrimaryToolbarBinding.inflate(LayoutInflater.from(context));
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PrimaryToolbar, defStyle, 0);
            //title
            String title = a.getString(R.styleable.PrimaryToolbar_title);
            boolean autoTitle = a.getBoolean(R.styleable.PrimaryToolbar_autoTitle, true);
            if (autoTitle && title == null) {
                Activity activity = ActivityStackManager.getTopActivity();
                if (activity != null) {
                    title = (String) activity.getTitle();
                }
            }
            binding.tvTitle.setText(title);
            boolean backVisibile = a.getBoolean(R.styleable.PrimaryToolbar_backVisibile, true);
            binding.ivBack.setVisibility(backVisibile ? VISIBLE : GONE);
            binding.ivBack.setOnClickListener(v ->
                ThreadPool.execute(() -> {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                })
            );
            Drawable rightDrawable = a.getDrawable(R.styleable.PrimaryToolbar_rightIcon);
            String rightText = a.getString(R.styleable.PrimaryToolbar_rightContent);
            if (rightDrawable != null || rightText!=null){
                binding.ivRight.setImageDrawable(rightDrawable);
                binding.tvRight.setText(rightText);
                binding.llRight.setVisibility(VISIBLE);
            }
            a.recycle();
        }
        if (getBackground() == null) {
            setBackgroundColor(ContextCompat.getColor(context,R.color.base_colorPrimary));
        }
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(binding.getRoot(), lp);
        //fits system windows
        DisplayUtils.fitsWindowStatus(binding.getRoot());
    }

    @Override
    public void setNavigationIcon(@Nullable Drawable icon) {
        binding.ivBack.setBackground(icon);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (binding != null) {
            binding.tvTitle.setText(title);
        }
    }

    public void setRightListener(OnClickListener listener) {
        if (binding != null) {
            binding.llRight.setOnClickListener(listener);
        }
    }
    public void setRightIcon(@Nullable Drawable icon) {
        if (binding != null) {
            binding.ivRight.setImageDrawable(icon);
        }
    }
    public void setRightText(CharSequence text) {
        if (binding != null) {
            binding.tvRight.setText(text);
        }
    }
}
