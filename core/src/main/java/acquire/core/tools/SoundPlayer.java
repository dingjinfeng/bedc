package acquire.core.tools;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

import androidx.annotation.RawRes;

import acquire.base.BaseApplication;
import acquire.core.R;

/**
 * Sound player
 *
 * @author Janson
 * @date 2021/10/25 11:18
 */
public class SoundPlayer {
    private final SparseIntArray ids = new SparseIntArray();
    private SoundPool soundPool;

    private static volatile SoundPlayer instance;

    private SoundPlayer() {
    }

    public static SoundPlayer getInstance() {
        if (instance == null) {
            synchronized (SoundPlayer.class) {
                if (instance == null) {
                    instance = new SoundPlayer();
                }
            }
        }
        return instance;
    }

    public void init() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_SYSTEM)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        ids.put(R.raw.click_keyboard, soundPool.load(BaseApplication.getAppContext(), R.raw.click_keyboard, 1));
        ids.put(R.raw.ding, soundPool.load(BaseApplication.getAppContext(), R.raw.ding, 1));
    }

    public void playScan() {
        play(R.raw.ding);
    }

    public void playClick() {
        play(R.raw.click_keyboard);
    }

    private void play(@RawRes int resId) {
        if (soundPool == null) {
            init();
        }
        int id = ids.get(resId);
        soundPool.play(id, 1, 1, 0, 0, 1);
    }
} 
