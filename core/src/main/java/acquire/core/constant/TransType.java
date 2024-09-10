package acquire.core.constant;


/**
 * Transaction type
 *
 * @author Janson
 * @date 2020/9/1 14:22
 */
public class TransType {
    /**
     * Sale
     */
    public final static String TRANS_SALE = "Sale";
    /**
     * Void Sale
     */
    public final static String TRANS_VOID_SALE = "VoidSale";
    /**
     * Refund
     */
    public final static String TRANS_REFUND = "Refund";
    /**
     * Query balance
     */
    public final static String TRANS_BALANCE = "Balance";

    /**
     * Pre-Auth
     */
    public final static String TRANS_PRE_AUTH = "PreAuth";
    /**
     * Void Pre-Auth
     */
    public final static String TRANS_VOID_PRE_AUTH = "VoidPreAuth";
    /**
     * Auth Complete
     */
    public final static String TRANS_AUTH_COMPLETE = "AuthComplete";
    /**
     * Void Auth Complete
     */
    public final static String TRANS_VOID_AUTH_COMPLETE = "VoidAuthComplete";
    /**
     * Reversal
     */
    public final static String TRANS_REVERSAL = "Reversal";
    /**
     * Installment Sale
     */
    public final static String TRANS_INSTALLMENT = "Installment";

    /**
     * Void Installment Sale
     */
    public final static String TRANS_VOID_INSTALLMENT = "VoidInstallment";

    /**
     * POS scan QR code for payment.
     */
    public final static String TRANS_SCAN_PAY = "ScanPay";

    /**
     * POS shows the payment QR code.
     */
    public final static String TRANS_QR_CODE = "QrCode";

    /**
     * QR refund.
     */
    public final static String TRANS_QR_REFUND = "QrRefund";
    /**
     * Settle
     */
    public final static String TRANS_SETTLE = "Settle";

    /**
     * REPRINT Last Receipt
     */
    public final static String TRANS_REPRINT_LAST_RECEIPT = "ReprintLastReceipt";
    /**
     * REPRINT Receipt
     */
    public final static String TRANS_REPRINT_RECEIPT = "ReprintReceipt";
    /**
     * REPRINT settle data
     */
    public final static String TRANS_REPRINT_SETTLE = "ReprintSettle";
    /**
     * Print detail
     */
    public final static String TRANS_PRINT_DETAIL = "PrintDetail";
    /**
     * Settings
     */
    public final static String TRANS_SETTINGS = "Settings";

    /**
     * Version information
     */
    public final static String TRANS_ABOUT = "About";
    /**
     * Login
     */
    public final static String TRANS_LOGIN = "Login";
}
