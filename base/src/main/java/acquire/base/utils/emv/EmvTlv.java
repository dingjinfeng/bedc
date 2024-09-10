package acquire.base.utils.emv;

import java.util.Arrays;

/**
 * EmvTLV
 *
 * @author hechun
 */
public class EmvTlv {
    private int tag;
    private int len;
    private byte[] value;


    public int getTag() {
        return tag;
    }


    public void setTag(int tag) {
        this.tag = tag;
    }


    public int getLen() {
        return len;
    }


    public void setLen(int len) {
        this.len = len;
    }


    public byte[] getValue() {
        return value;
    }


    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EmvTLV{" +
                "tag=" + tag +
                ", len=" + len +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}
