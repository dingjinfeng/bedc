package acquire.sdk.serial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.newland.AnalogSerialManager;
import android.newland.content.NlContext;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * A micro USB or type c communication tool.
 *
 * @author Janson
 * @date 2022/10/11 9:31
 * @since 3.6
 */
public class BMicroUsbPort {
    private final AnalogSerialManager analogSerialManager;

    @SuppressLint("WrongConstant")
    public BMicroUsbPort(Context context) {
        analogSerialManager =
                (AnalogSerialManager) context.getSystemService(NlContext.ANALOG_SERIAL_SERVICE);
    }

    public boolean open() {
        if (analogSerialManager.open() == -1) {
            return false;
        }
        return analogSerialManager.setconfig(9600, 0, "8N1NN".getBytes()) == 0;
    }

    public void close() {
        analogSerialManager.close();
    }

    public byte[] read(int lengthMax, int timeoutMillis) {
        byte[] readBuf = new byte[lengthMax];
        // <=0, read data immediately;
        // >0,It does not end until the maximum length data is read or the readTimeout occurs
        int readLen = analogSerialManager.read(readBuf, readBuf.length, (int) Math.ceil((float)timeoutMillis/1000));
        if (readLen >= 0) {
            //read success
            return Arrays.copyOf(readBuf, readLen);
        } else {
            //read failed
            return null;
        }
    }

    public boolean write(@NonNull byte[] data) {
        // <=0, write data immediately;
        // >0, write time allowed
        int timeout = 0;
        int writeLen = analogSerialManager.write(data, data.length, timeout);
        return writeLen >= 0;
    }

    public void flush(){
        /*
            0: flush read cache
            1: flush write cache
            2: flush read/write cache
         */
        byte[] args = new byte[]{2};
        analogSerialManager.ioctl(0x541B,args);
    }
} 
