package acquire.core.constant;

import android.content.Context;

import acquire.base.utils.iso8583.ISO8583;
import acquire.core.tools.AnswerCodeProvider;
import acquire.core.tools.AppParamsImporter;
import acquire.core.tools.CardBinProvider;
import acquire.core.tools.CurrencyCodeProvider;
import acquire.core.tools.EmvConfigXmlParser;
import acquire.core.trans.pack.iso.Caller;
import acquire.sdk.emv.IEmvParamLoader;

/**
 * Asset file name
 *
 * @author Janson
 * @date 2018/3/26
 */
public class FileConst {
    /**
     * 8583 configuration file as the argument {@link ISO8583#loadXmlFile(Context, String)}
     */
    public final static String CUPS8583 = "8583.xml";
    /**
     * Default parameters file used in {@link AppParamsImporter}
     */
    public final static String PARAMS = "default_params.properties";
    /**
     * Default merchants file used in {@link AppParamsImporter}
     */
    public final static String MERCHANTS = "default_merchants.properties";
    /**
     * Receipt LOGO.
     */
    public final static String LOGO_IMG = "RECEIPT_LOGO.bmp";
    /**
     * Response code mapping file used in {@link AnswerCodeProvider}
     */
    public final static String ANSWER_CODE = "answercode.properties";
    /**
     * Card bin table used in {@link CardBinProvider}
     */
    public final static String CARD_BIN = "cardbin.xml";

    /**
     * SSL certificate used by {@link Caller}.
     * <p>replace with your own SSL certificate
     */
    public final static String PEM_CERT = "cacert.pem";

    /**
     * EMV configuration as the argument {@link EmvConfigXmlParser#parseXml(Context, String, IEmvParamLoader)}
     * Newland_L3_configuration_UL.xml：UL EMV configuration
     */
    public final static String EMV_CONFIG = "Newland_L3_configuration.xml";

    /**
     * Currency code information used in {@link CurrencyCodeProvider}
     */
    public final static String CURRENCYCODE = "currencycode.properties";


}
