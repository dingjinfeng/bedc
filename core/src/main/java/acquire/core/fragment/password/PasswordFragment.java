package acquire.core.fragment.password;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.keyboard.listener.ViewKeyboardListener;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.PasswordType;
import acquire.core.databinding.CoreFragmentPasswordBinding;
import acquire.core.databinding.CorePasswordItemBinding;
import acquire.core.tools.PhysicalKeyboardUtils;
import acquire.sdk.device.BDevice;

/**
 * A authorization password {@link Fragment}
 *
 * @author Janson
 * @date 2019/5/11 15:30
 */
public class PasswordFragment extends BaseFragment {
    public static final String VENDOR_PASSWORD = "201003";
    private FragmentCallback<String> mCallback;
    private @PasswordType.TypeDef int type;
    private final static String ARG_TITLE ="title";
    private final static String ARG_TYPE ="type";
    private CoreFragmentPasswordBinding binding;
    /**
     * create a password fragment to verify whether the next operation is allowed
     *
     * @param title    fragment title
     * @param type     password type
     * @param callback verification results
     */
    @NonNull
    public static PasswordFragment newInstance(String title, @PasswordType.TypeDef int type, FragmentCallback<String> callback) {
        PasswordFragment fragment = new PasswordFragment();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putString(ARG_TITLE,title);
        args.putInt(ARG_TYPE,type);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * create a password fragment with default title to verify whether the next operation is allowed
     *
     * @param type     password type
     * @param callback verification results
     */
    @NonNull
    public static PasswordFragment newInstance(@PasswordType.TypeDef int type, FragmentCallback<String> callback) {
        PasswordFragment fragment = new PasswordFragment();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE,type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int[] getPopAnimation() {
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentPasswordBinding.inflate(inflater, container, false);
        if (getArguments() != null){
            String title = getArguments().getString(ARG_TITLE);
            if (title != null){
                binding.toolbar.setTitle(title);
            }
            type = getArguments().getInt(ARG_TYPE);
        }
        String password;
        //CONTENT
        switch (type) {
            case PasswordType.SECURITY:
                binding.tvContent.setText(R.string.core_password_input_security_password);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_SECURITY);
                break;
            case PasswordType.SYSTEM_ADMIN:
                binding.tvContent.setText(R.string.core_password_input_system_admin_password);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_SYSTEM_ADMIN);
                break;
            case PasswordType.ADMIN:
                binding.tvContent.setText(R.string.core_password_input_admin_password);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_ADMIN);
                break;
            default:
                throw new IllegalArgumentException("PasswordFragment type error!");
        }

        //password edit text list
        PwdAdapter pwdAdapter = new PwdAdapter(password.length());
        binding.rvPwd.setAdapter(pwdAdapter);
        binding.keyboardNumber.setKeyBoardListener(new ViewKeyboardListener(password.length()) {
            private String inputText;

            @Override
            public String getText() {
                return inputText;
            }

            @Override
            public void setText(String text) {
                binding.tvError.setText(null);
                inputText = text;
                pwdAdapter.update(text.length());
                LoggerUtils.d("Enter password: " + text);
                //check whether the password length meets the requirements
                if (text.length() == password.length()) {
                    if (type == PasswordType.SYSTEM_ADMIN && text.equals(VENDOR_PASSWORD)) {
                        mCallback.onSuccess(text);
                        return;
                    }
                    //verify password
                    if (text.equals(password)) {
                        //right
                        mCallback.onSuccess(text);
                    } else {
                        //wrong
                        binding.tvError.setText(R.string.core_password_incorrect);
                        ViewUtils.shakeAnimatie(binding.rvPwd);
                    }
                }
            }
        });
        //check P300 physical keyboard
        if (BDevice.supportPhysicalKeyboard()){
            binding.keyboardNumber.setVisibility(View.GONE);
            PhysicalKeyboardUtils.setKeyboardListener(binding.rvPwd,binding.keyboardNumber.getKeyBoardListener());
        }
        return binding.getRoot();
    }

    @Override
    public void onFragmentHide() {
        if (BDevice.supportPhysicalKeyboard()){
            PhysicalKeyboardUtils.removeKeyboardListener(binding.rvPwd);
        }
    }
    @Override
    public FragmentCallback<String> getCallback() {
        return mCallback;
    }


    private static class PwdAdapter extends BaseBindingRecyclerAdapter<CorePasswordItemBinding> {
        private final int passwordLength;
        private int inputLength;

        public PwdAdapter(int passwordLength) {
            this.passwordLength = passwordLength;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void update(int inputLength) {
            this.inputLength = inputLength;
            notifyDataSetChanged();
        }

        @Override
        protected void bindItemData(CorePasswordItemBinding itemBinding, int position) {
            itemBinding.ivDot.setVisibility(inputLength >= position + 1 ? View.VISIBLE : View.GONE);
        }

        @Override
        protected Class<CorePasswordItemBinding> getViewBindingClass() {
            return CorePasswordItemBinding.class;
        }

        @Override
        public int getItemCount() {
            return passwordLength;
        }
    }
}
