package acquire.sdk;

import android.content.Context;
import android.graphics.Bitmap;

import com.newland.toms.client.mss.api.TOMSClientMssApiManager;
import com.newland.toms.client.mss.api.inter.IMSSListener;
import com.newland.toms.client.mss.api.model.request.SettleRequestBean;
import com.newland.toms.client.mss.api.model.request.VouchUpRequestBean;

import acquire.base.utils.FormatUtils;

/**
 * A tool for Fly Receipt Service.
 *
 * @author Janson
 * @date 2023/2/1 16:08
 * @since 3.7
 */
public class FlyReceiptHelper {
    private static volatile FlyReceiptHelper instance;

    private FlyReceiptHelper() {
    }

    public static FlyReceiptHelper getInstance() {
        if (instance == null) {
            synchronized (FlyReceiptHelper.class) {
                if (instance == null) {
                    instance = new FlyReceiptHelper();
                }
            }
        }
        return instance;
    }

    /**
     * send the receipt to TOMS Platform
     */
    public void sendReceipt(Context context,ReceiptBean receiptBean, FlyReceiptCallback callback) {
        VouchUpRequestBean requestBean = new VouchUpRequestBean.Builder()
                .addMerchantName(receiptBean.merchantName)
                .addMerchantNo(receiptBean.mid)
                .addTerminalNo(receiptBean.tid)
                .addTransName(receiptBean.transName)
                .addTransCode(receiptBean.transCode)
                .addAmount(FormatUtils.formatAmount(receiptBean.amount,2,""))
                .addVoucherType("R")
                .addImageBase64(receiptBean.receipt)
                .build();
        TOMSClientMssApiManager.getInstance().vouchUp(context, requestBean, new IMSSListener() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailed(int code, String msg) {
                callback.onFailed(code, msg);
            }
        });
    }

    public static class ReceiptBean {
        private String merchantName;
        private String mid;
        private String tid;
        private String transName;
        private String transCode;
        private long amount;

        private Bitmap receipt;

        private Bitmap signature;

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public void setMid(String mid) {
            this.mid = mid;
        }


        public void setTid(String tid) {
            this.tid = tid;
        }

        public void setTransName(String transName) {
            this.transName = transName;
        }

        public void setTransCode(String transCode) {
            this.transCode = transCode;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public void setReceipt(Bitmap receipt) {
            this.receipt = receipt;
        }

        public void setSignature(Bitmap signature) {
            this.signature = signature;
        }
    }

    /**
     * send the settle ticket to TOMS Platform
     */
    public void sendSettle(Context context,SettleTicketBean settleTicketBean, FlyReceiptCallback callback) {
        SettleRequestBean requestBean = new SettleRequestBean.Builder()
                .addMerchantName(settleTicketBean.merchantName)
                .addMerchantNo(settleTicketBean.mid)
                .addTerminalNo(settleTicketBean.tid)
                .addTransName(settleTicketBean.transName)
                .addTransCode(settleTicketBean.transCode)
                .addVoucherType("R")
                .addDebitTotal(settleTicketBean.debitNumber+"")
                .addDebitAmount(FormatUtils.formatAmount(settleTicketBean.debitAmount,2,""))
                .addCreditTotal(settleTicketBean.creditNumber+"")
                .addCreditAmount(FormatUtils.formatAmount(settleTicketBean.creditAmount,2,""))
                .build();
        TOMSClientMssApiManager.getInstance().settle(context, requestBean, new IMSSListener() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailed(int code, String msg) {
                callback.onFailed(code, msg);
            }
        });
    }
    public static class SettleTicketBean {
        private String merchantName;
        private String mid;
        private String tid;
        private String transName;
        private String transCode;
        private long debitNumber;
        private long debitAmount;
        private long creditNumber;
        private long creditAmount;

        private Bitmap receipt;

        private Bitmap signature;

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public void setMid(String mid) {
            this.mid = mid;
        }


        public void setTid(String tid) {
            this.tid = tid;
        }

        public void setTransName(String transName) {
            this.transName = transName;
        }

        public void setTransCode(String transCode) {
            this.transCode = transCode;
        }

        public void setDebitNumber(long debitNumber) {
            this.debitNumber = debitNumber;
        }

        public void setDebitAmount(long debitAmount) {
            this.debitAmount = debitAmount;
        }

        public void setCreditNumber(long creditNumber) {
            this.creditNumber = creditNumber;
        }

        public void setCreditAmount(long creditAmount) {
            this.creditAmount = creditAmount;
        }

        public void setReceipt(Bitmap receipt) {
            this.receipt = receipt;
        }

        public void setSignature(Bitmap signature) {
            this.signature = signature;
        }
    }

    public interface FlyReceiptCallback {
        void onSuccess(String result);

        void onFailed(int errorCode, String message);
    }

}
