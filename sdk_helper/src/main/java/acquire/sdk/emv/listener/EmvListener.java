package acquire.sdk.emv.listener;


import java.util.List;

import acquire.sdk.emv.bean.EmvReadyBean;
import acquire.sdk.emv.bean.PinResult;

/**
 * A abstract EmvL3Listener
 *
 * @author Janson
 * @date 2021/4/15 16:11
 */
public interface EmvListener {

    /**
     * ready to read card
     *
     * @param emvReadyBean describe the card entry mode and status
     */
    void onReady(EmvReadyBean emvReadyBean);

    /**
     * reading card
     */
    void onReading();

    /**
     * select emv aid
     *
     * @param preferNames aids prefer name
     * @return aid index, If -1, failed.
     */
    int onSelectAid(List<String> preferNames);

    /**
     * Insert failed
     *
     * @return <code>true</code> if emv continues; <code>false</code> if cancel card
     */
    boolean onInsertError();

    /**
     * final select
     */
    void onFinalSelect();

    /**
     * see phone
     *
     * @return true if retry card; false if cancelling card.
     */
    boolean onSeePhone();

    /**
     * get card number
     *
     * @param pan card number
     * @return true if emv continues; false if card cancel.
     */
    boolean onCardNum(String pan);

    /**
     * input PIN
     *
     * @param online      online PIN
     * @param pinTryCount Entered PIN times
     * @return PIN result
     */
    PinResult onInputPin(boolean online, int pinTryCount);

    /**
     * emv result
     *
     * @param success   true if emv process executed success.
     * @param emvResult EMV result code.
     * @see acquire.sdk.emv.constant.EmvResult
     */
    void onResult(boolean success, int emvResult);

}
