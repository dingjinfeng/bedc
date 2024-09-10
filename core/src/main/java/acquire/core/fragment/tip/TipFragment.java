package acquire.core.fragment.tip;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.AmountFilter;
import acquire.base.widget.keyboard.listener.EditKeyboardListener;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentTipBinding;
import acquire.core.tools.CurrencyCodeProvider;


/**
 * A entry tip amount {@link Fragment}
 *
 * @author Janson
 * @date 2022/8/9 8:55
 */
public class TipFragment extends BaseFragment {

    private FragmentCallback<Long> mCallback;
    private CoreFragmentTipBinding binding;
    private final static int DECIMAL = 2;
    private final static String ARG_CURRENCY = "CURRENCY_CODE";
    private final static String ARG_AMOUNTY = "AMOUNT";

    @NonNull
    public static TipFragment newInstance(String currencyCode, long amount, FragmentCallback<Long> callback) {
        TipFragment fragment = new TipFragment();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putString(ARG_CURRENCY, currencyCode);
        args.putLong(ARG_AMOUNTY, amount);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentTipBinding.inflate(inflater, container, false);
        long amount = 0;
        String currencyFlag = "";
        if (getArguments() != null) {
            currencyFlag = CurrencyCodeProvider.getCurrencySymbol(getArguments().getString(ARG_CURRENCY));
            amount = getArguments().getLong(ARG_AMOUNTY);
        }
        final long origAmount = amount;
        String formatAmount = FormatUtils.formatAmount(origAmount, DECIMAL);
        binding.tvAmount.setText(formatAmount);
        binding.tvOrigAmount.setText(getString(R.string.core_tip_ori_amount_format, currencyFlag, formatAmount));
        binding.tvCurrency.setText(currencyFlag);

        //keyboard
        ViewUtils.setFocus(binding.etTip);
        binding.etTip.setFilters(new InputFilter[]{new AmountFilter(DECIMAL)});
        binding.keyboardNumber.setKeyBoardListener(new EditKeyboardListener(binding.etTip, 13) {

            @Override
            public void onText(int code) {
                String oldText = binding.etTip.getText().toString();
                super.onText(code);
                String newText = binding.etTip.getText().toString();
                if (getAmount(newText) > origAmount){
                    //invalid tip
                    binding.etTip.getEditableText().clear();
                    binding.etTip.getEditableText().append(oldText);
                }else  if (getAmount(newText) != getAmount(oldText)){
                    binding.groupPercents.clearChecked();
                }
            }

            @Override
            public void onBackspace() {
                binding.groupPercents.clearChecked();
                super.onBackspace();
            }

            @Override
            public void onClear() {
                binding.groupPercents.clearChecked();
                super.onClear();
            }

            @Override
            public void onEnter() {
                //enter tip
                String strTip = binding.etTip.getText().toString();
                LoggerUtils.d("enter tip: " + strTip);
                if (TextUtils.isEmpty(strTip)) {
                    mCallback.onSuccess(0L);
                    return;
                }

                long tip = getAmount(strTip);
                if (tip > origAmount) {
                    ToastUtils.showToast(R.string.core_tip_amount_limit_error);
                    return;
                }
                mCallback.onSuccess(tip);
            }
        });
        binding.etTip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                long tip = 0;
                String regex = "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0," + DECIMAL + "})?$";
                if (s.length() > 0 && text.matches(regex)) {
                    tip = getAmount(text);
                }
                long totalAmount = origAmount + tip;
                binding.tvAmount.setText(FormatUtils.formatAmount(totalAmount));
            }
        });
        binding.groupPercents.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                Button button = binding.getRoot().findViewById(checkedId);
                String percents = button.getText().toString().replace("%", "");
                long tip = origAmount * Long.parseLong(percents) / 100;
                String strTip = FormatUtils.formatAmount(tip, DECIMAL,"");
                binding.etTip.getEditableText().clear();
                binding.etTip.getEditableText().append(strTip);
            }
        });
        return binding.getRoot();
    }


    private long getAmount(String amountText) {
        double power = Math.pow(10, DECIMAL);
        try {
            double amount = Double.parseDouble(amountText);
            return (long) (amount * power);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public FragmentCallback<Long> getCallback() {
        return mCallback;
    }


}
