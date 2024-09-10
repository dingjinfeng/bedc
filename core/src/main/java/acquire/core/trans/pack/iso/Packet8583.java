package acquire.core.trans.pack.iso;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import acquire.base.BaseApplication;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.iso8583.ISO8583;
import acquire.base.utils.iso8583.ISO8583Exception;
import acquire.core.R;
import acquire.core.bean.PubBean;
import acquire.core.tools.AnswerCodeProvider;
import acquire.core.tools.PinpadHelper;

/**
 * Pack ISO8583
 *
 * @author Janson
 * @date 2019/11/7 16:50
 */
public class Packet8583 {
    /**
     * caculate mac data
     */
    public static String getMac(ISO8583 iso8583) throws Exception {
        iso8583.addFieldToBitmap(64, true);
        String macFields = iso8583.getMacSrcData();
        LoggerUtils.d("Mac src data:"+macFields);
        if (macFields == null){
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_mac_src_error));
        }
        String mac = new PinpadHelper().getMac(macFields);
        if (TextUtils.isEmpty(mac)) {
            LoggerUtils.e("Mac error");
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_add_mac_error));
        }
        return mac;
    }

    /**
     * Pack 8583
     *
     * @return 8583 data
     */
    @NonNull
    static byte[] pack8583(@NonNull ISO8583 iso8583) throws Exception {
        String request = iso8583.pack();
        if (TextUtils.isEmpty(request)) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_pack_iso8583_empty));
        }
        return BytesUtils.hexToBytes(request);
    }

    /**
     * unpack 8583
     */
    static void unpack8583(@NonNull ISO8583 iso8583, byte[] response) throws ISO8583Exception {
        String data = BytesUtils.bcdToString(response);
        LoggerUtils.d("Unpack ISO8583:[" + data + "]");
        iso8583.initPack();
        iso8583.unpack(data);
    }

    /**
     * Parse 8583 fields
     */
    static void parseRespons(@NonNull ISO8583 iso8583,boolean checkMac, @NonNull PubBean pubBean) throws Exception {
        //check message type
        byte[] respMsgId = BytesUtils.hexToBytes(iso8583.getField(0));
        byte[] reqMsgId = BytesUtils.hexToBytes(pubBean.getMessageId());
        reqMsgId[1] |= 0x10;
        if (!Arrays.equals(respMsgId, reqMsgId)) {
            throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_msg_type_error));
        }
        //check whether mac exists.
        if (checkMac && iso8583.getField(64) == null){
            throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_mac_error));
        }
        //parse fields
        char[] bitmap = iso8583.getIsoBitmap();
        String fieldData;
        for (int index = 2; index <= bitmap.length; index++) {
            if (bitmap[index - 1] != '1') {
                continue;
            }
            fieldData = iso8583.getField(index);
            if (TextUtils.isEmpty(fieldData)) {
                continue;
            }
            switch (index) {
                case 2:
                    //card number
                    pubBean.setCardNo(fieldData);
                    break;
                case 3:
                    if (!fieldData.equals(pubBean.getProcessCode())) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_process_code_error));
                    }
                    break;
                case 4:
                    long amount = Long.parseLong(fieldData);
                    if (pubBean.getAmount() != 0L && amount != pubBean.getAmount()) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_amount_error));
                    }
                    pubBean.setAmount(amount);
                    break;
                case 11:
                    //trace num
                    if (!fieldData.equals(pubBean.getTraceNo())) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_trace_no_error));
                    }
                    break;
                case 12:
                    pubBean.setTime(fieldData);
                    break;
                case 13:
                    if (fieldData.length() < 8) {
                        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                        pubBean.setDate(yearFormat.format(new Date()) + fieldData);
                    } else {
                        pubBean.setDate(fieldData);
                    }
                    break;
                case 14:
                    pubBean.setExpDate(fieldData);
                    break;
                case 25:
                    if (!fieldData.equals(pubBean.getServerCode())) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_sevice_code_error));
                    }
                    break;
                case 37:
                    if (fieldData.length() == 0) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_no_refnum_error));
                    }
                    pubBean.setReferNo(fieldData);
                    break;
                case 38:
                    pubBean.setAuthCode(fieldData);
                    break;
                case 39:

                    pubBean.setResultCode(fieldData);
                    pubBean.setMessage(AnswerCodeProvider.getRspMessage(fieldData));
                    break;
                case 41:
                    if (!fieldData.equals(pubBean.getTid())) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_posid_error));
                    }
                    break;
                case 42:
                    if (!fieldData.equals(pubBean.getMid())) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_shopid_error));
                    }
                    break;
                case 49:

                    if (!fieldData.equals(pubBean.getCurrencyCode())) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_currency_error));
                    }
                    break;
                case 64:

                    //check mac
                    String mac = getMac(iso8583);
                    if (!fieldData.equals(mac)) {
                        throw new Exception(BaseApplication.getAppString(R.string.core_response_field_fail_mac_error));
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
