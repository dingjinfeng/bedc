package acquire.sdk.serial;

import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.common.serialport.BaudRate;
import com.newland.nsdk.core.api.common.serialport.DataBits;
import com.newland.nsdk.core.api.common.serialport.ParityBit;
import com.newland.nsdk.core.api.common.serialport.StopBits;
import com.newland.nsdk.core.api.common.uart3.UART3Config;
import com.newland.nsdk.core.api.common.uart3.UART3Port;
import com.newland.nsdk.core.api.common.uart3.UART3Type;
import com.newland.nsdk.core.common.uart3.UART3PortImpl;

import acquire.sdk.device.BDevice;

/**
 * A serial port communication tool.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *   //Cash register's serial port is different from portable POS.
 *   BSerialPort port = new BSerialPort();
 *   //1. open by baud rate.
 *   boolean isOpen = port.open(baudRate);
 *   //2. read/write data.
 *   byte[] readBytes = port.read(maxLen,100);
 *   port.write(data,maxLen,100);
 *   //3. finish
 *   port.close();
 * </pre>
 *
 * @author Janson
 * @date 2021/11/15 10:14
 */
public class BSerialPort {
    private final UART3Port mUart3Port;

    /**
     * Construct a serial communication tool.
     */
    public BSerialPort() {
       this(false);
    }
    public BSerialPort(boolean isPinpad) {
        UART3Type uart3Type;
        if (BDevice.isCpos()) {
            if (isPinpad){
                uart3Type = UART3Type.PINPAD_CPOS;
            }else{
                uart3Type = UART3Type.RS232_CPOS;
            }
        } else {
            if (isPinpad){
                uart3Type = UART3Type.PINPAD_A7;
            }else{
                uart3Type = UART3Type.RS232_A7;
            }
        }
        mUart3Port = new UART3PortImpl(uart3Type);
    }

    /**
     * Open the serial port
     *
     * @param baudRate port baud rate. e.g. 9600、115200
     * @return true: open success; false: open failed
     */
    public boolean open(final int baudRate) {
        try {
            BaudRate rate = BaudRate.BPS115200;
            for (BaudRate value : BaudRate.values()) {
                if (value.toValue() == baudRate) {
                    rate = value;
                    break;
                }
            }
            UART3Config config = new UART3Config(rate, DataBits.DATA_BIT_8, ParityBit.NO_CHECK, StopBits.STOP_BIT_ONE);
            mUart3Port.setConfig(config);
            mUart3Port.open();
            return true;
        } catch (NSDKException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Read the serial port
     *
     * @param lengthMax    Expect read length
     * @param timeoutMillis Reading time out, unit: ms. If value <= 0, read the existing serial port data immediately without waiting.
     * @return data of reading serial port
     */
    public byte[] read(int lengthMax, int timeoutMillis) {
        try {
            return mUart3Port.read(lengthMax, timeoutMillis);
        } catch (NSDKException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Write data
     *
     * @param data         Writes the data buffer corresponding to the source address
     * @return true: write success; false: write failed
     */
    public boolean write(byte[] data) {
        try {
            int lengthMax = data.length;
            int timeoutMillis = 0;
            mUart3Port.write(data, lengthMax, timeoutMillis);
            return true;
        } catch (NSDKException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Close the serial port
     *
     * @return true: close success; false: close failed.
     */
    public boolean close() {
        try {
            mUart3Port.close();
            return true;
        } catch (NSDKException e) {
            e.printStackTrace();
            return false;
        }
    }
}
