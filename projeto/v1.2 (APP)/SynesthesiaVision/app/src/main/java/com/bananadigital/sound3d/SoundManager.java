package com.bananadigital.sound3d;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

import static android.media.AudioManager.STREAM_MUSIC;

/**
 * Created by Jonathan on 10/03/2017.
 */

public class SoundManager {

    private SoundPool soundPool;
    private int soundID;
    private Context context;

    public SoundManager(Context context) {
        this.context = context;
    }

    public void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();

        startSoundPool();
    }

    @SuppressWarnings("deprecation")
    private void createOldSoundPool(){
        soundPool = new SoundPool(10, STREAM_MUSIC, 0);
        startSoundPool();
    }

    private void startSoundPool() {
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(sampleId == soundID){
                    soundPool.play(soundID, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }
            }
        });
        soundID = soundPool.load(context, R.raw.bu, 2);
    }

    public void setVolume(float leftVolume, float rightVolume) {
        soundPool.setVolume(soundID, leftVolume, rightVolume);
    }

    public void setRate(float soundRate) {
        soundPool.setRate(soundID, soundRate);
    }

    public void destroySoundPool() {
        soundPool.release();
    }



}
