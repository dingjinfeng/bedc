package acquire.sdk.device;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.devicemanager.DeviceInfo;
import com.newland.nsdk.core.api.internal.devicemanager.DeviceManager;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import java.util.Date;

import acquire.sdk.device.constant.Model;

/**
 * Device information
 *
 * @author Janson
 * @date 2018/3/1
 */
public class BDevice {

    /**
     * set POS system time
     * <p><hr><b>e.g.</b></p>
     * <pre>
     *        Calendar calendar = Calendar.getInstance();
     *        //change time:
     *        //calendar.set(xxx);
     *        Date date = calendar.getTime();
     *        BDevice.setSystemTime(data);
     * </pre>
     *
     * @param date date instant.
     */
    public static void setSystemTime(Date date) {
        try {
            DeviceManager deviceManager = (DeviceManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.DEVICE_MANAGER);
            deviceManager.setPOSDate(date);
        } catch (NSDKException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return true if this device has security module.
     **/
    public static boolean isExistSecurityModule() {
        DeviceManager deviceManager = (DeviceManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.DEVICE_MANAGER);
        return deviceManager.isExistSecurityModule();
    }

    /**
     * Get sdk version
     *
     * @return sdk version
     **/
    public static String getSdkVersion() {
        DeviceManager deviceManager = (DeviceManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.DEVICE_MANAGER);
        return deviceManager.getSDKVersion();
    }


    /**
     * Get the device information
     */
    private static DeviceInfo getDeviceInfo() throws NSDKException {
        DeviceManager deviceManager = (DeviceManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.DEVICE_MANAGER);
        if (deviceManager == null) {
            return null;
        }
        return deviceManager.getDeviceInfo();
    }

    /**
     * Get the sequence number
     *
     * @return device sequence number
     */
    public static String getSn() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.getSN();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get the manufacturer serial number
     *
     * @return manufacturer serial number .
     */
    public static String getCsn() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.getCustomerID();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get the device model.
     *
     * @return The device model
     */
    public static String getDeviceModel() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.getDeviceModel();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * device is CPOS (cash register)
     */
    public static boolean isCpos() {
        String model = getDeviceModel();
        if (model == null) {
            return false;
        } else {
            return model.contains("CPOS");
        }
    }


    /**
     * Get the firmware version number
     *
     * @return Firmware version
     */
    public static String getFirmwareVersion() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.getFirmwareVer();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Return true if this device supports printing.
     */
    public static boolean supportPrint() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.isSupportPrint();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Return true if this device supports inserting card.
     */
    public static boolean supportInsert() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.isSupportICCard();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Return true if this device supports rfid card.
     */
    public static boolean supportRf() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.isSupportQuickPass();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Return true if this device supports mag card.
     */
    public static boolean supportMag() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.isSupportMagCard();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Return true if this device supports external scanner by USB.
     */
    public static boolean supportUsbHost() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.isSupportUSB();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Return true if this device supports external PIN pad
     */
    public static boolean supportExternalPinPad() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.isSupportPinpadPort();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Return true if this device supports RS232 serial port
     */
    public static boolean supportExternalRs232() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.isSupport232Port();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getLedConfig() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.getLEDConfig();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Return true if this device supports hard scanner
     */
    public static boolean supportHardScanner() {
        try {
            DeviceInfo deviceInfo = getDeviceInfo();
            if (null != deviceInfo) {
                return deviceInfo.getScannerConfig().supportHardScanning();
            }
        } catch (NSDKException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Return true if this device has physical keyboard
     */
    public static boolean supportPhysicalKeyboard() {
        String model = getDeviceModel();
        return Model.P300.equals(model);
    }
}
