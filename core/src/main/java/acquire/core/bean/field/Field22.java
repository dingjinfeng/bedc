package acquire.core.bean.field;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import acquire.core.bean.PubBean;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Field22, Pan Entry Mode(N2) + Pin Entry Capability(N1)
 *
 * @author Janson
 * @date 2021/4/6 14:45
 */
public class Field22 {
    /**
     * Entry mode of pan
     */
    private final String panEntryMode;
    /**
     * The flag of pin presence.
     */
    private final String pinFlag;

    public Field22(PubBean pubBean) {
        switch (pubBean.getEntryMode()) {
            case EntryMode.MAG:
                // magnetic
                panEntryMode = "02";
                break;
            case EntryMode.INSERT:
                // insert
                panEntryMode = "05";
                break;
            case EntryMode.TAP:
                // tap
                panEntryMode = "07";
                break;
            case EntryMode.MANUAL:
                // manual
                panEntryMode = "01";
                break;
            default:
                // unspecified
                panEntryMode = "00";
                break;
        }

        String pinBlock = pubBean.getPinBlock();
        if (TextUtils.isEmpty(pinBlock)) {
            // no PIN
            pinFlag = "2";
        } else {
            //has PIN
            pinFlag = "1";
        }
    }

    @Override
    @NonNull
    public String toString() {
        return panEntryMode + pinFlag;
    }
}
