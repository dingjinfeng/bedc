package acquire.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * Transaction record entity.
 *
 * @author Janson
 * @date 2021/1/5 17:18
 */
@Entity(tableName = "T_RECORD")
public class Record {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    /**
     * Merchant id
     */
    @ColumnInfo(name = "MID")
    private String mid;
    /**
     * Terminal id
     */
    @ColumnInfo(name = "TID")
    private String tid;

    /**
     * Transaction type
     */
    @ColumnInfo(name = "TRANS_TYPE")
    private String transType;

    /**
     * Process code
     */
    @ColumnInfo(name = "PROCESS_CODE")
    private String processCode;

    /**
     * Status
     */
    @ColumnInfo(name = "STATUS")
    private int status;

    /**
     * Card number
     */
    @ColumnInfo(name = "CARD_NO")
    private String cardNo;

    /**
     * Entry mode
     */
    @ColumnInfo(name = "ENTRY_MODE")
    private int entryMode;

    /**
     * Amount
     */
    @ColumnInfo(name = "AMOUNT")
    private long amount;

    /**
     * Tips Amount.(12)
     */
    @ColumnInfo(name = "TIP_AMOUNT")
    private long tipAmount;
    /**
     * Bill amount.If there is a tip, it indicates the amount before the tip is added.
     */
    @ColumnInfo(name = "BILL_AMOUNT")
    private long billAmount;

    /**
     * Trace No
     */
    @ColumnInfo(name = "TRACE_NO")
    private String traceNo;


    /**
     * Transaction time
     * <P>HHmmss</P>
     */
    @ColumnInfo(name = "TIME")
    private String time;


    /**
     * Transaction date
     * <P>yyyyMMdd</P>
     */
    @ColumnInfo(name = "DATE")
    private String date;

    /**
     * expiry  date
     * <P>yyMM</P>
     */
    @ColumnInfo(name = "EXP_DATE")
    private String expDate;

    /**
     * field 22
     */
    @ColumnInfo(name = "FIELD_22")
    private String field22;

    /**
     * Card Sequence No.
     */
    @ColumnInfo(name = "CARD_SERIAL_NO")
    private String cardSerialNo;

    /**
     * Track2 infomation
     */
    @ColumnInfo(name = "TRACK2")
    private String track2;

    /**
     * Track3 infomation
     */
    @ColumnInfo(name = "TRACK3")
    private String track3;

    /**
     * Reference No
     */
    @ColumnInfo(name = "REFER_NO")
    private String referNo;

    /**
     * Auth code
     */
    @ColumnInfo(name = "AUTH_CODE")
    private String authCode;

    /**
     * Response code
     */
    @ColumnInfo(name = "RESPONSE_CODE")
    private String responseCode;

    /**
     * Batch num
     */
    @ColumnInfo(name = "BATCH_NO")
    private String batchNo;

    /**
     * Original batch num
     */
    @ColumnInfo(name = "ORIGINAL_BATCH")
    private String origBatch;

    /**
     * Original trace num
     */
    @ColumnInfo(name = "ORIGINAL_TRACE_NO")
    private String origTraceNo;

    /**
     * Original auth code
     */
    @ColumnInfo(name = "ORIGINAL_AUTH_CODE")
    private String origAuthCode;

    /**
     * Original Reference No
     */
    @ColumnInfo(name = "ORIGINAL_REFER_NO")
    private String origReferNo;

    /**
     * Card organization
     */
    @ColumnInfo(name = "CARD_ORGANIZATION")
    private String cardOrg;

    /**
     * Batch up flag.
     */
    @ColumnInfo(name = "BATCH_UP_FLAG")
    private boolean batchUpFlag;

    /**
     * Original  date,yyyyMMdd
     */
    @ColumnInfo(name = "ORIGINAL_DATE")
    private String origDate;

    /**
     * Currency code
     */
    @ColumnInfo(name = "CURRENCY_CODE")
    private String currencyCode;

    /**
     * Qr pay code of scanning payment
     */
    @ColumnInfo(name = "QR_PAY_CODE")
    private String qrPayCode;
    /**
     * Business order number
     */
    @ColumnInfo(name = "BIZ_ORDER_NO")
    private String bizOrderNo;
    /**
     * field55
     */
    @ColumnInfo(name = "FIELD_55")
    private String field55;

    /**
     * Sign bitmap path
     */
    @ColumnInfo(name = "SIGN_PATH")
    private String signPath;
    /**
     * free sign
     */
    @ColumnInfo(name = "FREE_SIGN")
    private boolean freeSign;
    /**
     * free PIN
     */
    @ColumnInfo(name = "FREE_PIN")
    private boolean freePin;

    /**
     * Out order num
     */
    @ColumnInfo(name = "OUT_ORDER_NO")
    private String outOrderNo;

    /**
     * Card serial（0x5F34）
     */
    @ColumnInfo(name = "CARD_SN")
    private String cardSn;

    /**
     * EMV data
     */
    @ColumnInfo(name = "EMV_PRINT_DATA")
    private String emvPrintData;
    /**
     * Print remarks
     */
    @ColumnInfo(name = "REMARKS")
    private String remarks;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getTraceNo() {
        return traceNo;
    }

    public void setTraceNo(String traceNo) {
        this.traceNo = traceNo;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getField22() {
        return field22;
    }

    public void setField22(String field22) {
        this.field22 = field22;
    }

    public String getCardSerialNo() {
        return cardSerialNo;
    }

    public void setCardSerialNo(String cardSerialNo) {
        this.cardSerialNo = cardSerialNo;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getTrack3() {
        return track3;
    }

    public void setTrack3(String track3) {
        this.track3 = track3;
    }

    public String getReferNo() {
        return referNo;
    }

    public void setReferNo(String referNo) {
        this.referNo = referNo;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getOrigBatch() {
        return origBatch;
    }

    public void setOrigBatch(String origBatch) {
        this.origBatch = origBatch;
    }

    public String getOrigTraceNo() {
        return origTraceNo;
    }

    public void setOrigTraceNo(String origTraceNo) {
        this.origTraceNo = origTraceNo;
    }

    public String getOrigAuthCode() {
        return origAuthCode;
    }

    public void setOrigAuthCode(String origAuthCode) {
        this.origAuthCode = origAuthCode;
    }

    public String getOrigReferNo() {
        return origReferNo;
    }

    public void setOrigReferNo(String origReferNo) {
        this.origReferNo = origReferNo;
    }

    public String getCardOrg() {
        return cardOrg;
    }

    public void setCardOrg(String cardOrg) {
        this.cardOrg = cardOrg;
    }

    public boolean isBatchUpFlag() {
        return batchUpFlag;
    }

    public void setBatchUpFlag(boolean batchUpFlag) {
        this.batchUpFlag = batchUpFlag;
    }

    public String getOrigDate() {
        return origDate;
    }

    public void setOrigDate(String origDate) {
        this.origDate = origDate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getField55() {
        return field55;
    }

    public void setField55(String field55) {
        this.field55 = field55;
    }

    public String getOutOrderNo() {
        return outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getQrPayCode() {
        return qrPayCode;
    }

    public void setQrPayCode(String qrPayCode) {
        this.qrPayCode = qrPayCode;
    }

    public String getBizOrderNo() {
        return bizOrderNo;
    }

    public void setBizOrderNo(String bizOrderNo) {
        this.bizOrderNo = bizOrderNo;
    }

    public String getSignPath() {
        return signPath;
    }

    public void setSignPath(String signPath) {
        this.signPath = signPath;
    }

    public boolean isFreeSign() {
        return freeSign;
    }

    public void setFreeSign(boolean freeSign) {
        this.freeSign = freeSign;
    }

    public boolean isFreePin() {
        return freePin;
    }

    public void setFreePin(boolean freePin) {
        this.freePin = freePin;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public int getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(int entryMode) {
        this.entryMode = entryMode;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getCardSn() {
        return cardSn;
    }

    public void setCardSn(String cardSn) {
        this.cardSn = cardSn;
    }

    public long getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(long tipAmount) {
        this.tipAmount = tipAmount;
    }

    public long getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(long billAmount) {
        this.billAmount = billAmount;
    }

    public String getEmvPrintData() {
        return emvPrintData;
    }

    public void setEmvPrintData(String emvPrintData) {
        this.emvPrintData = emvPrintData;
    }
}
