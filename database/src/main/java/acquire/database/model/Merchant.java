package acquire.database.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Merchant table entity
 *
 * @author Janson
 * @date 2021/3/12 11:02
 */
@Entity(tableName = "T_MERCHANT")
public class Merchant implements Cloneable{
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
     * Card organization
     */
    @ColumnInfo(name = "CARD_ORGANIZATION")
    private String cardOrg;
    /**
     * Batch num
     */
    @ColumnInfo(name = "BATCH_NO")
    private String batchNo;

    /**
     * Settle balance
     */
    @ColumnInfo(name = "SETTLE_EQUAL")
    private boolean settleEqual;
    /**
     * The settle step
     */
    @ColumnInfo(name = "SETTLE_STEP")
    private int settleStep;
    /**
     * The date of settlement,yyyyMMdd
     */
    @ColumnInfo(name = "SETTLE_DATE")
    private String settleDate;
    /**
     * The time of settlement,HHmmss
     */
    @ColumnInfo(name = "SETTLE_TIME")
    private String settleTime;

    @ColumnInfo(name = "LAST_RECEIPT")
    private String lastReceipt;

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

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }


    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getSettleStep() {
        return settleStep;
    }

    public void setSettleStep(int settleStep) {
        this.settleStep = settleStep;
    }

    public boolean isSettleEqual() {
        return settleEqual;
    }

    public void setSettleEqual(boolean settleEqual) {
        this.settleEqual = settleEqual;
    }

    public String getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(String settleDate) {
        this.settleDate = settleDate;
    }

    public String getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(String settleTime) {
        this.settleTime = settleTime;
    }

    public String getCardOrg() {
        return cardOrg;
    }

    public void setCardOrg(String cardOrg) {
        this.cardOrg = cardOrg;
    }

    public String getLastReceipt() {
        return lastReceipt;
    }

    public void setLastReceipt(String lastReceipt) {
        this.lastReceipt = lastReceipt;
    }

    @NonNull
    @Override
    public Merchant clone()  {
        try {
            return (Merchant) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new Merchant();
        }
    }

    @Override
    public String toString() {
        return "Merchant{" +
                "mid='" + mid + '\'' +
                ", tid='" + tid + '\'' +
                ", cardOrg='" + cardOrg + '\'' +
                ", batchNo='" + batchNo + '\'' +
                '}';
    }
}
