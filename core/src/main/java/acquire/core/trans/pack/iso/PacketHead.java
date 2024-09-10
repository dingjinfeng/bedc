package acquire.core.trans.pack.iso;

import androidx.annotation.NonNull;

import java.util.Arrays;

import acquire.base.BaseApplication;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.constant.ParamsConst;

/**
 * Pack head and tpdu
 *
 * @author Janson
 * @date 2019/11/7 16:48
 */
public class PacketHead {
    /**
     * data length bytes
     */
    private final static int LEN_BYTE_COUNT = 2;

    /**
     * Add: length bytes + head + tdpu
     *
     * @param request request data
     * @return the data after adding
     */
    @NonNull
    static byte[] packHeadTpdu(byte[] request) throws Exception {
        //add tpdu
        String tpdu = ParamsUtils.getString(ParamsConst.PARAMS_KEY_COMM_TPDU)+"619900990305";
        byte[] bTpdu = BytesUtils.hexToBytes(tpdu);
        if (bTpdu == null) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_pack_tpdu_error));
        }
        LoggerUtils.d("Add request tpdu: " + tpdu);
        request = BytesUtils.merge(bTpdu, request);

        //add length bytes
        byte[] bLen = BytesUtils.intToBytes(request.length, LEN_BYTE_COUNT);
        LoggerUtils.d("Add request length bytes: " + BytesUtils.bcdToString(bLen)+" => "+request.length);
        return BytesUtils.merge(bLen, request);
    }

    /**
     * Delete: length bytes + head + tdpu
     *
     * @param responseData reponse data
     * @return the data after deleting
     */
    @NonNull
    static byte[] unpackHeadTpdu(@NonNull byte[] responseData) throws Exception {
        if (responseData.length < LEN_BYTE_COUNT) {
            LoggerUtils.e("Response length is shorter than " + LEN_BYTE_COUNT);
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_unpack_length_error));
        }
        LoggerUtils.d("Response length: " + BytesUtils.bcdToString(Arrays.copyOfRange(responseData, 0, responseData.length)));
        //delete length bytes
        byte[] bLen = Arrays.copyOfRange(responseData, 0, LEN_BYTE_COUNT);
        int len = BytesUtils.bytesToInt(bLen);
        if (len == 0 || len != responseData.length - LEN_BYTE_COUNT) {
            LoggerUtils.e("Response length is wrong.");
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_unpack_length_error));
        }
        LoggerUtils.d("Delete response length: " + BytesUtils.bcdToString(bLen)+" => "+len);
        responseData = Arrays.copyOfRange(responseData, LEN_BYTE_COUNT, responseData.length);

        //delete tpdu
        String tpdu = ParamsUtils.getString(ParamsConst.PARAMS_KEY_COMM_TPDU);
        int tpduLen = tpdu.length() + 1;
        if (responseData.length < tpduLen) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_unpack_tpdu_error));
        }
        LoggerUtils.d("Delete response tpdu: " + BytesUtils.bcdToString(Arrays.copyOfRange(responseData, 0, tpduLen)));
        responseData = Arrays.copyOfRange(responseData, tpduLen, responseData.length);
        if (responseData.length == 0) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_unpack_response_format_error));
        }
        return responseData;
    }
}
