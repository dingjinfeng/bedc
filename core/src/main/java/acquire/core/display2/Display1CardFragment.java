package acquire.core.display2;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.chain.Chain;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.thread.Locker;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.image.ImageDialog;
import acquire.core.R;
import acquire.core.bean.PubBean;
import acquire.core.bean.StepBean;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.databinding.CoreFragmentDisplay1CardBinding;
import acquire.core.databinding.CorePresentationCardBinding;
import acquire.core.fragment.card.CardFragmentArgs;
import acquire.core.fragment.card.CardFragmentCallback;
import acquire.core.tools.CurrencyCodeProvider;
import acquire.core.tools.EmvHelper;
import acquire.core.tools.PinpadHelper;
import acquire.core.trans.BaseStep;
import acquire.core.trans.steps.EmvOfflineStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.sdk.emv.bean.EmvFetchBean;
import acquire.sdk.emv.bean.EmvLaunchParam;
import acquire.sdk.emv.bean.EmvReadyBean;
import acquire.sdk.emv.bean.PinResult;
import acquire.sdk.emv.constant.EmvResult;
import acquire.sdk.emv.constant.EmvTransType;
import acquire.sdk.emv.constant.EntryMode;
import acquire.sdk.emv.listener.EmvListener;
import acquire.sdk.emv.listener.EmvSecondGacListener;
import acquire.sdk.pin.constant.KeyAlgorithmType;
import acquire.sdk.sound.BBeeper;


/**
 * A display1 reading card {@link Fragment} for dual screen
 *
 * @author Janson
 * @date 2022/9/8 13:53
 */
public class Display1CardFragment extends BaseDialogFragment {
    private CoreFragmentDisplay1CardBinding fragmentBinding;
    private CardFragmentCallback callback;
    private CardFragmentArgs cardFragmentArgs;
    private final EmvHelper mEmvHelper = new EmvHelper();
    private boolean forceExit;
    private CardPresentation presentation;
    private Dialog removeCardDialog;
    private DialogPresentation removePresentation;

    @NonNull
    public static Display1CardFragment newInstance(CardFragmentArgs cardFragmentArgs, CardFragmentCallback callback) {
        Display1CardFragment fragment = new Display1CardFragment();
        fragment.cardFragmentArgs = cardFragmentArgs;
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentBinding = CoreFragmentDisplay1CardBinding.inflate(inflater, container, false);
        //manual
        fragmentBinding.manual.setOnClickListener(v -> {
            LoggerUtils.e("cancel card,go to manual.");
            mEmvHelper.cancelEmv();
            forceExit = true;
            callback.onManual();
        });
        //show amount
        PubBean pubBean = cardFragmentArgs.getStepBean().getPubBean();
        if (pubBean.getAmount() != 0L) {
            String amount = CurrencyCodeProvider.getCurrencySymbol(pubBean.getCurrencyCode())
                    + FormatUtils.formatAmount(pubBean.getAmount());
            fragmentBinding.tvAmount.setText(amount);
        }
        //cancel card
        fragmentBinding.btnExit.setOnClickListener(v -> onBack());
        int supportEntry = cardFragmentArgs.getSupportEntry();
        if ((supportEntry & EntryMode.MANUAL) != 0) {
            fragmentBinding.manual.setVisibility(View.VISIBLE);
        } else {
            fragmentBinding.manual.setVisibility(View.GONE);
        }
        presentation = new CardPresentation(mActivity);
        presentation.show();
        return fragmentBinding.getRoot();
    }


    @Override
    public int[] getPopAnimation() {
        return null;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        PubBean pubBean = cardFragmentArgs.getStepBean().getPubBean();
        //exit
        if (presentation.dialogPresentation != null && presentation.dialogPresentation.isShowing()) {
            if (presentation.dialogPresentation.getCancelButton() != null) {
                presentation.dialogPresentation.getCancelButton().callOnClick();
            }
        } else {
            pubBean.setResultCode(ResultCode.UC);
            pubBean.setMessage(R.string.core_card_cancel);
            mEmvHelper.cancelEmv();
        }
        return true;
    }

    void showDisplay1Message(String prompt) {
        fragmentBinding.tvPrompt.setText(prompt);
    }

    void setDisplay1Manual(boolean enable) {
        fragmentBinding.manual.setEnabled(enable);
    }


    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        if (removeCardDialog != null) {
            removeCardDialog.dismiss();
            removeCardDialog = null;
        }
        if (removePresentation != null) {
            removePresentation.dismiss();
            removePresentation = null;
        }
    }


    private void terminateTransaction() {
        mEmvHelper.terminateTransaction();
        LoggerUtils.d("check whether the card exists...");
        int count = 0;
        //TODO 测试
//        if (cardFragmentArgs.getStepBean().getPubBean().isThirdCall()) {
//            return;
//        }
        EmvHelper emvHelper = new EmvHelper();
        boolean isShow = false;
        int ret;
        while ((ret = emvHelper.cardExist()) != 0) {
            if (!isShow) {
                int image = ret == 1 ? R.drawable.core_remove_insert_card_warn_x800 : R.drawable.core_remove_tap_card_warn_x800;
                ThreadPool.postOnMain(() -> {
                    removeCardDialog = new ImageDialog.Builder(mActivity)
                            .setImage(image)
                            .setMessage(R.string.core_remove_card)
                            .show();
                    removePresentation = new DialogPresentation.Builder(mActivity)
                            .setImage(image)
                            .setMessage(R.string.core_remove_card)
                            .show();
                });
                isShow = true;
            }
            try {
                if (count > 10 && count % 8 == 0) {
                    BBeeper.beep(750, 200);
                }
                count++;
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isShow) {
            ThreadPool.postOnMain(() -> {
                if (removeCardDialog != null) {
                    removeCardDialog.dismiss();
                    removeCardDialog = null;
                }
                if (removePresentation != null) {
                    removePresentation.dismiss();
                    removePresentation = null;
                }
            });
        }

        LoggerUtils.d("check card over...");
    }


    private class CardPresentation extends BasePresentation {
        private final CardReadingPresentation cardReadingPresentation = new CardReadingPresentation(mActivity);
        private CorePresentationCardBinding presentationBinding;
        private DialogPresentation dialogPresentation;

        public CardPresentation(Context outerContext) {
            super(outerContext);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            presentationBinding = CorePresentationCardBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
            int supportEntry = cardFragmentArgs.getSupportEntry();
            StepBean stepBean = cardFragmentArgs.getStepBean();
            PubBean pubBean = stepBean.getPubBean();
            //show amount
            if (pubBean.getAmount() != 0L) {
                String amount = CurrencyCodeProvider.getCurrencySymbol(pubBean.getCurrencyCode()) + FormatUtils.formatAmount(pubBean.getAmount());
                presentationBinding.tvAmount.setText(amount);
            } else {
                presentationBinding.tvAmount.setVisibility(View.INVISIBLE);
            }
            //show supported card entries
            showEntryAnimation(supportEntry);
            //read card
            startReadCard(pubBean, supportEntry, cardFragmentArgs.isForcePin(), stepBean, cardFragmentArgs.getPackStep(), cardFragmentArgs.getPinStep());
        }

        /**
         * read card by EMV
         */
        private void startReadCard(PubBean pubBean, int supportEntry, boolean forcePin, StepBean stepBean, BaseStep packStep, BaseStep pinStep) {
            ThreadPool.execute(() -> {
                int emvTransType;
                switch (pubBean.getTransType()) {
                    case TransType.TRANS_SALE:
                        emvTransType = EmvTransType.SALE;
                        break;
                    case TransType.TRANS_PRE_AUTH:
                        emvTransType = EmvTransType.PREAUTH;
                        break;
                    case TransType.TRANS_BALANCE:
                        emvTransType = EmvTransType.BALANCE;
                        break;
                    default:
                    case TransType.TRANS_REFUND:
                    case TransType.TRANS_VOID_SALE:
                        emvTransType = EmvTransType.REFUND;
                        break;
                }
                EmvLaunchParam param = new EmvLaunchParam.Builder(emvTransType)
                        .timeout(60)
                        .entryMode(supportEntry)
                        .amount(pubBean.getAmount())
                        .create();
                mEmvHelper.terminateTransaction();
                mEmvHelper.readCard(param, new EmvListener() {
                    private boolean hasInputPin;
                    private boolean retry;

                    @Override
                    public void onReady(EmvReadyBean emvReadyBean) {
                        LoggerUtils.d("startReadCard: onReady");
                        mActivity.runOnUiThread(() -> {
                            if ((supportEntry & EntryMode.MANUAL) != 0) {
                                setDisplay1Manual(true);
                            }
                            cardReadingPresentation.hide();
                            if (emvReadyBean.getStatus() == EmvReadyBean.FALLBACK) {
                                if (emvReadyBean.getSupportEntries() == EntryMode.MAG) {
                                    presentationBinding.tvError.setText(R.string.core_card_fallback_to_mag_prompt);
                                    showDisplay1Message(getString(R.string.core_card_fallback_to_mag_prompt));
                                } else {
                                    presentationBinding.tvError.setText(R.string.core_card_fallback_tp_insert_mag_prompt);
                                    showDisplay1Message(getString(R.string.core_card_fallback_tp_insert_mag_prompt));
                                }
                                ViewUtils.shakeAnimatie(presentationBinding.tvError);
                            } else if (emvReadyBean.getStatus() == EmvReadyBean.USE_CHIP) {
                                presentationBinding.tvError.setText(R.string.core_card_icc_prompt);
                                showDisplay1Message(getString(R.string.core_card_icc_prompt));
                                ViewUtils.shakeAnimatie(presentationBinding.tvError);
                            } else if (emvReadyBean.getStatus() == EmvReadyBean.AGAIN) {
                                presentationBinding.tvError.setText(R.string.core_card_try_again);
                                showDisplay1Message(getString(R.string.core_card_try_again));
                                ViewUtils.shakeAnimatie(presentationBinding.tvError);
                            }
                            if (supportEntry != emvReadyBean.getSupportEntries()) {
                                showEntryAnimation(emvReadyBean.getSupportEntries());
                            }
                        });
                    }

                    @Override
                    public void onReading() {
                        LoggerUtils.d("startReadCard: onReading");
                        mActivity.runOnUiThread(() -> {
                            presentationBinding.tvError.setText(null);
                            cardReadingPresentation.show();
                            setDisplay1Manual(false);
                            showDisplay1Message(getString(R.string.core_card_reading_hint));
                        });
                    }

                    @Override
                    public int onSelectAid(List<String> preferNames) {
                        LoggerUtils.d("startReadCard: onSelectAid");
                        Locker<Integer> locker = new Locker<>(-1);
                        mActivity.runOnUiThread(() -> {
                            showDisplay1Message(getString(R.string.core_presentation_waiting_user_aids));
                            new MenuPresentation.Builder(mActivity)
                                    .setItems(preferNames)
                                    .setTitle(R.string.core_emv_aids_dialog_title)
                                    .setConfirmButton(index -> {
                                        locker.setResult(index);
                                        locker.wakeUp();
                                    }).setCancelButton(dialog -> {
                                        pubBean.setResultCode(ResultCode.UC);
                                        pubBean.setMessage(R.string.core_card_cancel);
                                        locker.setResult(-1);
                                        locker.wakeUp();
                                    })
                                    .show();
                        });
                        locker.waiting();
                        return locker.getResult();
                    }

                    @Override
                    public boolean onInsertError() {
                        LoggerUtils.d("startReadCard: onInsertError");
                        Locker<Boolean> locker = new Locker<>(false);
                        mActivity.runOnUiThread(() -> {
                            showDisplay1Message(getString(R.string.core_card_chip_error_dialog_message));
                            dialogPresentation = new DialogPresentation.Builder(mActivity)
                                    .setMessage(R.string.core_card_chip_error_dialog_message)
                                    .setConfirmButton(R.string.core_card_chip_error_dialog_button_ok, (dialog, which) -> {
                                        locker.setResult(true);
                                        locker.wakeUp();
                                    })
                                    .setCancelButton(R.string.base_exit, (dialog, which) -> {
                                        LoggerUtils.e("cancel card.");
                                        pubBean.setResultCode(ResultCode.UC);
                                        pubBean.setMessage(R.string.core_card_cancel);
                                        locker.setResult(false);
                                        locker.wakeUp();
                                    })
                                    .show();
                        });
                        locker.waiting();
                        return locker.getResult();
                    }

                    @Override
                    public void onFinalSelect() {
                        LoggerUtils.d("startReadCard: onFinalSelect");
                        EmvFetchBean emvFetchBean = mEmvHelper.getEmvFetchBean();
                        pubBean.setEntryMode(emvFetchBean.getUserEntryMode());
                    }

                    @Override
                    public boolean onSeePhone() {
                        LoggerUtils.d("startReadCard: onSeePhone");
                        Locker<Boolean> locker = new Locker<>(false);
                        mActivity.runOnUiThread(() -> {
                            showDisplay1Message(getString(R.string.core_card_try_again));
                            dialogPresentation = new DialogPresentation.Builder(mActivity)
                                    .setMessage(R.string.core_card_try_again)
                                    .setConfirmButton((dialog, which) -> {
                                        locker.setResult(true);
                                        locker.wakeUp();
                                    })
                                    .setCancelButton(R.string.base_exit, (dialog, which) -> {
                                        LoggerUtils.e("cancel card.");
                                        pubBean.setResultCode(ResultCode.UC);
                                        pubBean.setMessage(R.string.core_card_cancel);
                                        locker.setResult(false);
                                        locker.wakeUp();
                                    })
                                    .show();
                        });
                        locker.waiting();
                        return locker.getResult();
                    }

                    @Override
                    public boolean onCardNum(String pan) {
                        LoggerUtils.d("startReadCard: onCardNum");
                        if (TextUtils.isEmpty(pan)) {
                            pubBean.setMessage(R.string.core_card_null);
                            pubBean.setResultCode(ResultCode.FL);
                            return false;
                        }
                        EmvFetchBean emvFetchBean = mEmvHelper.getEmvFetchBean();
                        if (pubBean.getEntryMode() == 0) {
                            pubBean.setEntryMode(emvFetchBean.getUserEntryMode());
                        }
                        if (pubBean.getCardOrg() == null) {
                            //card organization
                            pubBean.setCardOrg(mEmvHelper.getCardOrg(pubBean.getEntryMode(), pan));
                        }
                        if (pubBean.getEntryMode() == EntryMode.MAG) {
                            // mag
                            String track2 = emvFetchBean.getTrack2();
                            if (TextUtils.isEmpty(track2)) {
                                ToastUtils.showToast(R.string.core_card_swipe_again);
                                retry = true;
                                return false;
                            }
                        }
                        pubBean.setTrack2(emvFetchBean.getTrack2());
                        pubBean.setTrack3(emvFetchBean.getTrack3());
                        pubBean.setExpDate(emvFetchBean.getExpDate());
                        pubBean.setCardNo(pan);
                        //TODO 测试
//                        if (pubBean.isThirdCall()) {
//                            return true;
//                        }

                        Locker<Boolean> locker = new Locker<>(false);
                        mActivity.runOnUiThread(() -> {
                            cancelAnimation();
                            cardReadingPresentation.hide();
                            setDisplay1Manual(false);
                            showDisplay1Message(FormatUtils.maskCardNo(pan));
                            dialogPresentation = new DialogPresentation.Builder(mActivity)
                                    .setMessage(FormatUtils.maskCardNo(pan))
                                    .setConfirmButton((dialog, which) -> {
                                        locker.setResult(true);
                                        locker.wakeUp();
                                    })
                                    .setCancelButton(R.string.base_exit, (dialog, which) -> {
                                        LoggerUtils.e("cancel card.");
                                        pubBean.setResultCode(ResultCode.UC);
                                        pubBean.setMessage(R.string.core_card_cancel);
                                        locker.setResult(false);
                                        locker.wakeUp();
                                    })
                                    .show();
                        });
                        locker.waiting();
                        return locker.getResult();
                    }

                    @Override
                    public PinResult onInputPin(boolean isOnlinePin, int pinTryCount) {
                        LoggerUtils.d("startReadCard: onInputPin");
                        hasInputPin = true;
                        if (pubBean.getCardNo() == null) {
                            EmvFetchBean emvFetchBean = mEmvHelper.getEmvFetchBean();
                            pubBean.setCardNo(emvFetchBean.getPan());
                        }
                        if (pubBean.getCardOrg() == null) {
                            //card organization
                            pubBean.setCardOrg(mEmvHelper.getCardOrg(pubBean.getEntryMode(), pubBean.getCardNo()));
                        }
                        if (pinStep == null) {
                            // Not need pin
                            return PinResult.newStatusByPass();
                        }
                        if (pinStep instanceof InputPinStep) {
                            InputPinStep inputPinStep = (InputPinStep) pinStep;
                            inputPinStep.setOfflinePin(!isOnlinePin);
                            inputPinStep.setEmvMode(true);
                        }
                        Locker<PinResult> locker = new Locker<>();
                        Chain<StepBean> chain = new Chain<>(stepBean);
                        chain.next(pinStep)
                                .proceed(isSucc -> {
                                    if (isSucc) {
                                        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD)) {
                                            PinpadHelper.waitKsn();
                                            //external PIN pad
                                            int pinIndex = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_MASTER_KEY_INDEX);
                                            int algorithmType = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_ALGORITHM_TYPE, KeyAlgorithmType.DUKPT);
                                            int timeoutSec = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_TIMEOUT, 60);
                                            locker.setResult(PinResult.newStatusExtPinpad(pinIndex, algorithmType, timeoutSec));
                                        } else {
                                            //built-in card reader
                                            String pinBlock;
                                            if (isOnlinePin) {
                                                pinBlock = pubBean.getPinBlock();
                                            } else {
                                                pinBlock = pubBean.getOfflinePinBlock();
                                            }
                                            if (TextUtils.isEmpty(pinBlock)) {
                                                //No input any PIN.
                                                locker.setResult(PinResult.newStatusByPass());
                                            } else {
                                                locker.setResult(PinResult.newStatusOk(BytesUtils.hexToBytes(pinBlock)));
                                            }
                                        }
                                    } else {
                                        //Cancel PIN
                                        locker.setResult(PinResult.newStatusCancel());
                                    }
                                    locker.wakeUp();
                                });
                        locker.waiting();
                        return locker.getResult();

                    }

                    @Override
                    public void onResult(boolean success, int emvResult) {
                        LoggerUtils.d("startReadCard: onResult");
                        mActivity.runOnUiThread(() -> {
                            cardReadingPresentation.hide();
                            cancelAnimation();
                        });
                        if (retry) {
                            LoggerUtils.e("Icc. Re read card!");
                            startReadCard(pubBean, supportEntry, forcePin, stepBean, packStep, pinStep);
                            return;
                        }
                        if (forceExit) {
                            LoggerUtils.e("force exit from Emv.");
                            terminateTransaction();
                            return;
                        }
                        if (!success) {
                            terminateTransaction();
                            if (TextUtils.isEmpty(pubBean.getMessage())) {
                                pubBean.setMessage(R.string.core_card_emv_fail);
                            }
                            if (TextUtils.isEmpty(pubBean.getResultCode())) {
                                pubBean.setResultCode(ResultCode.FL);
                            }
                            callback.onFail(FragmentCallback.FAIL,pubBean.getMessage());
                            return;
                        }

                        if (!mEmvHelper.dealEmvData(pubBean)) {
                            terminateTransaction();
                            callback.onFail(FragmentCallback.FAIL,pubBean.getMessage());
                            return;
                        }
                        boolean needInputPin = !hasInputPin && forcePin;
                        if (packStep == null && !needInputPin) {
                            terminateTransaction();
                            callback.onSuccess(null);
                            return;
                        }
                        boolean needSecondGac = emvResult == EmvResult.TXN_ONLINE && pubBean.getEntryMode() == EntryMode.INSERT;
                        if (!needSecondGac) {
                            //End of EMV, power off
                            terminateTransaction();
                        }
                        Chain<StepBean> chain = new Chain<>(stepBean);
                        if (needInputPin) {
                            chain.next(pinStep);
                        }
                        if (emvResult == EmvResult.TXN_OK || emvResult == EmvResult.TXN_ONLINE) {
                            chain.next(packStep);
                        } else {
                            LoggerUtils.d("Emv offline");
                            chain.next(new EmvOfflineStep());
                        }
                        chain.proceed(stepSucc -> {
                            if (!needSecondGac) {
                                //Not require Secondary Authorization
                                if (stepSucc) {
                                    callback.onSuccess(null);
                                } else {
                                    callback.onFail(FragmentCallback.FAIL,pubBean.getMessage());
                                }
                                return;
                            }
                            //start Secondary Authorization
                            byte[] gac = mEmvHelper.packGac(stepSucc, pubBean.getResultCode(), pubBean.getField55());
                            mEmvHelper.secondGac(pubBean.isRequestOnlineSucc(), gac, new EmvSecondGacListener() {
                                @Override
                                public void completeResult(boolean result) {
                                    //finish
                                    terminateTransaction();
                                    if (stepSucc && !result) {
                                        //host success, but second gac failed
                                        pubBean.setResultCode(ResultCode.FL);
                                        pubBean.setMessage(R.string.core_card_second_gac_failed);
                                    }
                                    if (result) {
                                        //update emv print data
                                        pubBean.setEmvPrintData(mEmvHelper.packEmvPrintData());
                                        callback.onSuccess(null);
                                    } else {
                                        callback.onFail(FragmentCallback.FAIL,pubBean.getMessage());
                                    }
                                }

                                @Override
                                public void recard() {
                                    mActivity.runOnUiThread(() -> {
                                        presentationBinding.tvError.setText(null);
                                        cardReadingPresentation.show();
                                    });
                                }
                            });

                        });
                    }
                });
            });
        }

        private boolean cancelAnimation;

        private void cancelAnimation() {
            cancelAnimation = true;
            presentationBinding.lottieCard.cancelAnimation();
        }


        /**
         * show the entry animations
         */
        private void showEntryAnimation(int entryMode) {
            boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD);
            if (external) {
                List<Integer> animations = new ArrayList<>();
                if ((entryMode & EntryMode.MAG) != 0) {
                    animations.add(R.raw.lottie_ext_card_mag);
                }
                if ((entryMode & EntryMode.INSERT) != 0) {
                    animations.add(R.raw.lottie_ext_card_insert);
                }
                if ((entryMode & EntryMode.TAP) != 0) {
                    animations.add(R.raw.lottie_ext_card_tap);
                }
                if (animations.size() == 0) {
                    return;
                }
                cancelAnimation = false;
                presentationBinding.lottieCard.setRepeatCount(0);
                presentationBinding.lottieCard.removeAllAnimatorListeners();
                presentationBinding.lottieCard.setAnimation(animations.get(0));
                presentationBinding.tvEntryMode.setText(R.string.core_card_on_external_pinpad);
                presentationBinding.lottieCard.playAnimation();
                presentationBinding.lottieCard.addAnimatorListener(new Animator.AnimatorListener() {
                    int index = 0;

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (cancelAnimation) {
                            return;
                        }
                        index++;
                        if (index >= animations.size()) {
                            index = 0;
                        }
                        presentationBinding.lottieCard.setAnimation(animations.get(index));
                        presentationBinding.lottieCard.playAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                return;
            }
            entryMode &= EntryMode.TAP | EntryMode.INSERT | EntryMode.MAG;
            switch (entryMode) {
                case EntryMode.TAP | EntryMode.INSERT | EntryMode.MAG:
                    presentationBinding.tvEntryMode.setText(String.format(Locale.getDefault(), "%s/%s/%s", getString(R.string.core_card_tap), getString(R.string.core_card_insert), getString(R.string.core_card_mag)));
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_tap_insert_mag);
                    break;
                case EntryMode.TAP | EntryMode.INSERT:
                    presentationBinding.tvEntryMode.setText(String.format(Locale.getDefault(), "%s/%s", getString(R.string.core_card_tap), getString(R.string.core_card_insert)));
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_tap_insert);
                    break;
                case EntryMode.TAP | EntryMode.MAG:
                    presentationBinding.tvEntryMode.setText(String.format(Locale.getDefault(), "%s/%s", getString(R.string.core_card_tap), getString(R.string.core_card_mag)));
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_tap_mag);
                    break;
                case EntryMode.INSERT | EntryMode.MAG:
                    presentationBinding.tvEntryMode.setText(String.format(Locale.getDefault(), "%s/%s", getString(R.string.core_card_insert), getString(R.string.core_card_mag)));
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_insert_mag);
                    break;
                case EntryMode.TAP:
                    presentationBinding.tvEntryMode.setText(R.string.core_card_tap);
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_tap);
                    break;
                case EntryMode.INSERT:
                    presentationBinding.tvEntryMode.setText(R.string.core_card_insert);
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_insert);
                    break;
                case EntryMode.MAG:
                    presentationBinding.tvEntryMode.setText(R.string.core_card_mag);
                    presentationBinding.lottieCard.setAnimation(R.raw.lottie_display2_card_mag);
                    break;
                default:
                    break;
            }
            presentationBinding.lottieCard.playAnimation();
        }
    }
}
