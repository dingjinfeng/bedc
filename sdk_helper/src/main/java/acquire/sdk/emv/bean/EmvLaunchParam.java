package acquire.sdk.emv.bean;


import androidx.annotation.NonNull;

import com.newland.nsdk.core.api.internal.emvl2.type.EmvConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.TlvUtils;
import acquire.sdk.emv.constant.EmvTransType;
import acquire.sdk.emv.constant.EntryMode;

/**
 * emv launch parameters
 *
 * @author Janson
 * @date 2019/10/21 9:24
 */
public class EmvLaunchParam {
    /**
     * entry type.
     *
     * @see EntryMode
     */
    private int entryMode;

    private int timeoutSec;

    /**
     * money in cents
     */
    private long amount;
    /**
     * Force online
     */
    private boolean forceOnline;
    private byte[] transData;

    private EmvLaunchParam() {
    }

    /**
     * Get card type
     *
     * @return EMV card type.
     * @see EntryMode
     */
    public int getEntryMode() {
        return entryMode;
    }

    public int getTimeoutSec() {
        return timeoutSec;
    }

    public byte[] getTransData() {
        return transData;
    }


    @NonNull
    @Override
    public String toString() {
        return "EmvLaunchParam{" +
                "entryMode=" + entryMode +
                ", timeoutSec=" + timeoutSec +
                ", amount=" + amount +
                ", forceOnline=" + forceOnline +
                ", transData=" + BytesUtils.bcdToString(transData) +
                '}';
    }

    public static class Builder {
        private int entryMode;
        private long amount;
        private boolean forceOnline;
        private final int emvTransType;

        private int timeoutSec = 60;

        public Builder(int emvTransType) {
            this.emvTransType = emvTransType;
        }

        /**
         * set time out
         *
         * @return <code>this</code>
         */
        public Builder timeout(int timeoutSec) {
            this.timeoutSec = timeoutSec;
            return this;
        }

        /**
         * set card type
         *
         * @return <code>this</code>
         * @see EntryMode
         */
        public Builder entryMode(int entryMode) {
            this.entryMode = entryMode;
            return this;
        }

        /**
         * set money
         *
         * @param amount money
         * @return <code>this</code>
         */
        public Builder amount(long amount) {
            this.amount = amount;
            return this;
        }

        /**
         * set online
         *
         * @param forceOnline true online ; false offline
         * @return <code>this</code>
         */
        public Builder forceOnline(boolean forceOnline) {
            this.forceOnline = forceOnline;
            return this;
        }


        /**
         * create EMV param bean
         *
         * @return EMV param {@link EmvLaunchParam}
         */
        public EmvLaunchParam create() {
            EmvLaunchParam emvLaunchParam = new EmvLaunchParam();
            emvLaunchParam.entryMode = entryMode;
            emvLaunchParam.timeoutSec = timeoutSec;
            emvLaunchParam.amount = amount;
            emvLaunchParam.forceOnline = forceOnline;
            TlvUtils.PackTlv pack = TlvUtils.newPackTlv();
            //Amount (excluding adjustments)
            String amountStr = String.format(Locale.getDefault(), "%012d", amount);
            pack.append(EmvConfig._EMVPARAM_9F02_AUTHAMNTN, BytesUtils.hexToBytes(amountStr));
            //Transaction type
            pack.append(EmvConfig._EMVPARAM_9C_TRANSTYPE, new byte[]{(byte) emvTransType});
            if (emvTransType == EmvTransType.REFUND) {
                //Simple process
                pack.append(0x1F8128, new byte[]{1});
            }
            //Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.getDefault());
            pack.append(EmvConfig._EMVPARAM_9A_TRANSDATE, BytesUtils.hexToBytes(dateFormat.format(new Date())));
            //Time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
            pack.append(EmvConfig._EMVPARAM_9F21_TRANSTIME, BytesUtils.hexToBytes(timeFormat.format(new Date())));
            if (forceOnline) {
                //Force online request
                pack.append(0x1F8126, new byte[]{1});
            } else {
                //Card devices online/offline request
                pack.append(0x1F8126, new byte[]{0});
            }
            /*
             * Only valid on the external card reader
             * make the UI(card confirm,aid selection) show on the external card reader
             */
//            pack.append(0x1F8139, new byte[]{(byte) 0xFF, 0x00, 0x00});
            pack.append(0x1F8139, new byte[]{(byte) 0x08, 0x00, 0x00});
            pack.append(0x1F8130, new byte[]{ 0x03});
            /*
             * fallback configure
             */
            pack.append(0x1F8122, new byte[]{0x00, (byte) 0x9F, 0x00});
            //UnionPay QPS limit amount(free pin/sign)
//            pack.append(0x1F8124, new byte[]{ 0x00, 0x00, 0x00,0x10, 0x00, 0x00});
            //Retry Times before Fallback
//        pack.append(0x1F8127, new byte[]{1});
            emvLaunchParam.transData = pack.pack();

            return emvLaunchParam;
        }
    }
}
