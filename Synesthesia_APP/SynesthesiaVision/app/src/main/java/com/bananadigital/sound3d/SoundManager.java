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

class SoundManager{

    private static final int MAX_STREAMS = 20;
    //Class that will plays the sound
    private SoundPool soundPool;

    //Necessary for SoundPool plays a sound
    private int soundID;
    private int soundID2;
    private int soundID3;

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
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(MAX_STREAMS)
                .build();

        startSoundPool();
    }

    /**
     * Creates an old sound pool.
     */
    @SuppressWarnings("deprecation")
    private void createOldSoundPool(){
        soundPool = new SoundPool(MAX_STREAMS, STREAM_MUSIC, 0);
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
                if(sampleId == soundID2) {
                    soundPool.play(soundID2, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }
                if(sampleId == soundID3) {
                    soundPool.play(soundID2, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }
            }
        });
        soundID = soundPool.load(context, R.raw.bu, 2);
        soundID2 = soundPool.load(context, R.raw.bu, 2);
        soundID3 = soundPool.load(context, R.raw.bu, 2);
    }

    /**
     * Sets the volume.
     *
     * @param      leftVolume   The left volume
     * @param      rightVolume  The right volume
     */
    void setVolume(int ID, float leftVolume, float rightVolume) {
        if(ID == 1) soundPool.setVolume(soundID, leftVolume, rightVolume);
        else if(ID == 2) soundPool.setVolume(soundID2, leftVolume, rightVolume);
        else if(ID == 3) soundPool.setVolume(soundID3, leftVolume, rightVolume);
    }

    /**
     * Sets the rate.
     *
     * @param      soundRate  The sound rate
     */

    void setRate(int ID, float soundRate) {
        if(ID == 1) soundPool.setRate(soundID, soundRate);

        else if(ID == 2) soundPool.setRate(soundID2, soundRate);

        else if(ID == 3) soundPool.setRate(soundID3, soundRate);
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
