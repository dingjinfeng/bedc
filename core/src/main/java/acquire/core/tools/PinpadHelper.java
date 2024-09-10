package acquire.core.tools;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.constant.ParamsConst;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.pin.BExternalPinpad;
import acquire.sdk.pin.BPinpad;
import acquire.sdk.pin.constant.KeyAlgorithmType;
import acquire.sdk.pin.constant.MacMode;
import acquire.sdk.pin.constant.WorkKeyType;
import acquire.sdk.pin.listener.PinpadListener;

/**
 * PIN pad helper utils
 *
 * @author Janson
 * @date 2021/11/26 9:34
 */
public class PinpadHelper {
    /**
     * Index of Master key
     */
    private final int mMasterIndex;

    /**
     * Key algorithm
     */
    private final int mAlgorithmType;
    /**
     * External PIN pad flag
     */
    private final boolean mIsExternal;

    private BPinpad pinpad;

    private BExternalPinpad externalPinpad;


    public PinpadHelper() {
        mIsExternal = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD)
                && ExtServiceHelper.getInstance().isInit();
        if (mIsExternal) {
            LoggerUtils.d("external PIN pad");
            externalPinpad = new BExternalPinpad();
        } else {
            LoggerUtils.d("built-in PIN pad");
            pinpad = new BPinpad();
        }
        mMasterIndex = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_MASTER_KEY_INDEX);
        LoggerUtils.d("current master key index " + mMasterIndex);
        mAlgorithmType = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_ALGORITHM_TYPE, KeyAlgorithmType.DUKPT);
    }

    /***
     * Set layout of PIN Pad,only the built-in PIN pad is valid.
     *
     * @param numBtn         10 number button coordinates
     * @param funcBtn        3 function button coordinates
     * @param isRandomLayout true if the key layout is placed randomly.
     * @return 10 number values that match the 10 number button. This can be used to display keyboard.
     */
    public byte[] setPinpadLayout(byte[] numBtn, byte[] funcBtn, boolean isRandomLayout) {
        return pinpad.setPinpadLayout(numBtn, funcBtn, isRandomLayout);
    }

    /**
     * input PIN
     *
     * @param isOnline       if ture, online PIN，otherwise offline PIN.
     * @param pan            card number
     * @param supportPinLens only PIN lengths within the range of this array are allowed. e.g. {0,4,6,12} => support no pin,4,6,12 pin bytes.
     * @param pinpadListener PIN input listener
     */
    public void startPinInput(boolean isOnline, @NonNull String pan, @NonNull byte[] supportPinLens, final PinpadListener pinpadListener) {
        if (mAlgorithmType == KeyAlgorithmType.DUKPT) {
            waitKsn();
        }
        int pinIndex = mMasterIndex;
        int timeoutSec = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_TIMEOUT, 60);
        if (mIsExternal) {
            byte maxLen = 0;
            for (byte supportPinLen : supportPinLens) {
                maxLen = (byte) Math.max(maxLen, supportPinLen);
            }
            externalPinpad.startPinInput(mAlgorithmType, isOnline, pinIndex, pan, maxLen, timeoutSec, pinpadListener);
        } else {
            pinpad.startPinInput(mAlgorithmType, isOnline, pinIndex, pan, supportPinLens, timeoutSec, pinpadListener);
        }
    }

    /**
     * cancel PIN input
     */
    public boolean cancelPinInput() {
        if (mIsExternal) {
            return externalPinpad.cancelPinInput();
        } else {
            return pinpad.cancelPinInput();
        }
    }

    /**
     * load master key.
     *
     * @param key the master key to be loaded.
     */
    public boolean loadMkskMasterKey(byte[] key) {
        if (mIsExternal) {
            return externalPinpad.loadMkskMasterKey(mMasterIndex, key);
        } else {
            return pinpad.loadMkskMasterKey(mMasterIndex, key);
        }
    }

    /**
     * load mksk work key.
     *
     * @param workKeyType Work key type.
     * @param key         work key (cipher data)
     * @param kcv         check value
     */
    public boolean loadMkskWorkKey(WorkKeyType workKeyType, byte[] key, @Nullable byte[] kcv) {
        int workIndex = mMasterIndex;
        if (mIsExternal) {
            return externalPinpad.loadMkskWorkKey(mMasterIndex, workKeyType, workIndex, key, kcv);
        } else {
            return pinpad.loadMkskWorkKey(mMasterIndex, workKeyType, workIndex, key, kcv);
        }
    }

    /**
     * encrypt data by data key
     *
     * @param plainData plain text.
     * @return cipher text
     */
    public byte[] encryptData(byte[] plainData) {
        if (mAlgorithmType == KeyAlgorithmType.DUKPT) {
            waitKsn();
        }
        if (plainData.length == 0) {
            return null;
        }
        int dataIndex = mMasterIndex;
        byte[] cipherData;
        if (mIsExternal) {
            cipherData = externalPinpad.encryptData(plainData, dataIndex, mAlgorithmType);
        } else {
            cipherData = pinpad.encryptData(plainData, dataIndex, mAlgorithmType);
        }
        return cipherData;
    }

    /**
     * calculate mac
     *
     * @param src source data
     * @return hexadecimal mac value
     */
    public String getMac(String src) {
        if (mAlgorithmType == KeyAlgorithmType.DUKPT) {
            waitKsn();
        }
        int macIndex = mMasterIndex;
        byte[] srcBytes = BytesUtils.hexToBytes(src);
        if (srcBytes == null || srcBytes.length == 0) {
            return null;
        }
        byte[] mac;
        if (mIsExternal) {
            mac = externalPinpad.getMac(srcBytes, mAlgorithmType, MacMode.TYPE_ECB, macIndex);
        } else {
            mac = pinpad.getMac(srcBytes, mAlgorithmType, MacMode.TYPE_ECB, macIndex);
        }
        String result = BytesUtils.bcdToString(mac);
        if (!TextUtils.isEmpty(result) && result.length() > 16) {
            result = result.substring(0, 16);
        }
        return result;
    }

    /**
     * increase ksn
     *
     * @return true -succ, false -failed
     */
    private boolean increaseKsn() {
        if (mIsExternal) {
            return externalPinpad.increaseKsn((byte) mMasterIndex);
        } else {
            return pinpad.increaseKsn((byte) mMasterIndex);
        }
    }

    /**
     * increase ksn in a thread
     */
    public void asyncIncreaseKsn() {
        ThreadPool.execute(() -> {
            ksnFlag = true;
            increaseKsn();
            ksnFlag = false;
        });
    }

    public static boolean ksnFlag;

    /**
     * wait the {@link #asyncIncreaseKsn} result.
     */
    public static void waitKsn() {
        if (!ksnFlag) {
            return;
        }
        long start = System.currentTimeMillis();
        while (ksnFlag) {
            if (System.currentTimeMillis() - start > 1000) {
                ksnFlag = false;
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get ksn.
     *
     * @return ksn string.
     */
    public String getKsn() {
        byte[] ksn;
        if (mIsExternal) {
            ksn = externalPinpad.getKsn(mMasterIndex);
        } else {
            ksn = pinpad.getKsn(mMasterIndex);
        }
        return BytesUtils.bcdToString(ksn);
    }
}
