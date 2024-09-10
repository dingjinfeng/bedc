package acquire.sdk.emv.constant;


import com.newland.nsdk.core.api.internal.emvl2.type.EmvConst;

/**
 * EMV transaction type
 *
 * @author Janson
 * @date 2019/10/21 9:25
 */
public class EmvTransType {
    public static final int SALE = EmvConst.EMV_TRANS_EP_PURCHASE;
    public static final int REFUND = EmvConst.EMV_TRANS_EP_REFUND;
    public static final int CASH_BACK = EmvConst.EMV_TRANS_EP_PURCHASE_CASHBACK;
    public static final int PREAUTH = EmvConst.EMV_TRANS_PREAUTH;
    public static final int BALANCE = EmvConst.EMV_TRANS_BALANCE;

}
