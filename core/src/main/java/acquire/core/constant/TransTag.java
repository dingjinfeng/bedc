package acquire.core.constant;

import acquire.sdk.emv.constant.EntryMode;

/**
 * Transaction parameter tag
 *
 * @author Janson
 * @date 2018/3/26
 */
public class TransTag {
    /**
     * Transaction type({@link String} value).
     * @see TransType
     */
    public final static String TRANS_TYPE = "transType";
    /**
     * Transaction amount (long value in cents)
     */
    public final static String AMOUNT = "amount";
    /**
     * Tip amount (long value in cents)
     */
    public final static String TIP = "tip";
    /**
     * Bill amount.If there is a tip, it indicates the amount before the tip is added.(long value in cents)
     */
    public final static String BILL_AMOUNT = "billAmount";
    /**
     * Entry mode (int value)
     * @see EntryMode
     */
    public final static String ENTRY_MODE = "entryMode";
    /**
     * Out order number ({@link String} value).
     */
    public final static String OUT_ORDER_NO = "outOrderNo";
    /**
     * Receipt remark ({@link String} value).
     */
    public final static String REMARK = "remark";
    /**
     * Pay QR code ({@link String} value).
     */
    public final static String PAY_CODE = "payCode";
    /**
     * Result code ({@link String} value).
     */
    public final static String RESULT_CODE = "resultCode";
    /**
     * Response message ({@link String} value).
     */
    public final static String MESSAGE = "message";
    /**
     * Mid ({@link String} value).
     */
    public final static String MID = "mid";
    /**
     * Tid ({@link String} value).
     */
    public final static String TID = "tid";
    /**
     * Merchant name ({@link String} value).
     */
    public final static String MERCHANT_NAME = "merchantName";
    /**
     * Card num ({@link String} value).
     */
    public final static String CARD_NO = "cardNo";
    /**
     * Trace ({@link String} value).
     */
    public final static String TRACE_NO = "traceNo";
    /**
     * Batch ({@link String} value).
     */
    public final static String BATCH_NO = "batchNo";
    /**
     * Reference number ({@link String} value).
     */
    public final static String REFERENCE_NO = "referenceNo";
    /**
     * Auth code ({@link String} value).
     */
    public final static String AUTH_CODE = "authCode";
    /**
     * Organization ({@link String} value).
     * @see CardOrg
     */
    public final static String ORGANIZATION = "organization";

    /**
     * Balance (long value in cents)
     */
    public final static String BALANCE = "balance";
    /**
     * Original trace ({@link String} value).
     */
    public final static String ORIG_TRACE_NO = "origTraceNo";

    /**
     * Original auth code ({@link String} value).
     */
    public final static String ORIG_AUTH_CODE = "origAuthCode";

    /**
     * Original ref ({@link String} value).
     */
    public final static String ORIG_REFERENCE_NO = "origReferenceNo";

    /**
     * Original out order no ({@link String} value).
     */
    public final static String ORIG_OUT_ORDER_NO = "origOutOrderNo";

    /**
     * Pay date. yyyyMMdd ({@link String} value).
     */
    public final static String DATE = "date";
    /**
     * Pay time.HHmmss ({@link String} value).
     */
    public final static String TIME = "time";

    /**
     * Original date. yyyyMMdd ({@link String} value).
     */
    public final static String ORIG_DATE = "origDate";
    /**
     * Business order ({@link String} value).
     */
    public final static String BIZ_ORDER_NO = "bizOrderNo";
    /**
     * Original business order ({@link String} value).
     */
    public final static String ORIG_BIZ_ORDER_NO = "origBizOrderNo";
    /**
     * Currency code ({@link String} value).
     */
    public final static String CURRENCY_CODE = "currencyCode";
    /**
     * Installment term (long value)
     */
    public final static String INSTALLMENT_TERM  = "term";
    /**
     * settle all merchant (boolean value)
     */
    public final static String SETTLE_ALL = "settleAll";
}
