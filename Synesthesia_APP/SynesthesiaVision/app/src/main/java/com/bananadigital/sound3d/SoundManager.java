package com.bananadigital.sound3d;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

import static android.media.AudioManager.STREAM_MUSIC;


class SoundManager{

    private static final int MAX_STREAMS = 20;
    //Class that will plays the sound
    private SoundPool soundPool;

    //Necessary for SoundPool plays a sound
    private int soundID;
    private int soundID2;
    private int soundID3;
    private int soundID4;
    private int soundID5;

    //Context that will be created the Soundpool
    private Context context;
    private int soundID6;

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
                    soundPool.play(soundID3, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }
                if(sampleId == soundID4) {
                    soundPool.play(soundID4, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }
                if(sampleId == soundID5) {
                    soundPool.play(soundID5, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }
                if(sampleId == soundID6) {
                    soundPool.play(soundID6, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }

            }
        });
        //Id for all sensors play
        soundID = soundPool.load(context, R.raw.bu, 2);
        soundID2 = soundPool.load(context, R.raw.bu, 2);
        soundID3 = soundPool.load(context, R.raw.bu, 2);
        soundID4 = soundPool.load(context, R.raw.bu, 2);
        soundID5 = soundPool.load(context, R.raw.bu, 2);
        soundID6 = soundPool.load(context, R.raw.bu, 2);
    }

    /**
     * Sets the volume.
     *
     * @param      leftVolume   The left volume
     * @param      rightVolume  The right volume
     */
    void setVolume(int ID, float leftVolume, float rightVolume) {
        //Get the ID and play the respective soundPool
        //Because we need simultaneous sounds playing, so set three sounds ID for three sensors
        if(ID == 1) soundPool.setVolume(soundID, leftVolume, rightVolume);
        else if(ID == 2) soundPool.setVolume(soundID2, leftVolume, rightVolume);
        else if(ID == 3) soundPool.setVolume(soundID3, leftVolume, rightVolume);
        else if(ID == 4) soundPool.setVolume(soundID4, leftVolume, rightVolume);
        else if(ID == 5) soundPool.setVolume(soundID5, leftVolume, rightVolume);
    }


    void setVolume(float leftVolume, float rightVolume) {

        soundPool.setVolume(soundID6, leftVolume, rightVolume);

    }

    /**
     * Sets the rate.
     *
     * @param      soundRate  The sound rate
     */

    void setRate(int ID, float soundRate) {

        //Get the ID and set rate the respective soundPool
        if(ID == 1) soundPool.setRate(soundID, soundRate);
        else if(ID == 2) soundPool.setRate(soundID2, soundRate);
        else if(ID == 3) soundPool.setRate(soundID3, soundRate);
        else if(ID == 4) soundPool.setRate(soundID4, soundRate);
        else if(ID == 5) soundPool.setRate(soundID5, soundRate);
    }

    void setRate(float soundRate) {

        soundPool.setRate(soundID6, soundRate);

    }

    /**
     * Release the SoundPool
     */
    void destroySoundPool() {
        soundPool.release();
    }

    /**
     * Pauses the SoundPool
     */
    void pause() {
        soundPool.autoPause();
    }

    int getStreamID(int ID) {

        if(ID == 1) return soundID;
        else if(ID == 2) return soundID2;
        else if(ID == 3) return soundID3;
        else if(ID == 4) return soundID4;
        else if(ID == 5) return soundID5;
        else if(ID == 6) return soundID6;
        return -1;
    }

    void pause(int ID) {
        soundPool.pause(ID);
    }
    /**
     * Resumes the SoundPool
     */
    void resume() {
        soundPool.autoResume();
    }

    void resume(int ID) {
        soundPool.resume(ID);
    }
}
