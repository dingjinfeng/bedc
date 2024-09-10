package acquire.sdk.pin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.crypto.CipherOutput;
import com.newland.nsdk.core.api.common.crypto.CipherType;
import com.newland.nsdk.core.api.common.crypto.KCVMode;
import com.newland.nsdk.core.api.common.crypto.MACOutput;
import com.newland.nsdk.core.api.common.crypto.MACType;
import com.newland.nsdk.core.api.common.crypto.PaddingMode;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.common.keymanager.DUKPTKey;
import com.newland.nsdk.core.api.common.keymanager.Key;
import com.newland.nsdk.core.api.common.keymanager.KeyGenerateMethod;
import com.newland.nsdk.core.api.common.keymanager.KeyInfoID;
import com.newland.nsdk.core.api.common.keymanager.KeyType;
import com.newland.nsdk.core.api.common.keymanager.KeyUsage;
import com.newland.nsdk.core.api.common.keymanager.SymmetricKey;
import com.newland.nsdk.core.api.common.pinentry.PINBlockMode;
import com.newland.nsdk.core.api.internal.crypto.Crypto;
import com.newland.nsdk.core.api.internal.emvl2.type.publickey;
import com.newland.nsdk.core.api.internal.keymanager.KeyManager;
import com.newland.nsdk.core.api.internal.pinentry.PINEntry;
import com.newland.nsdk.core.api.internal.pinentry.PINEntryListener;
import com.newland.nsdk.core.api.internal.pinentry.PINEntryParameters;
import com.newland.nsdk.core.api.internal.pinentry.RSAKey;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import java.util.Arrays;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.sdk.R;
import acquire.sdk.emv.EmvProvider;
import acquire.sdk.pin.constant.KeyAlgorithmType;
import acquire.sdk.pin.constant.MacMode;
import acquire.sdk.pin.constant.WorkKeyType;
import acquire.sdk.pin.listener.PinpadListener;

/**
 * Built-in PIN Pad.
 * <p><hr><b>Feature</b></p>
 * <pre>
 *     1.Load/Clear key (Development firmware or installed checkSum device);
 *     2.Enter card PIN;
 *     3.Calculate MAC data;
 *     4.Encrypt data by DES;
 *     5.Get/Increase ksn;
 * </pre>
 *
 * @author Janson
 * @date 2021/11/4 9:02
 */
public class BPinpad {

    private final KeyManager mKeyManager;
    private final PINEntry mPinEntry;
    private final Crypto mCyrpto;

    public BPinpad() {
        mKeyManager = (KeyManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.KEY_MANAGER);
        mPinEntry = (PINEntry) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.PIN_ENTRY);
        mCyrpto = (Crypto) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.CRYPTO);
    }


    /**
     * Set layout Of PIN pad
     *
     * @param numBtn         10 number button coordinates
     * @param funcBtn        3 function button coordinates
     * @param isRandomLayout true if the key layout is placed randomly.
     * @return 10 number values that match the 10 number button. This can be used to display keyboard.
     */
    public byte[] setPinpadLayout(byte[] numBtn, byte[] funcBtn, boolean isRandomLayout) {
        try {
            LoggerUtils.d("[NSDK Pinpad]--init key layout.");
            return mPinEntry.initKeyLayout(numBtn, funcBtn, isRandomLayout);
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--init key layout failed.", e);
            return null;
        }
    }

    /**
     * Input PIN
     *
     * @param keyAlgorithm   {@link KeyAlgorithmType}
     * @param isOnline       Set the PIN type. True to be online PIN, false to be offline PIN.
     * @param pinKeyIndex    Set the PIN key index used for starting PIN pad
     * @param pan            Set the card number used for encryption.
     * @param pinLens        Set the PIN length that is allowed to be input.
     * @param timeoutSec     Set starting PIN pad timeout in second units.
     * @param pinpadListener starting PIN pad listener.
     */
    public void startPinInput(int keyAlgorithm, boolean isOnline, int pinKeyIndex, @NonNull String pan, @NonNull byte[] pinLens, int timeoutSec, final PinpadListener pinpadListener) {
        Key key;
        if (keyAlgorithm == KeyAlgorithmType.MKSK) {
            SymmetricKey symmetricKey = new SymmetricKey();
            symmetricKey.setKeyUsage(KeyUsage.PIN);
            symmetricKey.setKeyID((byte) pinKeyIndex);
            symmetricKey.setKeyType(KeyType.DES);
            key = symmetricKey;
            LoggerUtils.d("[NSDK Pinpad]--start to input " + (isOnline ? "online" : "offline") + " MKSK pin(pin key index = " + pinKeyIndex + ").");
        } else {
            DUKPTKey dukptKey = new DUKPTKey();
            dukptKey.setKeyID((byte) pinKeyIndex);
            dukptKey.setKeyUsage(KeyUsage.DUKPT);
            dukptKey.setKeyType(KeyType.DES);
            key = dukptKey;
            LoggerUtils.d("[NSDK Pinpad]--start to input " + (isOnline ? "online" : "offline") + " DUKPT pin(pin key index = " + pinKeyIndex + ").");
        }

        final PINEntryParameters params = new PINEntryParameters();
        params.setPINBlockMode(PINBlockMode.ISO9564_0);
        params.setPINLengthRange(pinLens);
        try {
            if (isOnline) {
                mPinEntry.startOnlinePINEntry(key, pan, timeoutSec, params, new PinInputListenerIml(pinpadListener));
            } else {
                publickey offlinePubKey = EmvProvider.getInstance().getOffPublicKey();
                if (offlinePubKey == null) {
                    mPinEntry.startOfflinePINEntry(null, timeoutSec, params, new PinInputListenerIml(pinpadListener));
                } else {
                    RSAKey rsaKey = new RSAKey();
                    rsaKey.setExponent(offlinePubKey.pk_exponent);
                    byte[] modulus = Arrays.copyOf(offlinePubKey.pk_modulus, offlinePubKey.pk_mod_len);
                    rsaKey.setModulus(modulus);
                    LoggerUtils.d("modLen:" + offlinePubKey.pk_mod_len);
                    LoggerUtils.d("exponent:" + BytesUtils.bcdToString(offlinePubKey.pk_exponent));
                    LoggerUtils.d("modulus:" + BytesUtils.bcdToString(offlinePubKey.pk_modulus));
                    LoggerUtils.d("in modulus:" + BytesUtils.bcdToString(modulus));
                    mPinEntry.startOfflinePINEntry(rsaKey, timeoutSec, params, new PinInputListenerIml(pinpadListener));
                }
            }
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--start PIN failed.", e);
            pinpadListener.onError(e.getCode(), e.getMessage());
        }
    }

    /**
     * Cancel PIN input
     */
    public boolean cancelPinInput() {
        try {
            LoggerUtils.d("[NSDK Pinpad]--cancel pinpad.");
            mPinEntry.cancelPINEntry();
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--cancel pinpad failed.", e);
            return false;
        }
    }

    /**
     * Encrypt data
     *
     * @param plainText    The value to be encrypted.
     * @param dataKeyIndex Data key index.
     * @return result.
     */
    public byte[] encryptData(byte[] plainText, int dataKeyIndex, int algorithmType) {
        CipherType cipherType;
        SymmetricKey symmetricKey = new SymmetricKey();
        symmetricKey.setKeyID((byte) dataKeyIndex);
        symmetricKey.setKeyType(KeyType.DES);
        if (algorithmType == KeyAlgorithmType.DUKPT) {
            LoggerUtils.d("[NSDK Pinpad]--encrypt DUKPT data(data key index = " + dataKeyIndex + ").");
            symmetricKey.setKeyUsage(KeyUsage.DUKPT);
            cipherType = CipherType.DUKPT_ECB_BOTH;
        } else {
            LoggerUtils.d("[NSDK Pinpad]--encrypt MKSK data(data key index = " + dataKeyIndex + ").");
            symmetricKey.setKeyUsage(KeyUsage.DATA);
            cipherType = CipherType.DES_ECB;
        }
        try {
            CipherOutput cipherOutput = mCyrpto.encrypt(symmetricKey, cipherType, PaddingMode.NONE, null, plainText);
            byte[] output = cipherOutput.getData();
            if (output == null || output.length == 0) {
                LoggerUtils.e("[NSDK Pinpad]--encrypt data failed.");
                return null;
            } else {
                LoggerUtils.d("[NSDK Pinpad]--encrypt data success.");
                return output;
            }
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--encrypt data failed.", e);
            return null;
        }
    }

    /**
     * Get Mac
     *
     * @param macIndex      Mac key index .
     * @param plainText     The value to be encrypted.
     * @param algorithmType Algorithm type. {@link KeyAlgorithmType}
     * @param macMode       MAC mode. {@link MacMode}
     * @return Mac result.
     */
    public byte[] getMac(byte[] plainText, @KeyAlgorithmType.AlgorithmTypeDef int algorithmType, @MacMode.MacModeTypeDef int macMode, int macIndex) {
        MACType macType;
        if (algorithmType == KeyAlgorithmType.DUKPT) {
            switch (macMode) {
                case MacMode.TYPE_9606:
                    macType = MACType.DUKPT_LAST;
                    break;
                case MacMode.TYPE_X919:
                    macType = MACType.DUKPT_X919;
                    break;

                case MacMode.TYPE_ECB:
                    macType = MACType.DUKPT_UNIONPAY_ECB;
                    break;
                case MacMode.TYPE_X99:
                default:
                    macType = MACType.DUKPT_X99;
                    break;
            }
        } else {
            switch (macMode) {
                case MacMode.TYPE_9606:
                    macType = MACType.TDES_LAST;
                    break;
                case MacMode.TYPE_X919:
                    macType = MACType.TDES_X919;
                    break;
                case MacMode.TYPE_ECB:
                    macType = MACType.TDES_UNIONPAY_ECB;
                    break;
                case MacMode.TYPE_X99:
                default:
                    macType = MACType.TDES_X99;
                    break;
            }
        }
        try {
            MACOutput macOutput = mCyrpto.generateMAC((byte) macIndex, macType, null, plainText);
            byte[] mac = macOutput.getData();
            LoggerUtils.d("[NSDK Pinpad]--get mac(type = " + macType + ",mac index = " + macIndex + "): " + BytesUtils.bcdToString(mac) + ".");
            return mac;
        } catch (Exception e) {
            LoggerUtils.e("[NSDK Pinpad]--get mac(type = " + macType + ",mac index = " + macIndex + ") failed.", e);
            return null;
        }
    }

    /**
     * Increase KSN.
     *
     * @param masterIndex master key index.
     */
    public boolean increaseKsn(int masterIndex) {
        try {
            LoggerUtils.d("[NSDK Pinpad]--increase ksn(master key index = " + masterIndex + ").");
            mKeyManager.increaseKSN((byte) masterIndex);
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--increase ksn failed.", e);
            return false;
        }
    }

    /**
     * Get the work key KCV under the specified index. It can be used to determine whether the key exists
     *
     * @param index       work key index
     * @param workKeyType key type. PIN\MAC\DATA key.
     * @return kcv byte array
     */
    public byte[] getWorkKcv(byte index, WorkKeyType workKeyType) {
        SymmetricKey key = new SymmetricKey();
        key.setKeyID(index);
        key.setKeyType(KeyType.DES);
        switch (workKeyType) {
            case MAC_KEY:
                key.setKeyUsage(KeyUsage.MAC);
                break;
            case PIN_KEY:
                key.setKeyUsage(KeyUsage.PIN);
                break;
            case DATA_KEY:
                key.setKeyUsage(KeyUsage.DATA);
                break;
            default:
                LoggerUtils.e("[NSDK Pinpad]--work key type error!");
                return null;
        }
        try {
            byte[] kcv = mKeyManager.getKeyInfo(KeyInfoID.KCV, key);
            LoggerUtils.d("[NSDK Pinpad]--get work key's kcv (type = " + key.getKeyUsage() + ",index = " + index + "): " + BytesUtils.bcdToString(kcv));
            return null;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--get work key's kcv failed.", e);
            return null;
        }
    }

    /**
     * Get the master key KCV under the specified index. It can be used to determine whether the key exists
     *
     * @param index            master key index
     * @param keyAlgorithmType key type. DUKPT or MKSK.
     * @return kcv byte array
     */
    public byte[] getMasterKcv(byte index, @KeyAlgorithmType.AlgorithmTypeDef int keyAlgorithmType) {
        SymmetricKey key = new SymmetricKey();
        key.setKeyID(index);
        if (keyAlgorithmType == KeyAlgorithmType.DUKPT) {
            key.setKeyUsage(KeyUsage.DUKPT);
        } else {
            key.setKeyUsage(KeyUsage.KEK);
        }
        key.setKeyType(KeyType.DES);
        try {
            byte[] kcv = mKeyManager.getKeyInfo(KeyInfoID.KCV, key);
            LoggerUtils.d("[NSDK Pinpad]--get master key's kcv (type = " + key.getKeyUsage() + ",index = " + index + "): " + BytesUtils.bcdToString(kcv));
            return null;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--get master key's kcv failed.", e);
            return null;
        }
    }

    /**
     * Get KSN
     *
     * @param masterIndex master key index
     * @return KSN
     */
    public byte[] getKsn(int masterIndex) {
        DUKPTKey keyInfo = new DUKPTKey();
        try {
            keyInfo.setKeyID((byte) masterIndex);
            keyInfo.setKeyType(KeyType.DES);
            keyInfo.setKeyUsage(KeyUsage.DUKPT);
            byte[] ksn = mKeyManager.getKeyInfo(KeyInfoID.KSN, keyInfo);
            LoggerUtils.d("[NSDK Pinpad]--get ksn(master key index = " + masterIndex + "):" + BytesUtils.bcdToString(ksn));
            return ksn;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--get ksn failed.", e);
            return null;
        }
    }

    /**
     * load mksk master key.
     *
     * @param masterIndex index of master key.
     * @param key         the master key to be loaded.
     */
    public boolean loadMkskMasterKey(int masterIndex, byte[] key) {
        SymmetricKey destKey = new SymmetricKey();
        destKey.setKeyID((byte) masterIndex);
        destKey.setKeyType(KeyType.DES);
        destKey.setKeyUsage(KeyUsage.KEK);
        destKey.setKeyLen(16);
        destKey.setKeyData(key);
        destKey.setKCVMode(KCVMode.NONE);
        try {
            LoggerUtils.d("[NSDK Pinpad]--load MKSK master key(key index = " + masterIndex + ").");
            mKeyManager.generateKey(KeyGenerateMethod.CLEAR, null, null, destKey);
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--load MKSK master key failed.", e);
            return false;
        }
    }

    /**
     * load mksk work key.
     *
     * @param masterIndex index of master key.
     * @param workKeyType Work key type.
     * @param keyIndex    key index of work key
     * @param key         work key
     * @param kcv         check value
     */
    public boolean loadMkskWorkKey(int masterIndex, WorkKeyType workKeyType, int keyIndex, byte[] key, @Nullable byte[] kcv) {
        SymmetricKey sourceKey = new SymmetricKey();
        sourceKey.setKeyID((byte) masterIndex);
        sourceKey.setKeyType(KeyType.DES);
        sourceKey.setKeyUsage(KeyUsage.KEK);
        SymmetricKey destKey = new SymmetricKey();
        destKey.setKeyID((byte) keyIndex);
        destKey.setKeyType(KeyType.DES);
        destKey.setKeyLen(16);
        destKey.setKeyData(key);
        destKey.setKCVMode(KCVMode.ZERO);
        destKey.setKCV(kcv);
        String modeName;
        switch (workKeyType) {
            case MAC_KEY:
                destKey.setKeyUsage(KeyUsage.MAC);
                break;
            case PIN_KEY:
                destKey.setKeyUsage(KeyUsage.PIN);
                break;
            case DATA_KEY:
                destKey.setKeyUsage(KeyUsage.DATA);
                break;
            default:
                LoggerUtils.e("[NSDK Pinpad]--work key type error!");
                return false;
        }
        try {
            LoggerUtils.d("[NSDK Pinpad]--load MKSK "+destKey.getKeyUsage()+" key" +
                    "(MASTER key index = " + masterIndex + ", "+destKey.getKeyUsage()+" key index = " + keyIndex + ").");
            mKeyManager.generateKey(KeyGenerateMethod.CIPHER, null, sourceKey, destKey);
            return true;
        } catch (NSDKException e) {
            if (e.getCode() == -1309){
                LoggerUtils.e("[NSDK Pinpad]--"+destKey.getKeyUsage()+"=>> master key doesn't exist.", e);
            }else if (e.getCode() == -1014){
                LoggerUtils.e("[NSDK Pinpad]--"+destKey.getKeyUsage()+"=>> key parameters or check value error.", e);
            }else if (e.getCode() == -1702){
                LoggerUtils.e("[NSDK Pinpad]--"+destKey.getKeyUsage()+"=>> duplicate work key.", e);
            }else{
                LoggerUtils.e("[NSDK Pinpad]--"+destKey.getKeyUsage()+"=>> load work key failed.", e);
            }
            return false;
        }
    }

    /**
     * load dukpt ipek.
     *
     * @param masterIndex index of dukpt key.
     * @param ipek        Dukpt ipek
     * @param ksn         10 bytes ksn.
     */
    public boolean loadIpekKey(int masterIndex, byte[] ipek, byte[] ksn) {
        DUKPTKey destKey = new DUKPTKey();
        destKey.setKeyID((byte) masterIndex);
        destKey.setKeyType(KeyType.DES);
        destKey.setKeyUsage(KeyUsage.DUKPT);
        destKey.setKeyData(ipek);
        destKey.setKSN(ksn);
        destKey.setKeyLen(16);
        destKey.setKCVMode(KCVMode.NONE);
        try {
            LoggerUtils.d("[NSDK Pinpad]--load DUKPT ipek (key index = " + masterIndex + ").");
            mKeyManager.generateKey(KeyGenerateMethod.CLEAR, null, null, destKey);
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK Pinpad]--load DUKPT ipek failed.", e);
            return false;
        }
    }

    /**
     * Clear  key.
     *
     * @param keyAlgorithm   {@link KeyAlgorithmType}
     * @param masterKeyIndex master key index.
     * @param pikIndex       PIN key index.
     * @param makIndex       mac key index.
     * @param dataIndex      data key index.
     */
    public void clearKey(int keyAlgorithm, int masterKeyIndex, int pikIndex, int makIndex, int dataIndex) {
        SymmetricKey symmetricKey = new SymmetricKey();
        symmetricKey.setKeyType(KeyType.DES);
        if (keyAlgorithm == KeyAlgorithmType.DUKPT) {
            symmetricKey.setKeyID((byte) masterKeyIndex);
            symmetricKey.setKeyUsage(KeyUsage.DUKPT);
            try {
                LoggerUtils.d("[NSDK Pinpad]--delete IPEK key(key index = " + masterKeyIndex + ").");
                mKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                e.printStackTrace();
                LoggerUtils.e("[NSDK Pinpad]--delete IPEK key failed.", e);
            }
        } else {
            symmetricKey.setKeyID((byte) masterKeyIndex);
            symmetricKey.setKeyUsage(KeyUsage.KEK);
            try {
                LoggerUtils.d("[NSDK Pinpad]--delete MKSK master key(key index = " + masterKeyIndex + ").");
                mKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK Pinpad]--delete MKSK master key failed.", e);
            }

            symmetricKey.setKeyID((byte) pikIndex);
            symmetricKey.setKeyUsage(KeyUsage.PIN);
            try {
                LoggerUtils.d("[NSDK Pinpad]--delete MKSK PIN key(key index = " + pikIndex + ").");
                mKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK Pinpad]--delete MKSK PIN key failed.", e);
            }

            symmetricKey.setKeyID((byte) makIndex);
            symmetricKey.setKeyUsage(KeyUsage.MAC);
            try {
                LoggerUtils.d("[NSDK Pinpad]--delete MKSK mac key(key index = " + makIndex + ").");
                mKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK Pinpad]--delete MKSK mac key failed.", e);
            }

            symmetricKey.setKeyID((byte) dataIndex);
            symmetricKey.setKeyUsage(KeyUsage.DATA);
            try {
                LoggerUtils.d("[NSDK Pinpad]--delete MKSK data key(key index = " + dataIndex + ").");
                mKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK Pinpad]--delete MKSK data key failed.", e);
            }
        }
    }

    /**
     * Set key owner.
     *
     * @param name owner name.
     * @return true if set success.
     */
    boolean setKeyOwner(String name) {
        try {
            mKeyManager.setKeyOwner(name);
            return true;
        } catch (NSDKException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get key owner name.
     *
     * @return name.
     */
    String getKeyOwner() {
        try {
            return mKeyManager.getKeyOwner();
        } catch (NSDKException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class PinInputListenerIml implements PINEntryListener {
        private PinpadListener pinPadListener;
        private int inputLen = 0;

        private PinInputListenerIml(PinpadListener listener) {
            this.pinPadListener = listener;
        }

        @Override
        public void onKeyPress() {
            inputLen = inputLen + 1;
            LoggerUtils.d("[NSDK Pinpad]-- Click number key(length = " + inputLen + ").");
            if (pinPadListener != null) {
                pinPadListener.onKeyDown(inputLen);
            }
        }

        @Override
        public void onBackspace() {
            inputLen = (inputLen <= 0 ? 0 : inputLen - 1);
            LoggerUtils.d("[NSDK Pinpad]-- Click backspace key(length = " + inputLen + ").");
            if (pinPadListener != null) {
                pinPadListener.onKeyDown(inputLen);
            }
        }

        @Override
        public void onCancel() {
            LoggerUtils.e("[NSDK Pinpad]-- Cancel PIN.");
            if (pinPadListener != null) {
                pinPadListener.onCancel();
                pinPadListener = null;
            }
        }

        /**
         *
         */
        @Override
        public void onClear() {
            inputLen = 0;
            LoggerUtils.d("[NSDK Pinpad]-- Clear PIN.");
            if (pinPadListener != null) {
                pinPadListener.onKeyDown(inputLen);
            }
        }

        @Override
        public void onFinish(int pinLen, byte[] pinblock, byte[] ksn) {
            LoggerUtils.d("[NSDK Pinpad]-- Input PIN success. PIN block is " + BytesUtils.bcdToString(pinblock));
            if (pinPadListener != null) {
                pinPadListener.onPinRslt(pinblock);
                pinPadListener = null;
            }
        }

        @Override
        public void onTimeout() {
            LoggerUtils.e("[NSDK Pinpad]-- Input PIN timeout.");
            if (pinPadListener != null) {
                pinPadListener.onError(-1, NSDKModuleManagerImpl.getInstance().getContext().getString(R.string.sdk_helper_pinpad_timeout));
                pinPadListener = null;
            }
        }

        @Override
        public void onError(int errorCode, String message) {
            LoggerUtils.e("[NSDK Pinpad]-- Input PIN error, error code is " + errorCode + ", error message is " + message);
            if (pinPadListener != null) {
                pinPadListener.onError(errorCode, "Input Error " + message);
                pinPadListener = null;
            }
        }
    }
}
