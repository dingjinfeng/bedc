package acquire.core.fragment.result;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentResultSimpleBinding;

/**
 * A {@link Fragment} that displays the simple result information
 *
 * @author Janson
 * @date 2019/1/23 10:22
 */
public class ResultSimpleFragment extends BaseFragment {

    /**
     * Result callback
     */
    private SimpleCallback mCallback;

    private CoreFragmentResultSimpleBinding binding;
    private final static String ARG_MESSAGE = "MESSAGE";
    private final static String ARG_RESULT = "RESULT";

    @NonNull
    public static ResultSimpleFragment newInstance(boolean success, String message, SimpleCallback callback) {
        ResultSimpleFragment fragment = new ResultSimpleFragment();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putBoolean(ARG_RESULT, success);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentResultSimpleBinding.inflate(inflater, container, false);
        if (getArguments() != null){
            binding.tvResult.setText(getArguments().getString(ARG_MESSAGE));
            if (!getArguments().getBoolean(ARG_RESULT)){
                binding.ivResult.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.core_result_ic_fail));
                binding.tvResult.setTextColor(ContextCompat.getColor(mActivity, R.color.base_warning));
            }
        }
        //count down timer
        CountDownTimer timer = new CountDownTimer(5 * 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int countDown = (int) Math.ceil(millisUntilFinished / 1000f);
                String content = String.format(Locale.getDefault(),"%s(%d)",
                        mActivity.getString(R.string.base_done), countDown );
                binding.btnDone.setText(content);
            }

            @Override
            public void onFinish() {
                if (mCallback != null) {
                    mCallback.onSuccess(null);
                    mCallback = null;
                }
            }
        }.start();

        //done button
        binding.btnDone.setOnClickListener(v -> {
            timer.cancel();
            if (mCallback != null) {
                mCallback.onSuccess(null);
                mCallback = null;
            }
        });
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return mCallback;
    }


}
