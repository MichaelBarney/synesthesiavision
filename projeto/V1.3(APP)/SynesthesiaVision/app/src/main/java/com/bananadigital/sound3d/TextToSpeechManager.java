package com.bananadigital.sound3d;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;


public class TextToSpeechManager {

    private TextToSpeech mTTS;
    private Context mContext;

    TextToSpeechManager(Context context) {
        this.mContext = context;
        createTTS();
    }

    private void createTTS() {

        //Creates an instance of Google API TTS
        mTTS = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                
                //If initializaation is sucessful so set language.
                if (status == TextToSpeech.SUCCESS) {

                    //Set language to PT-BR and get the value returned
                    int result = mTTS.setLanguage(new Locale("pt", "br"));

                    //Check if the language is supported, if not print it on Android Monitor.
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TextToSpeechManager", "This Language is not supported");
                    }

                } else {
                    Log.e("TextToSpeechManager", "Initilization Failed!");
                }
            }
        });
    }

    /**
     * Make TTS speak the specified text.
     *
     * @param      textToSpeech  The text to speech
     */
    public void speak(String textToSpeech){
        mTTS.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Destroy the instance of TTS made.
     */
    public void destroyTTS(){
        if(mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }

}