package acquire.core.constant;

import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * Receipt font size in pixels
 *
 * @author Janson
 * @date 2022/4/2 10:15
 */
public class PrintSize {
    public static int NORMAL = 22;
    public static int SMALL = 18;
    public static int TRAN_TYPE = 32;
    public static int AMOUNT = 50;
    public static int LINE = 30;
    public static int SIGN_FEED = 80;
    public static int END_FEED = 60;

    static {
        if (BDevice.getDeviceModel() != null){
            switch (BDevice.getDeviceModel()) {
                case Model.X3:
                case Model.X5:
                    NORMAL = 30;
                    TRAN_TYPE = 40;
                    AMOUNT = 60;
                    LINE = 40;
                    SIGN_FEED = 60;
                    END_FEED = 0;
                    break;
                case Model.N950:
                    END_FEED = 100;
                    break;
                case Model.X800:
                    END_FEED = 140;
                    break;
                default:
                    break;
            }
        }


    }
}
