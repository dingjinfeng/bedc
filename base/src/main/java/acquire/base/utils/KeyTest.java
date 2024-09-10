package acquire.base.utils;

import acquire.base.utils.crypto.DukptUtils;
import acquire.base.utils.crypto.MacUtils;
import acquire.base.utils.crypto.PinUtils;

/**
 * Test mac or pin.
 *
 * @author Janson
 * @date 2022/4/2 15:42
 */
public class KeyTest {

    public static String testDukptMac(String ipek, String ksn, String data){
        byte[] pek = DukptUtils.generatePek(BytesUtils.hexToBytes(ipek),BytesUtils.hexToBytes(ksn));
        byte[] macKey = DukptUtils.generateWorkKey(pek)[1];
        byte[] mac = MacUtils.softMacX919(macKey,BytesUtils.hexToBytes(data));
        return BytesUtils.bcdToString(mac);
    }

    public static String testDukptPin(String ipek, String ksn, String pin, String cardNo){
        byte[] pek = DukptUtils.generatePek(BytesUtils.hexToBytes(ipek),BytesUtils.hexToBytes(ksn));
        byte[] pinKey = DukptUtils.generateWorkKey(pek)[0];
        byte[] pinBlock = PinUtils.softPinBlock(pinKey,pin,cardNo);
        return BytesUtils.bcdToString(pinBlock);
    }


}
