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

    //Class that will plays the sound
    private SoundPool soundPool;

    //Necessary for SoundPool plays a sound
    private int soundID;

    //Context that will be created the Soundpool
    private Context context;

    /**
     * Constructs the object.
     *
     * @param      context  Receives a context from the class that created it
     */
    SoundManager(Context context) {
        this.context = context;
        createSoundPool();
    }

    /**
     * Creates a sound pool.
     */
    private void createSoundPool() {
        //If Android Version is new, create the new soundPool, else create oldSoundPool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    /**
     * Creates a new sound pool.
     */
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

    /**
     * Creates an old sound pool.
     */
    @SuppressWarnings("deprecation")
    private void createOldSoundPool(){
        soundPool = new SoundPool(10, STREAM_MUSIC, 0);
        startSoundPool();
    }

    /**
     * Starts the sound pool.
     */
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

    /**
     * Sets the volume.
     *
     * @param      leftVolume   The left volume
     * @param      rightVolume  The right volume
     */
    public void setVolume(float leftVolume, float rightVolume) {
        soundPool.setVolume(soundID, leftVolume, rightVolume);
    }

    /**
     * Sets the rate.
     *
     * @param      soundRate  The sound rate
     */
    public void setRate(float soundRate) {
        soundPool.setRate(soundID, soundRate);
    }

    /**
     * Release the SoundPool
     */
    public void destroySoundPool() {
        soundPool.release();
    }

    /**
     * Pauses the SoundPool
     */
    public void pause() {
        soundPool.autoPause();
    }

    /**
     * Resumes the SoundPool
     */
    public void resume() {
        soundPool.autoResume();
    }

}
