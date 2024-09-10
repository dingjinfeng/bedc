package acquire.core.fragment.pin;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.tools.PinpadHelper;
import acquire.core.tools.SoundPlayer;
import acquire.sdk.device.BDevice;
import acquire.sdk.pin.listener.PinpadListener;

/**
 * PIN view model
 *
 * @author Janson
 * @date 2022/8/26 16:11
 */
public class PinViewModel extends ViewModel {
    private final MutableLiveData<byte[]> pinBlock = new MutableLiveData<>();
    private final MutableLiveData<Integer> inputLength = new MutableLiveData<>();
    private final MutableLiveData<PinError> error = new MutableLiveData<>();

    private boolean isFinish;
    private final PinpadHelper pinpad = new PinpadHelper();

    public LiveData<byte[]> getPinBlock() {
        return pinBlock;
    }

    public LiveData<Integer> getInputLength() {
        return inputLength;
    }

    public LiveData<PinError> getError() {
        return error;
    }

    public void setRandomKeyboard(List<View> numberKeyViews, List<View> funcKeyViews) {
        //button coordinates
        List<int[]> numberCoords = getCoords(numberKeyViews);
        List<int[]> funcCoords = getCoords(funcKeyViews);
        //button layout bytes
        byte[] numLayout = packNumLayout(numberCoords);
        byte[] funclayout = packFuncLayout(funcCoords);
        //set button to PIN pad
        byte[] randomKeyCodes = pinpad.setPinpadLayout(numLayout, funclayout, true);
        if (randomKeyCodes == null) {
            //generate random keyboard failed
            LoggerUtils.e("Random pinpad generation failed.");
            error.postValue(new PinError(PinError.FAIL, BaseApplication.getAppString(R.string.core_pin_random_error)));
            return;
        }
        //Set PIN pad layouts bytes to keyboard number button
        for (int i = 0; i < randomKeyCodes.length; i++) {
            String codeText = Character.toString((char) randomKeyCodes[i]);
            ((TextView) numberKeyViews.get(i)).setText(codeText);
        }

    }

    public void startPin(boolean onlinePin, String pan, byte[] supportLens) {
        isFinish = false;
        //input PIN
        ThreadPool.execute(() ->
                pinpad.startPinInput(onlinePin, pan, supportLens, new PinpadListener() {
                    @Override
                    public void onCancel() {
                        isFinish = true;
                        if (!BDevice.supportPhysicalKeyboard()) {
                            SoundPlayer.getInstance().playClick();
                        }
                        error.postValue(new PinError(PinError.CANCEL, BaseApplication.getAppString(R.string.core_pin_cancel)));
                    }

                    @Override
                    public void onError(int errorCode, String errorDescription) {
                        isFinish = true;
                        error.postValue(new PinError(PinError.FAIL, BaseApplication.getAppString(R.string.core_pin_error_format,errorCode,errorDescription)));
                    }

                    @Override
                    public void onKeyDown(int len) {
                        if (!BDevice.supportPhysicalKeyboard()) {
                            SoundPlayer.getInstance().playClick();
                        }
                        inputLength.postValue(len);
                    }

                    @Override
                    public void onPinRslt(byte[] pin) {
                        isFinish = true;
                        pinBlock.postValue(pin);
                    }
                })
        );
    }

    public void cancelPin() {
        if (!isFinish){
            LoggerUtils.e("invoke cancel PIN");
            pinpad.cancelPinInput();
            isFinish = true;
        }
    }

    /**
     * get the view coordinates. Every element is int[4]{leftTopX,leftTopY,rigthBottomX,rightBottomY}
     */
    private List<int[]> getCoords(List<View> views) {
        List<int[]> coordList = new ArrayList<>();
        for (View view : views) {
            int[] local = new int[2];
            view.getLocationOnScreen(local);
            int leftTopX = local[0];
            int leftTopY = local[1];
            int rightBottomX = local[0] + view.getWidth();
            int rightBottomY = local[1] + view.getHeight();
            int[] coords = new int[4];
            coords[0] = leftTopX;
            coords[1] = leftTopY;
            coords[2] = rightBottomX;
            coords[3] = rightBottomY;
            coordList.add(coords);
        }
        return coordList;
    }

    /**
     * Convert button coordinates to  PIN pad number button bytes.
     *
     * @param numCoords number button coordinates. It is from {@link #getCoords(List)}.
     * @return PIN pad number button bytes
     */
    private byte[] packNumLayout(@NonNull List<int[]> numCoords) {
        //All number key coordiates
        byte[] coords = new byte[numCoords.size() * 8];
        for (int i = 0; i < numCoords.size(); i++) {
            //A key coordiates
            int[] keyCoord = numCoords.get(i);
            int start = i * 8;
            //Left top X
            coords[start] = (byte) (keyCoord[0] & 0xff);
            coords[start + 1] = (byte) ((keyCoord[0] >> 8) & 0xff);
            //Left top Y
            coords[start + 2] = (byte) (keyCoord[1] & 0xff);
            coords[start + 3] = (byte) ((keyCoord[1] >> 8) & 0xff);
            //Right bottom X
            coords[start + 4] = (byte) (keyCoord[2] & 0xff);
            coords[start + 5] = (byte) ((keyCoord[2] >> 8) & 0xff);
            //Right bottom Y
            coords[start + 6] = (byte) (keyCoord[3] & 0xff);
            coords[start + 7] = (byte) ((keyCoord[3] >> 8) & 0xff);

        }
        LoggerUtils.d("number key[" + coords.length + "]:" + Arrays.toString(coords));
        return coords;
    }

    /**
     * Convert button coordinates to PIN pad function button bytes.
     *
     * @param funcCoords function button coordinates. It is from {@link #getCoords(List)}.
     * @return PIN pad function button bytes
     */
    private byte[] packFuncLayout(@NonNull List<int[]> funcCoords) {
        //All function key coordiates
        byte[] coords = new byte[funcCoords.size() * 12];
        for (int i = 0; i < funcCoords.size(); i++) {
            //A key coordiates
            int[] keyCoord = funcCoords.get(i);
            int start = i * 12;
            switch (i) {
                case 2:
                    //enter
                    coords[start] = 0x0D;
                    coords[start + 1] = 0x00;
                    coords[start + 2] = 0x00;
                    coords[start + 3] = 0x00;
                    break;
                case 0:
                    //backspace
                    coords[start] = 0x0A;
                    coords[start + 1] = 0x00;
                    coords[start + 2] = 0x00;
                    coords[start + 3] = 0x00;
                    break;
                case 1:
                default:
                    //cancel
                    coords[start] = 0x1B;
                    coords[start + 1] = 0x00;
                    coords[start + 2] = 0x00;
                    coords[start + 3] = 0x00;
                    break;
            }
            //Left top X
            coords[start + 4] = (byte) (keyCoord[0] & 0xff);
            coords[start + 5] = (byte) ((keyCoord[0] >> 8) & 0xff);
            //Left top Y
            coords[start + 6] = (byte) (keyCoord[1] & 0xff);
            coords[start + 7] = (byte) ((keyCoord[1] >> 8) & 0xff);
            //Right bottom X
            coords[start + 8] = (byte) (keyCoord[2] & 0xff);
            coords[start + 9] = (byte) ((keyCoord[2] >> 8) & 0xff);
            //Right bottom Y
            coords[start + 10] = (byte) (keyCoord[3] & 0xff);
            coords[start + 11] = (byte) ((keyCoord[3] >> 8) & 0xff);
        }
        LoggerUtils.d("function key[" + coords.length + "]: " + Arrays.toString(coords));
        return coords;
    }

    public static class PinError {
        public final static int FAIL = -1, CANCEL = -2;
        private final int errorCode;
        private final String description;

        public PinError(int error, String description) {
            this.errorCode = error;
            this.description = description;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getDescription() {
            return description;
        }
    }
}
