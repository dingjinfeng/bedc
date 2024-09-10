package acquire.core.bean;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.List;
import java.util.Locale;

import acquire.base.BaseApplication;
import acquire.base.utils.ParamsUtils;
import acquire.core.BindTag;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.core.tools.DataConverter;
import acquire.core.trans.impl.settle.Settle;
import acquire.database.model.Merchant;
import acquire.sdk.emv.constant.EntryMode;

/**
 * A public bean of transaction.
 * <p>It can be set value from bundle or uri by {@link BindTag}.</p>
 *
 * @author Janson
 * @date 2021/6/30 15:45
 * @see DataConverter#intentToPubBean(Intent, PubBean)
 */
public class PubBean implements Cloneable {
    /**
     * Transaction Type.
     *
     * @see TransType
     */
    @BindTag(TransTag.TRANS_TYPE)
    private String transType;

    /**
     * Transaction Name.
     */
    private String transName;

    /**
     * Result Message.
     */
    @BindTag(TransTag.MESSAGE)
    private String message;
    /**
     * Entry Mode.
     *
     * @see EntryMode
     */
    @BindTag(TransTag.ENTRY_MODE)
    private int entryMode;

    /**
     * Currency Code
     */
    @BindTag(TransTag.CURRENCY_CODE)
    private String currencyCode;

    /**
     * Field 0 Message Type Id (4)
     */
    private String messageId;

    /**
     * Card No.(19)
     */
    @BindTag(TransTag.CARD_NO)
    private String cardNo;

    /**
     * Processing Code (6)
     */
    private String processCode;

    /**
     * Transaction Amount.(12)
     */
    @BindTag(TransTag.AMOUNT)
    private long amount;

    /**
     * Tips Amount.(12)
     */
    @BindTag(TransTag.TIP)
    private long tipAmount;
    /**
     * Bill Amount.
     * <p>If tip amount exists, it indicates the amount before the tip amount is added.
     */
    @BindTag(TransTag.BILL_AMOUNT)
    private long billAmount;

    /**
     * System Trace No. (6)
     */
    @BindTag(TransTag.TRACE_NO)
    private String traceNo;

    /**
     * Date(yyyyMMdd)
     */
    @BindTag(TransTag.DATE)
    private String date;

    /**
     * Time(HHmmss)
     */
    @BindTag(TransTag.TIME)
    private String time;

    /**
     * Card Expiry Date,YYMM (4)
     */
    private String expDate;

    /**
     * Pos Entry Mode Code
     */
    private String field22;

    /**
     * Card Sequence No.
     */
    private String cardSn;

    /**
     *
     * Server Code
     */
    private String serverCode;

    /**
     * Track 2 Data
     */
    private String track2;

    /**
     * Track 3 Data
     */
    private String track3;

    /**
     * Retrival Reference No.
     */
    @BindTag(TransTag.REFERENCE_NO)
    private String referNo;

    /**
     * Authorization Code
     */
    @BindTag(TransTag.AUTH_CODE)
    private String authCode;
    /**
     * Response Code
     */
    @BindTag(TransTag.RESULT_CODE)
    private String resultCode;

    /**
     * Terminal Id
     */
    @BindTag(TransTag.TID)
    private String tid;

    /**
     * Merchant Id
     */
    @BindTag(TransTag.MID)
    private String mid;

    /**
     * Online Pin Block
     */
    private String pinBlock;
    /**
     * Offline Pin Block
     */
    private String offlinePinBlock;
    /**
     * EMV data
     */
    private String field55;

    /**
     * Batch No.
     */
    @BindTag(TransTag.BATCH_NO)
    private String batchNo;
    /**
     * Original Authorization Code
     */
    @BindTag(TransTag.ORIG_AUTH_CODE)
    private String origAuthCode;

    /**
     * Original Retrival Reference No.
     */
    @BindTag(TransTag.ORIG_REFERENCE_NO)
    private String origReferNo;

    /**
     * Original Transaction Date(yyyyMMdd)
     */
    @BindTag(TransTag.ORIG_DATE)
    private String origDate;

    /**
     * Qr Pay Code
     */
    @BindTag(TransTag.PAY_CODE)
    private String qrPayCode;

    /**
     * Original Trace No. (6)
     */
    @BindTag(TransTag.ORIG_TRACE_NO)
    private String origTraceNo;
    /**
     * Business Order
     */
    @BindTag(TransTag.BIZ_ORDER_NO)
    private String bizOrderNo;
    /**
     * Original business order
     */
    @BindTag(TransTag.ORIG_BIZ_ORDER_NO)
    private String origBizOrderNo;
    /**
     * Card Balance
     */
    @BindTag(TransTag.BALANCE)
    private Long balance;
    /**
     * Print Remarks
     */
    @BindTag(TransTag.REMARK)
    private String remarks;
    /**
     * Require Sign Flag
     */
    private boolean freeSign;
    /**
     * Signature Bitmap Path
     */
    private String signPath;

    /**
     * Out Order No.
     */
    @BindTag(TransTag.OUT_ORDER_NO)
    private String outOrderNo;


    /**
     * merchant name
     */
    @BindTag(TransTag.MERCHANT_NAME)
    private String merchantName;

    /**
     * Card organization
     */
    @BindTag(TransTag.ORGANIZATION)
    private String cardOrg;


    private String nii;

    @BindTag(TransTag.INSTALLMENT_TERM)
    private int instalmentTerm;

    private boolean thirdCall;
    /**
     * merchants to be settled in transaction.
     *
     * @see Settle
     */
    private List<Merchant> settleMerchants;

    @BindTag(TransTag.SETTLE_ALL)
    private boolean settleAll;

    private String emvPrintData;

    private boolean requestOnlineSucc;

    public boolean isFreeSign() {
        return freeSign;
    }

    public void setFreeSign(boolean freeSign) {
        this.freeSign = freeSign;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    /**
     * get entry mode
     * @see EntryMode
     */
    public int getEntryMode() {
        return entryMode;
    }

    /**
     * set entry mode
     * @see EntryMode
     */
    public void setEntryMode(int entryMode) {
        this.entryMode = entryMode;
    }

    public String getCurrencyCode() {
        if (currencyCode == null) {
            currencyCode = ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_CURRENCY_CODE, "840");
        }
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public long getAmount() {
        return amount;
    }

    public String getAmountField() {
        return String.format(Locale.getDefault(), "%012d", amount);
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

    public String getCardSn() {
        return cardSn;
    }

    public void setCardSn(String cardSn) {
        this.cardSn = cardSn;
    }

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
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

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getPinBlock() {
        return pinBlock;
    }

    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }

    public String getField55() {
        return field55;
    }

    public void setField55(String field55) {
        this.field55 = field55;
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

    public String getOrigDate() {
        return origDate;
    }

    public void setOrigDate(String origDate) {
        this.origDate = origDate;
    }

    public String getCardOrg() {
        return cardOrg;
    }

    public void setCardOrg(String internationOrg) {
        this.cardOrg = internationOrg;
    }

    public String getOrigTraceNo() {
        return origTraceNo;
    }

    public void setOrigTraceNo(String origTraceNo) {
        this.origTraceNo = origTraceNo;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    public String getOutOrderNo() {
        return outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(@StringRes int message) {
        this.message = BaseApplication.getAppString(message);
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


    public String getOrigBizOrderNo() {
        return origBizOrderNo;
    }

    public void setOrigBizOrderNo(String origBizOrderNo) {
        this.origBizOrderNo = origBizOrderNo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getSignPath() {
        return signPath;
    }

    public void setSignPath(String signPath) {
        this.signPath = signPath;
    }

    public long getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(long tipAmount) {
        this.tipAmount = tipAmount;
    }



    public boolean isThirdCall() {
        return thirdCall;
    }

    public void setThirdCall(boolean thirdCall) {
        this.thirdCall = thirdCall;
    }

    public List<Merchant> getSettleMerchants() {
        return settleMerchants;
    }

    public void setSettleMerchants(List<Merchant> settleMerchants) {
        this.settleMerchants = settleMerchants;
    }

    public int getInstalmentTerm() {
        return instalmentTerm;
    }

    public void setInstalmentTerm(int instalmentTerm) {
        this.instalmentTerm = instalmentTerm;
    }

    public boolean isSettleAll() {
        return settleAll;
    }

    public void setSettleAll(boolean settleAll) {
        this.settleAll = settleAll;
    }

    public long getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(long billAmount) {
        this.billAmount = billAmount;
    }

    public String getOfflinePinBlock() {
        return offlinePinBlock;
    }

    public void setOfflinePinBlock(String offlinePinBlock) {
        this.offlinePinBlock = offlinePinBlock;
    }

    public String getEmvPrintData() {
        return emvPrintData;
    }

    public void setEmvPrintData(String emvPrintData) {
        this.emvPrintData = emvPrintData;
    }

    public boolean isRequestOnlineSucc() {
        return requestOnlineSucc;
    }

    public void setRequestOnlineSucc(boolean requestOnlineSucc) {
        this.requestOnlineSucc = requestOnlineSucc;
    }

    @NonNull
    @Override
    public PubBean clone() throws CloneNotSupportedException {
        return (PubBean) super.clone();
    }

}
