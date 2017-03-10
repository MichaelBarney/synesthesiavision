package com.bananadigital.sound3d;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;


public class TextToSpeechManager {

    private TextToSpeech mTTS;
    private Context mContext;

    public TextToSpeechManager(Context context) {
        mContext = context;
    }

    public void createTTS() {
        mTTS = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = mTTS.setLanguage(new Locale("pt", "POR"));

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

    public void speak(String textToSpeech){
        mTTS.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void destroyTTS(){
        if(mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }

}