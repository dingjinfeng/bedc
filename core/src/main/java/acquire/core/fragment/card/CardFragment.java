package acquire.core.fragment.card;

import android.animation.Animator;
import android.app.Dialog;
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

import acquire.base.activity.BaseFragment;
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
import acquire.base.widget.dialog.menu.MenuDialog;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.bean.PubBean;
import acquire.core.bean.StepBean;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.databinding.CoreFragmentCardBinding;
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
 * A reading card {@link Fragment}
 *
 * @author Janson
 * @date 2020/9/25 9:20
 */
public class CardFragment extends BaseFragment {
    private CoreFragmentCardBinding binding;
    private CardFragmentCallback callback;
    private CardFragmentArgs cardFragmentArgs;
    private final EmvHelper mEmvHelper = new EmvHelper();
    private boolean forceExit;
    private MessageDialog exitDialog;
    private CardReadingDialog cardReadingDialog;
    private boolean exitEnable = true;
    @NonNull
    public static CardFragment newInstance(CardFragmentArgs cardFragmentArgs, CardFragmentCallback callback) {
        CardFragment fragment = new CardFragment();
        fragment.cardFragmentArgs = cardFragmentArgs;
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentCardBinding.inflate(inflater, container, false);
        cardReadingDialog = new CardReadingDialog(mActivity);
        //manual
        binding.manual.setOnClickListener(v -> {
            LoggerUtils.e("cancel card,go to manual.");
            mEmvHelper.cancelEmv();
            forceExit = true;
            callback.onManual();
        });
        int supportEntry = cardFragmentArgs.getSupportEntry();
        StepBean stepBean = cardFragmentArgs.getStepBean();
        PubBean pubBean = stepBean.getPubBean();
        //show amount
        if (pubBean.getAmount() != 0L) {
            String amount = CurrencyCodeProvider.getCurrencySymbol(pubBean.getCurrencyCode())
                    + FormatUtils.formatAmount(pubBean.getAmount());
            binding.tvAmount.setText(amount);
        } else {
            binding.tvAmount.setVisibility(View.INVISIBLE);
            binding.tvAmountTag.setVisibility(View.INVISIBLE);
        }
        //show supported card entries
        showEntryAnimation(supportEntry);
        if ((supportEntry & EntryMode.MANUAL) != 0) {
            binding.manual.setVisibility(View.VISIBLE);
        } else {
            binding.manual.setVisibility(View.GONE);
        }
        if (binding.tvAmount.getVisibility() != View.VISIBLE && binding.manual.getVisibility() != View.VISIBLE) {
            binding.rlTop.setVisibility(View.GONE);
        }
        //read card
        startReadCard(pubBean, supportEntry, cardFragmentArgs.isForcePin(), stepBean, cardFragmentArgs.getPackStep(), cardFragmentArgs.getPinStep());
        return binding.getRoot();
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
                    .entryMode(supportEntry)
                    .amount(pubBean.getAmount())
                    .timeout(60)
                    .create();
            mEmvHelper.terminateTransaction();
            mEmvHelper.readCard(param, new EmvListener() {
                private boolean hasInputPin;
                private boolean retry;

                @Override
                public void onReady(EmvReadyBean emvReadyBean) {
                    LoggerUtils.d("startReadCard: onReady");
                    exitEnable = true;
                    mActivity.runOnUiThread(() -> {
                        if ((supportEntry & EntryMode.MANUAL) != 0) {
                            binding.manual.setEnabled(true);
                        }
                        cardReadingDialog.dismiss();
                        if (supportEntry != emvReadyBean.getSupportEntries()) {
                            showEntryAnimation(emvReadyBean.getSupportEntries());
                        }
                        if (emvReadyBean.getStatus() == EmvReadyBean.FALLBACK) {
                            if (emvReadyBean.getSupportEntries() == EntryMode.MAG){
                                binding.tvContent.setText(R.string.core_card_fallback_to_mag_prompt);
                            }else{
                                binding.tvContent.setText(R.string.core_card_fallback_tp_insert_mag_prompt);
                            }
                            ViewUtils.shakeAnimatie(binding.tvContent);
                        } else if (emvReadyBean.getStatus() == EmvReadyBean.USE_CHIP) {
                            binding.tvContent.setText(R.string.core_card_icc_prompt);
                            ViewUtils.shakeAnimatie(binding.tvContent);
                        }else if (emvReadyBean.getStatus() == EmvReadyBean.AGAIN) {
                            binding.tvContent.setText(R.string.core_card_try_again);
                            ViewUtils.shakeAnimatie(binding.tvContent);
                        }
                    });
                }

                @Override
                public void onReading() {
                    LoggerUtils.d("startReadCard: onReading");
                    exitEnable = false;
                    mActivity.runOnUiThread(() -> {
                        dismissExitDialog();
                        cardReadingDialog.show();
                        binding.manual.setEnabled(false);
                    });
                }

                @Override
                public int onSelectAid(List<String> preferNames) {
                    LoggerUtils.d("startReadCard: onSelectAid");
                    Locker<Integer> locker = new Locker<>(-1);
                    mActivity.runOnUiThread(()-> {
                        cardReadingDialog.dismiss();
                        new MenuDialog.Builder(mActivity)
                                .setItems(preferNames)
                                .setTitle(R.string.core_emv_aids_dialog_title)
                                .setConfirmButton(index -> {
                                    locker.setResult(index);
                                    locker.wakeUp();
                                })
                                .setCancelButton(dialog -> {
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
                    mActivity.runOnUiThread(() ->{
                        cardReadingDialog.dismiss();
                        dismissExitDialog();
                        new MessageDialog.Builder(mActivity)
                                .setTitle(R.string.core_card_chip_error_dialog_title)
                                .setMessage(R.string.core_card_chip_error_dialog_message)
                                .setConfirmButton(R.string.core_card_chip_error_dialog_button_ok, dialog -> {
                                    locker.setResult(true);
                                    locker.wakeUp();
                                })
                                .setCancelButton(dialog->{
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
                        cardReadingDialog.dismiss();
                        new MessageDialog.Builder(mActivity)
                                .setMessage(R.string.core_card_try_again)
                                .setConfirmButton(dialog -> {
                                    locker.setResult(true);
                                    locker.wakeUp();
                                })
                                .setCancelButton(dialog -> {
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
                    cancelAnimation();
                    mActivity.runOnUiThread(()-> cardReadingDialog.dismiss());
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
//                    if (pubBean.isThirdCall()){
//                        return true;
//                    }
                    Locker<Boolean> locker = new Locker<>(false);
                    mActivity.runOnUiThread(() ->
                            new MessageDialog.Builder(mActivity)
                                    .setMessage(FormatUtils.maskCardNo(pan))
                                    .setConfirmButton(dialog -> {
                                        locker.setResult(true);
                                        locker.wakeUp();
                                    })
                                    .setCancelButton(dialog -> {
                                        LoggerUtils.e("cancel card.");
                                        pubBean.setResultCode(ResultCode.UC);
                                        pubBean.setMessage(R.string.core_card_cancel);
                                        locker.setResult(false);
                                        locker.wakeUp();
                                    })
                                    .show()
                    );
                    locker.waiting();
                    return locker.getResult();
                }

                @Override
                public PinResult onInputPin(boolean isOnlinePin, int pinTryCount) {
                    LoggerUtils.d("startReadCard: onInputPin");
                    cancelAnimation();
                    mActivity.runOnUiThread(()-> cardReadingDialog.dismiss());
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
                    if (pinStep instanceof InputPinStep){
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
                    cancelAnimation();
                    mActivity.runOnUiThread(()-> cardReadingDialog.dismiss());
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
                        //End of EMV, power off card
                        terminateTransaction();
                    }
                    Chain<StepBean> chain = new Chain<>(stepBean);
                    if (needInputPin && pinStep!=null) {
                        chain.next(pinStep);
                    }
                    //check emvResult
                    if (emvResult == EmvResult.TXN_OK || emvResult == EmvResult.TXN_ONLINE) {
                        //Simple/Standard Process OK
                        chain.next(packStep);
                    } else {
                        //Approved
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
                                mActivity.runOnUiThread(() -> cardReadingDialog.dismiss());
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
                                    binding.tvContent.setText(null);
                                    cardReadingDialog.show();
                                });
                            }
                        });

                    });
                }
            });
        });
    }

    private Dialog removeCardDialog;

    /**
     * card power off and prompt to remove it
     */
    private void terminateTransaction(){
        mEmvHelper.terminateTransaction();
        LoggerUtils.d("check whether the card exists...");
        //TODO 测试
//        if (cardFragmentArgs.getStepBean().getPubBean().isThirdCall()){
//            return ;
//        }
        int count = 0;
        boolean isShow = false;
        int ret ;
        while ((ret = mEmvHelper.cardExist()) != 0) {
            if (!isShow) {
                int image = ret == 1?R.drawable.core_remove_insert_card_warn:R.drawable.core_remove_tap_card_warn;
                ThreadPool.postOnMain(()->
                    removeCardDialog = new ImageDialog.Builder(mActivity)
                            .setImage(image)
                            .setMessage(R.string.core_remove_card)
                            .show()
                );
                isShow = true;
            }
            try {
                if (count > 10 &&count % 8 == 0) {
                    BBeeper.beep(750, 200);
                }
                count++;
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isShow){
            ThreadPool.postOnMain(()->{
                if (removeCardDialog != null){
                    removeCardDialog.dismiss();
                    removeCardDialog = null;
                }
            });
        }
        LoggerUtils.d("check card over...");
    }
    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        PubBean pubBean = cardFragmentArgs.getStepBean().getPubBean();
        if (exitEnable) {
            //exit prompt dialog
            exitDialog = new MessageDialog.Builder(mActivity)
                    .setMessage(R.string.core_card_cancel_dialog_message)
                    .setBackEnable(true)
                    .setConfirmButton(dialog -> {
                        pubBean.setResultCode(ResultCode.UC);
                        pubBean.setMessage(R.string.core_card_cancel);
                        mEmvHelper.cancelEmv();
                    })
                    .setCancelButton(dialog -> {
                    })
                    .show();
        }
        return true;
    }

    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        dismissExitDialog();
        if (removeCardDialog != null){
            removeCardDialog.dismiss();
        }
        if (cardReadingDialog != null){
            cardReadingDialog.dismiss();
        }
        cancelAnimation();
    }

    /**
     * dismiss exit dialog
     */
    private void dismissExitDialog() {
        mActivity.runOnUiThread(() -> {
            if (exitDialog != null && exitDialog.isShowing()) {
                exitDialog.dismiss();
            }
        });
    }
    private boolean cancelAnimation;

    /**
     * close card animations
     */
    private void cancelAnimation(){
        cancelAnimation = true;
    }
    /**
     * show the entry animations
     */
    private void showEntryAnimation(int entryMode) {
        List<Integer> animations = new ArrayList<>();
        List<Integer> names = new ArrayList<>();
        boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD);
        if ((entryMode & EntryMode.MAG) != 0) {
            if (external){
                animations.add(R.raw.lottie_ext_card_mag);
            }else{
                animations.add(R.raw.lottie_card_mag);
            }
            names.add(R.string.core_card_mag);
        }
        if ((entryMode & EntryMode.INSERT) != 0) {
            if (external){
                animations.add(R.raw.lottie_ext_card_insert);
            }else{
                animations.add(R.raw.lottie_card_insert);
            }
            names.add(R.string.core_card_insert);
        }
        if ((entryMode & EntryMode.TAP) != 0) {
            if (external){
                animations.add(R.raw.lottie_ext_card_tap);
            }else{
                animations.add(R.raw.lottie_card_tap);
            }
            names.add(R.string.core_card_tap);
        }
        if (animations.size() == 0) {
            return;
        }
        cancelAnimation = false;
        binding.lottieAnimation.removeAllAnimatorListeners();
        binding.lottieAnimation.setAnimation(animations.get(0));
        binding.tvName.setText(names.get(0));
        binding.lottieAnimation.playAnimation();
        binding.lottieAnimation.addAnimatorListener(new Animator.AnimatorListener() {
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
                binding.lottieAnimation.setAnimation(animations.get(index));
                binding.lottieAnimation.playAnimation();
                binding.tvName.setText(names.get(index));
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
