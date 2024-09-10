package acquire.sdk.emv.constant;

import java.util.Locale;

import acquire.base.utils.BytesUtils;

public interface AidConstant {
    //接触Mastercard AID
    byte[] MasterCard1AidCfgCt = BytesUtils.hexToBytes(
            "9F0607A0000000041010" +
                    "DF13050000000000" + "DF1205FC50BCF800" + "DF1105FC50BC2000" +
                    "9F09020002" + "9F1B0400000000" + "9F1D086C78000000000000");
    //接触VISA AID
    byte[] VisaAidCfgCt = BytesUtils.hexToBytes(
            "9F0607A0000000031010" +
                    "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" +
                    "9F0902008C" + "9F1B0400000000" + "9F530152" +
                    "1F811F0A9F0AFFFFFFFF02030320");
    //接触银联AID
    byte[] PbocAidCfgCt = BytesUtils.hexToBytes(
            "9F0607A0000003330101" +
                    "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" +
                    "9F09020020");
    //非接Mastercar AID
    byte[] Paypass1AidCfgCl = BytesUtils.hexToBytes(
            "9F0607A0000000041010" +
                    "DF37080200000000000000" + "9F3303E0F8C8" + "DF480168" +
                    "DF420110" + "DF470100" + "DF2F0120" + "DF2B039F6A04" + "DF460101" +
                    "9F7B06000009999999" + "9F1D086C78000000000000" +
                    "DF2106000000025000" + "DF1906000000020000" +
                    "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800");
    //非接VISA AID
    byte[] VisaAidCfgCl = BytesUtils.hexToBytes(
            "9F0607A0000000031010" +
                    "DF37080300000000000000" + "9F660432204000" + "9F1A020826" +
                    "9F0902008C" + "9F1B0400000000" + "9F530152" +
                    "DF2006009999999999" + "DF1906000000000000" + "DF2106000000025001");
    //非接银联 AID
    byte[] PbocAidCfgCl = BytesUtils.hexToBytes(
            "9F0607A0000003330101" +
                    "DF37080700000000000000" + "9F660436004080" + "5F2A020156");
    byte[] termCfgCt = com.newland.sdk.emvl3.api.common.util.BytesUtils.hexToBytes(
            ("9F061000000000000000000000000000000000" +
                    "DF2407F4C0F0E8EF0E60" +
                    "5F2A020156" + "DF220400000000" +
                    "9F7A0101" + "9F350122" + "DF010101" + "9F3303E0F8C8" +
                    "9F4005FF80F0A001" + "9F0106123456789000" + "9F15021234" +
                    "9F160F313233343536373839303132333435" + "9F3C020826" + "5F360102" +
                    "9F3D0100" + "9F1A020156" + "9F1E083030303030303031" + "9F1C083132333435363738" +
                    "9F7B06000000050000" + "DF160100" + "DF170100" + "DF1504000001F4" + "9F1B0400000000" +
                    "DF44039F3704" + "DF450F9F02065F2A029A039C0195059F3704" + "9F09020002").toUpperCase(Locale.ROOT));
    byte[] termCfgCl = com.newland.sdk.emvl3.api.common.util.BytesUtils.hexToBytes(
            ("9F061000000000000000000000000000000000" +
                    "DF2407F4C0F0E8EF0E62" + "9F350122" + "9F3303E0F8C8" + "9F4005FF80F0A001" +
                    "9F0106123456789000" + "9F15021234" + "9F160F313233343536373839303132333435" +
                    "5F360102" + "9F3C020826" + "9F3D0102" + "9F1A020156" + "9F1E083030303030303031" +
                    "DF27011F" + "DF2006000099999999" + "DF1906000000020000" + "DF2106000000500000" +
                    "DF3A0101" + "DF390100" + "DF150400000000" + "9F09020002" +
                    "DF440B9f37049f47018f019f3201" + "DF45039F0802" + "DF010101" +
                    "5F2A020156" + "9F1B0400000000" + "1F81020100").toUpperCase(Locale.ROOT));
}
