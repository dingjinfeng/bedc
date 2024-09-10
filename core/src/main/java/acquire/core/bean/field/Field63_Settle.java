package acquire.core.bean.field;

import java.util.Locale;
import java.util.Map;

import acquire.core.constant.SettleAttr;
import acquire.core.tools.StatisticsUtils;
import acquire.core.tools.TransUtils;

/**
 * Settlement data
 *
 * @author Janson
 * @date 2020/7/10 10:13
 */
public class Field63_Settle {

    /**
     * Debit numbers of CUP
     */
    private int debitNum;
    /**
     * Debit amout of CUP
     */
    private long debitAmount;
    /**
     * Credit numbers of CUP
     */
    private int creditNum;
    /**
     * Credit amout of CUP
     */
    private long creditAmount;


    public Field63_Settle(String mid,String tid) {
        Map<String,long[]> total = StatisticsUtils.getTotalAmtNum(mid,tid);
        for (String transType : total.keySet()) {
            long[] amtNum = total.get(transType);
            if (amtNum == null){
                continue;
            }
            long amt = amtNum[0];
            long num = amtNum[1];
            int settleAttr = TransUtils.getSettleAttr(transType);
            if (settleAttr == SettleAttr.PLUS){
                debitAmount += amt;
                debitNum +=num;
            }else if (settleAttr == SettleAttr.REDUCE){
                creditAmount += amt;
                creditNum +=num;
            }
        }
    }

    public int getDebitNum() {
        return debitNum;
    }

    public long getDebitAmount() {
        return debitAmount;
    }

    public int getCreditNum() {
        return creditNum;
    }

    public long getCreditAmount() {
        return creditAmount;
    }

    public String getString() {
        return String.format(Locale.getDefault(), "%012d", debitAmount)
                + String.format(Locale.getDefault(), "%03d", debitNum)
                + String.format(Locale.getDefault(), "%012d", creditAmount)
                + String.format(Locale.getDefault(), "%03d", creditNum);
    }

    public long getTotalAmount() {
        return debitAmount + creditAmount;
    }

}
