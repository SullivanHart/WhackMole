package com.example.whackamole;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

/**
 * Simple wrapper for SoundPool to play short sound effects.
 */
public class SoundManager {
    private SoundPool soundPool;
    private int spawnSoundId = 0;
    private int hitSoundId = 0;
    private boolean loaded = false;

    public SoundManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attrs)
                    .setMaxStreams(4)
                    .build();
        } else {
            // deprecated constructor for older devices
            soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        }

        // load sounds from res/raw (IDs must match files you added)
        spawnSoundId = soundPool.load(context, R.raw.spawn, 1);
        hitSoundId   = soundPool.load(context, R.raw.hit, 1);

        // optional: mark as loaded after short delay or use soundPool.setOnLoadCompleteListener
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool sp, int sampleId, int status) {
                if (status == 0) loaded = true;
            }
        });
    }

    public void playSpawn() {
        if (soundPool == null || spawnSoundId == 0) return;
        float vol = 1.0f;
        soundPool.play(spawnSoundId, vol, vol, /*priority*/1, /*loop*/0, /*rate*/1f);
    }

    public void playHit() {
        if (soundPool == null || hitSoundId == 0) return;
        float vol = 1.0f;
        soundPool.play(hitSoundId, vol, vol, 1, 0, 1f);
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
