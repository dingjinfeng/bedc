package acquire.core.constant;

import acquire.sdk.ConnectMode;
import acquire.sdk.pin.constant.KeyAlgorithmType;

/**
 * The keys of parameters for local file storage
 *
 * @author Janson
 * @date 2018/3/26
 */
public class ParamsConst {

    /**
     * App first start flag.
     */
    public final static String PARAMS_KEY_FIRST_RUN = "FIRST_RUN";

    //===========================[CONFIG VERSION]===========================
    
    public final static String PARAMS_KEY_EMV_AID_CAPK = "EMV_AID_CAPK";

    //===========================[BASE]===========================
    /**
     * merchant name
     */
    public final static String PARAMS_KEY_BASE_MERCHANT_NAME = "BASE_MERCHANT_NAME";
    /**
     * Trace No.
     */
    public final static String PARAMS_KEY_BASE_TRACE_NO = "BASE_TRACE_NO";
    /**
     * Max amount of refund in cents (long type)
     */
    public final static String PARAMS_KEY_BASE_MAX_REFUND_AMOUNT = "BASE_MAX_REFUND_AMOUNT";
    /**
     * Currency code. Such as, 840($),156(¥)
     */
    public final static String PARAMS_KEY_BASE_CURRENCY_CODE = "BASE_CURRENCY_CODE";
    /**
     * Transaction records max count
     */
    public final static String PARAMS_KEY_BASE_MAX_TRANS_COUNT = "BASE_MAX_TRANS_COUNT";

    //----------------------------[PINPAD]----------------------------
    /**
     * Master key index
     */
    public final static String PARAMS_KEY_PINPAD_MASTER_KEY_INDEX = "PINPAD_MASTER_KEY_INDEX";
    /**
     * Pinpad timeout in seconds
     */
    public final static String PARAMS_KEY_PINPAD_TIMEOUT = "PINPAD_TIMEOUT";


    /**
     * algorithm type.
     *
     * @see KeyAlgorithmType
     */
    public final static String PARAMS_KEY_PINPAD_ALGORITHM_TYPE = "PINPAD_ALGORITHM_TYPE";

    //===========================[EXTERNAL PIN PAD]===========================
    /**
     * input PIN&Read Card by external PIN pad
     */
    public final static String PARAMS_KEY_EXTERNAL_PINPAD = "EXTERNAL_PINPAD";
    /**
     * external PIN pad connection mode.
     *
     * @see ConnectMode
     */
    public final static String PARAMS_KEY_EXTERNAL_PINPAD_CONNECT_MODE = "EXTERNAL_PINPAD_CONNECT_MODE";
    //===========================[TRANS SUPPORT]===========================
    /**
     * Sale
     */
    public final static String PARAMS_KEY_TRANS_SALE = "TRANS_SALE";
    /**
     * Void sale
     */
    public final static String PARAMS_KEY_TRANS_VOID = "TRANS_VOID";
    /**
     * Refund
     */
    public final static String PARAMS_KEY_TRANS_REFUND = "TRANS_REFUND";
    /**
     * Balance
     */
    public final static String PARAMS_KEY_TRANS_BALANCE = "TRANS_BALANCE";
    /**
     * Pre-Auth
     */
    public final static String PARAMS_KEY_TRANS_PREAUTH = "TRANS_PREAUTH";
    /**
     * Mobile Pay
     */
    public final static String PARAMS_KEY_TRANS_MOBILE_PAY = "TRANS_MOBILE_PAY";
    /**
     * Installment
     */
    public final static String PARAMS_KEY_TRANS_INSTALLMENT = "TRANS_INSTALLMENT";

    //===========================[COMM]===========================
    /**
     * use ssl
     */
    public final static String PARAMS_KEY_COMM_USE_SSL = "COMM_USE_SSL";
    /**
     * Communication timeout in seconds
     */
    public final static String PARAMS_KEY_COMM_TIMEOUT = "COMM_TIMEOUT";
    /**
     * Server address
     */
    public final static String PARAMS_KEY_COMM_SERVER_ADDRESS = "COMM_SERVER_ADDRESS";
    /**
     * Server port
     */
    public final static String PARAMS_KEY_COMM_PORT = "COMM_PORT";
    /**
     * TPDU
     */
    public final static String PARAMS_KEY_COMM_TPDU = "COMM_TPDU";
    /**
     * NII
     */
    public final static String PARAMS_KEY_COMM_NII = "COMM_NII";

    //===========================[ELECSIGN]===========================
    /**
     * use electric signature
     */
    public final static String PARAMS_KEY_ELECSIGN_IS_SUPPORT = "ELECSIGN_IS_SUPPORT";

    //===========================[PRINT]===========================
    /**
     * Printing count
     */
    public final static String PARAMS_KEY_PRINT_COUNT = "PRINT_COUNT";
    /**
     * Printing remarks
     */
    public final static String PARAMS_KEY_PRINT_REMARKS = "PRINT_REMARKS";
    /**
     * use external printer
     */
    public final static String PARAMS_KEY_PRINT_EXTERNAL = "PRINT_EXTERNAL";
    /**
     * external printer connection mode.
     *
     * @see ConnectMode
     */
    public final static String PARAMS_KEY_PRINT_EXTERNAL_CONNECT_MODE = "PRINT_EXTERNAL_CONNECT_MODE";
    /**
     * External scanner serial baud rate. e.g. 115200, 9600
     */
    public final static String PARAMS_KEY_PRINT_EXTERNAL_SERIAL_BAUDRATE = "PRINT_EXTERNAL_SERIAL_BAUDRATE";
    //===========================[SCAN]===========================
    /**
     * first priority scanner.
     *
     * @see Scanner
     */
    public final static String PARAMS_KEY_SCAN_PRIORITY_SCANNER = "SCAN_PRIORITY_SCANNER";
    /**
     * The external scanner mode. {@link ConnectMode}
     */
    public final static String PARAMS_KEY_SCAN_EXTERN_CONNECT_MODE = "SCAN_EXTERN_CONNECT_MODE";
    /**
     * Waiting time for USB receipting completion.
     */
    public final static String PARAMS_KEY_SCAN_EXTERN_USB_WAIT_TIME = "SCAN_EXTERN_USB_WAIT_TIME";
    /**
     * External scanner serial baud rate. e.g. 115200, 9600
     */
    public final static String PARAMS_KEY_SCAN_EXTERN_SERIAL_BAUDRATE = "SCAN_EXTERN_SERIAL_BAUDRATE";

    //===========================[PASSWORD]===========================
    /**
     * Transaction password
     */
    public final static String PARAMS_KEY_PASSWORD_ADMIN = "PASSWORD_ADMIN";
    /**
     * Setting password
     */
    public final static String PARAMS_KEY_PASSWORD_SYSTEM_ADMIN = "PASSWORD_SYSTEM_ADMIN";
    /**
     * Safe password
     */
    public final static String PARAMS_KEY_PASSWORD_SECURITY = "PASSWORD_SECURITY";

    //===========================[TOMS]===========================
    /**
     * TOMS FLY Parameter
     */
    public final static String PARAMS_KEY_TOMS_FLY_PARAMETERS = "TOMS_FLY_PARAMETERS";
    /**
     * TOMS FLY Receipt
     */
    public final static String PARAMS_KEY_TOMS_FLY_RECEIPT = "TOMS_FLY_RECEIPT";


    //===========================[OTHER]===========================
    /**
     * Void transaction need card
     */
    public final static String PARAMS_KEY_OTHER_VOID_CARD = "OTHER_VOID_CARD";
    /**
     * Void transaction need PIN
     */
    public final static String PARAMS_KEY_OTHER_VOID_PIN = "OTHER_VOID_PIN";
    /**
     * Third application display transaction result
     */
    public final static String PARAMS_KEY_OTHER_THRID_BILL_SHOW = "OTHER_THRID_BILL_SHOW";
    /**
     * Input tip
     */
    public final static String PARAMS_KEY_OTHER_TIP_INPUT = "OTHER_TIP_INPUT";
    /**
     * The interface overlay others on the scecond screen.
     */
    public final static String PARAMS_KEY_OTHER_SECOND_SCREEN_TOP = "OTHER_SECOND_SCREEN_TOP";
}
