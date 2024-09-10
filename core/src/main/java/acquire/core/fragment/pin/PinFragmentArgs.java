package acquire.core.fragment.pin;

import java.io.Serializable;

/**
 * The PIN pad fragment arguments
 *
 * @author Janson
 * @date 2021/6/25 9:12
 */
public class PinFragmentArgs implements Serializable {
    private String pan;
    private String currencyCode;
    private long amount;
    private String cardOrganization;
    private boolean isOnlinePin;
    /**
     * Supported pin length
     */
    private byte[] pinLengths;

    public void setPan(String pan) {
        this.pan = pan;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setCardOrganization(String cardOrganization) {
        this.cardOrganization = cardOrganization;
    }

    public void setOnlinePin(boolean onlinePin) {
        isOnlinePin = onlinePin;
    }

    public void setPinLengths(byte... pinLengths) {
        this.pinLengths = pinLengths;
    }

    public String getPan() {
        return pan;
    }

    public long getAmount() {
        return amount;
    }

    public String getCardOrganization() {
        return cardOrganization;
    }

    public boolean isOnlinePin() {
        return isOnlinePin;
    }

    public byte[] getPinLengths() {
        return pinLengths;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
