package acquire.settings.fragment.password;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.PasswordType;
import acquire.settings.R;
import acquire.settings.databinding.SettingsFragmentPasswordChangeBinding;

/**
 * A {@link Fragment} that changes authorization password.
 *
 * @author Janson
 * @date 2019/8/9 9:09
 */
public class PasswordChangeFragment extends BaseFragment {
    private SettingsFragmentPasswordChangeBinding binding;
    private final static String ARG_TYPE ="type";
    private @PasswordType.TypeDef int type;

    @NonNull
    public static PasswordChangeFragment newInstance(@PasswordType.TypeDef int type){
        PasswordChangeFragment fragment = new PasswordChangeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE,type);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentPasswordChangeBinding.inflate(inflater,container,false);
        if (getArguments() != null){
            type = getArguments().getInt(ARG_TYPE);
        }
        String password;
        switch (type){
            case PasswordType.SECURITY:
                binding.toolbar.setTitle(R.string.settings_password_item_security);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_SECURITY);
                break;
            case PasswordType.SYSTEM_ADMIN:
                binding.toolbar.setTitle(R.string.settings_password_item_system_admin);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_SYSTEM_ADMIN);
                break;
            case PasswordType.ADMIN:
                binding.toolbar.setTitle(R.string.settings_password_item_admin);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_ADMIN);
                break;
            default:
                throw new IllegalArgumentException("PasswordChangeFragment type error!");
        }
        binding.tilOldPassowrd.setCounterMaxLength(password.length());
        binding.etOldPassowrd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(password.length())});
        binding.tilNewPassowrd.setCounterMaxLength(password.length());
        binding.etNewPassowrd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(password.length())});
        binding.tilNewPassowrd2.setCounterMaxLength(password.length());
        binding.etNewPassowrd2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(password.length())});

        binding.etOldPassowrd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tilOldPassowrd.setError(null);
            }
        });
        binding.etNewPassowrd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tilNewPassowrd.setError(null);
            }
        });
        binding.etNewPassowrd2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tilNewPassowrd2.setError(null);
            }
        });
        binding.btnDone.setOnClickListener(v->{
            String oldPassword = binding.etOldPassowrd.getText().toString();
            String newPassword = binding.etNewPassowrd.getText().toString();
            String newPassword2 = binding.etNewPassowrd2.getText().toString();
            String lengthError = getString(R.string.settings_password_error_password_length,password.length());
            //Orig. password is null
            if (oldPassword.length() == 0){
                binding.tilOldPassowrd.setError(getString(R.string.settings_password_error_password_null));
                return;
            }
            //New password length wrong
            if (oldPassword.length() != password.length()){
                binding.tilOldPassowrd.setError(lengthError);
                return;
            }
            //Orig. password is wrong
            if (!oldPassword.equals(password)){
                binding.tilOldPassowrd.setError(getString(R.string.settings_password_error_old_password_incorrect));
                return;
            }
            //New password is null
            if (newPassword.length() == 0){
                binding.tilNewPassowrd.setError(getString(R.string.settings_password_error_password_null));
                return;
            }
            //New password length wrong
            if (newPassword.length() != password.length()){
                binding.tilNewPassowrd.setError(lengthError);
                return;
            }
            //Confirm password is null
            if (newPassword2.length() == 0){
                binding.tilNewPassowrd2.setError(getString(R.string.settings_password_error_password_null));
                return;
            }
            //Confirm password length wrong
            if (newPassword2.length() != password.length()){
                binding.tilNewPassowrd2.setError(lengthError);
                return;
            }
            //New password and Confirm password  is not same.
            if (!newPassword.equals(newPassword2)){
                binding.tilNewPassowrd2.setError(getString(R.string.settings_password_error_new_password_difference));
                return;
            }

            if (newPassword.equals(oldPassword)){
                binding.tilNewPassowrd.setError(getString(R.string.settings_password_error_new_password_same_old));
                return;
            }
            switch (type){
                case PasswordType.SECURITY:
                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_SECURITY,newPassword);
                    break;
                case PasswordType.SYSTEM_ADMIN:
                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_SYSTEM_ADMIN,newPassword);
                    break;
                case PasswordType.ADMIN:
                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_ADMIN,newPassword);
                    break;
                default:
                    throw new IllegalArgumentException("PasswordChangeFragment type error!");
            }
            ToastUtils.showToast(R.string.settings_password_update_password_success);
            mSupportDelegate.popBackFragment(1);
        });
        return binding.getRoot();
    }
    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }


}
