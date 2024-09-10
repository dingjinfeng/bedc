package acquire.settings.fragment.merchant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.InputUtils;
import acquire.base.utils.StringUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.constant.CardOrg;
import acquire.database.model.Merchant;
import acquire.database.service.MerchantService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.settings.R;
import acquire.settings.databinding.SettingsFragmentMerchantEditBinding;

/**
 * A {@link Fragment} that displays and configures merchant information.
 *
 * @author Janson
 * @date 2019/2/14 15:12
 */
public class MerchantEditFragment extends BaseFragment {
    private SettingsFragmentMerchantEditBinding binding;
    private Merchant merchant;
    private FragmentCallback<Merchant> callback;
    private boolean isModified;

    /**
     * create a fragment to edit merchant information.
     * @param merchant if null, it is to add a merchant; else, it is to modify a merchant.
     * @param callback the editing result.
     * @return A fragment to edit merchant information.
     */
    @NonNull
    public static MerchantEditFragment newInstance(@Nullable Merchant merchant, FragmentCallback<Merchant> callback) {
        MerchantEditFragment fragment = new MerchantEditFragment();
        if (merchant != null) {
            fragment.merchant = merchant.clone();
        }
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentMerchantEditBinding.inflate(inflater, container, false);
        MerchantService merchantService = new MerchantServiceImpl();
        //card organization
        List<String> items = new ArrayList<>();
        for (Field field : CardOrg.class.getDeclaredFields()) {
            if (field.getType() == String.class
                    && (Modifier.FINAL & field.getModifiers()) != 0
                    && (Modifier.STATIC & field.getModifiers()) != 0) {
                try {
                    String cardOrg = (String) field.get(CardOrg.class);
                    if (merchantService.find(cardOrg) == null){
                        items.add(cardOrg);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (merchant != null) {
            String currentCardOrg = merchant.getCardOrg();
            binding.toolbar.setTitle(currentCardOrg);
            if (!items.contains(currentCardOrg)){
                items.add(currentCardOrg);
            }
            binding.spCardOrg.setText(currentCardOrg);
            binding.spCardOrg.setSimpleItems(items.toArray(new String[0]));
        } else {
            merchant = new Merchant();
            binding.spCardOrg.setSimpleItems(items.toArray(new String[0]));
        }
        binding.spCardOrg.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                InputUtils.hideKeyboard(binding.spCardOrg);
            }
        });
        addClearErrorWatch(binding.tilCardOrg,binding.spCardOrg);

        //merchant id
        binding.etMid.setText(merchant.getMid());
        addClearErrorWatch(binding.tilMid,binding.etMid);

        //terminal id
        binding.etTid.setText(merchant.getTid());
        addClearErrorWatch(binding.tilTid,binding.etTid);

        //batch No
        binding.etBatch.setText(merchant.getBatchNo());
        addClearErrorWatch(binding.tilBatch,binding.etBatch);

        //confirm button
        binding.btnDone.setOnClickListener(v -> {
            String cardOrg = binding.spCardOrg.getText().toString();
            if (TextUtils.isEmpty(cardOrg)) {
                binding.tilCardOrg.setError(mActivity.getString(R.string.settings_merchant_edit_card_organization_error_null));
                return;
            }
            String mid = binding.etMid.getText().toString();
            if (TextUtils.isEmpty(mid)) {
                binding.tilMid.setError(mActivity.getString(R.string.settings_merchant_edit_mid_error_null));
                return;
            } else if (mid.length() != 15) {
                binding.tilMid.setError(mActivity.getString(R.string.settings_merchant_edit_mid_error_length));
                return;
            }
            String tid = binding.etTid.getText().toString();
            if (TextUtils.isEmpty(tid)) {
                binding.tilTid.setError(mActivity.getString(R.string.settings_merchant_edit_tid_error_null));
                return;
            } else if (tid.length() != 8) {
                binding.tilTid.setError(mActivity.getString(R.string.settings_merchant_edit_tid_error_length));
                return;
            }
            String batch = binding.etBatch.getText().toString();
            if (TextUtils.isEmpty(batch)) {
                binding.tilBatch.setError(mActivity.getString(R.string.settings_merchant_edit_batch_error_null));
                return;
            }
            merchant.setCardOrg(cardOrg);
            merchant.setMid(mid);
            merchant.setTid(tid);
            merchant.setBatchNo(StringUtils.fill(batch, "0", 6, true));
            if (callback != null) {
                callback.onSuccess(merchant);
            }
        });
        return binding.getRoot();
    }

    private void addClearErrorWatch(TextInputLayout textInputLayout,EditText editText){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textInputLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isModified = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    @Override
    public boolean onBack() {
        if (isModified){
            new MessageDialog.Builder(mActivity)
                    .setMessage(R.string.settings_merchant_edit_exit_prompt)
                    .setBackEnable(true)
                    .setCancelButton(R.string.base_cancel,v -> {
                    })
                    .setConfirmButton(R.string.base_ok,v -> callback.onFail(FragmentCallback.CANCEL, getString(R.string.base_cancel)))
                    .show();
        }else{
            callback.onFail(FragmentCallback.CANCEL, getString(R.string.base_cancel));
        }
        return true;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }

}
