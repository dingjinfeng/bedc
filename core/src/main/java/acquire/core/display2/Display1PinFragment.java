package acquire.core.display2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentDisplay1PinBinding;
import acquire.core.databinding.CorePresentationPinBinding;
import acquire.core.fragment.pin.PinFragmentArgs;
import acquire.core.fragment.pin.PinViewModel;
import acquire.core.tools.CurrencyCodeProvider;


/**
 * A display1 pinpad {@link Fragment} for dual screen
 *
 * @author Janson
 * @date 2022/9/8 13:53
 */
public class Display1PinFragment extends BaseDialogFragment {
    private FragmentCallback<byte[]> mCallback;
    private PinFragmentArgs pinFragmentArgs;
    private CoreFragmentDisplay1PinBinding fragmentBinding;
    private PinPresentation presentation;
    @NonNull
    public static Display1PinFragment newInstance(PinFragmentArgs pinFragmentArgs, FragmentCallback<byte[]> callback) {
        Display1PinFragment fragment = new Display1PinFragment();
        fragment.pinFragmentArgs = pinFragmentArgs;
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentBinding = CoreFragmentDisplay1PinBinding.inflate(inflater, container, false);
        presentation = new PinPresentation(mActivity);
        presentation.show();
        fragmentBinding.btnExit.setOnClickListener(v ->
                new MessageDialog.Builder(mActivity)
                        .setMessage(R.string.core_presentation_cancel_pin)
                        .setBackEnable(true)
                        .setConfirmButton(dialog -> {
                            if (presentation.pinViewModel != null){
                                presentation.pinViewModel.cancelPin();
                            }
                        })
                        .setCancelButton(dialog -> {})
                        .show()
        );
        return fragmentBinding.getRoot();
    }


    @Override
    public int[] getPopAnimation() {
        return null;
    }

    @Override
    public FragmentCallback<byte[]> getCallback() {
        return mCallback;
    }

    @Override
    public boolean onBack() {
        return true;
    }


    @Override
    public void onStop() {
        presentation.pinViewModel.cancelPin();
        super.onStop();
    }

    private class PinPresentation extends BasePresentation {
        private PinViewModel pinViewModel;

        public PinPresentation(Context outerContext) {
            super(outerContext);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            CorePresentationPinBinding presentationBinding = CorePresentationPinBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
            pinViewModel = new ViewModelProvider(Display1PinFragment.this).get(PinViewModel.class);

            //prompts
            if (pinFragmentArgs.isOnlinePin()) {
                presentationBinding.tvMsg.setText(R.string.core_pin_input_password);
            } else {
                presentationBinding.tvMsg.setText(R.string.core_pin_input_offline_password);
            }
            //amount
            if (pinFragmentArgs.getAmount() != 0L) {
                String amt = CurrencyCodeProvider.getCurrencySymbol(pinFragmentArgs.getCurrencyCode()) + FormatUtils.formatAmount(pinFragmentArgs.getAmount());
                presentationBinding.tvAmount.setText(amt);
                fragmentBinding.tvAmount.setText(amt);
            }
            //card number
            presentationBinding.tvPan.setText(FormatUtils.maskCardNo(pinFragmentArgs.getPan()));
            //card organization
            presentationBinding.tvCardOrg.setText(pinFragmentArgs.getCardOrganization());
            presentationBinding.key0.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    presentationBinding.key0.getViewTreeObserver().removeOnPreDrawListener(this);
                    List<View> numberKeyViews = new ArrayList<>();
                    List<View> funcKeyViews = new ArrayList<>();
                    numberKeyViews.add(presentationBinding.key1);
                    numberKeyViews.add(presentationBinding.key2);
                    numberKeyViews.add(presentationBinding.key3);
                    numberKeyViews.add(presentationBinding.key4);
                    numberKeyViews.add(presentationBinding.key5);
                    numberKeyViews.add(presentationBinding.key6);
                    numberKeyViews.add(presentationBinding.key7);
                    numberKeyViews.add(presentationBinding.key8);
                    numberKeyViews.add(presentationBinding.key9);
                    numberKeyViews.add(presentationBinding.key0);
                    funcKeyViews.add(presentationBinding.keyBack);
                    funcKeyViews.add(presentationBinding.keyCancel);
                    funcKeyViews.add(presentationBinding.keyEnter);
                    pinViewModel.setRandomKeyboard(numberKeyViews, funcKeyViews);
                    pinViewModel.startPin(pinFragmentArgs.isOnlinePin(), pinFragmentArgs.getPan(), pinFragmentArgs.getPinLengths());
                    return true;
                }
            });

            pinViewModel.getError().observe(getViewLifecycleOwner(), pinError -> {
                if (pinError.getErrorCode() == PinViewModel.PinError.CANCEL) {
                    mCallback.onFail(FragmentCallback.CANCEL, pinError.getDescription());
                } else {
                    mCallback.onFail(FragmentCallback.FAIL, pinError.getDescription());
                }
            });
            pinViewModel.getInputLength().observe(getViewLifecycleOwner(), inputLength -> {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < inputLength; i++) {
                    builder.append(" * ");
                }
                presentationBinding.tvPin.setText(builder.toString());
            });
            pinViewModel.getPinBlock().observe(getViewLifecycleOwner(), pinBlock -> mCallback.onSuccess(pinBlock));
        }


    }
}
