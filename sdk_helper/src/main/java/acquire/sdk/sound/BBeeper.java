package acquire.sdk.sound;

import androidx.annotation.IntRange;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.beeper.Beeper;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

/**
 * POS beeper
 *
 * @author Janson
 * @date 2021/6/10 16:30
 */
public class BBeeper {

    /**
     * beeps
     *
     * @param frequency Set beep frequency in Hz
     * @param duration  Set how long to beep in ms.
     */
    public static void beep(@IntRange(from = 1, to = 4000) int frequency, @IntRange(from = 1) int duration) {
        try {
            Beeper beeper = (Beeper) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.BEEPER);
            beeper.beep(frequency, duration);
        } catch (NSDKException e) {
            e.printStackTrace();
        }
    }

} 
