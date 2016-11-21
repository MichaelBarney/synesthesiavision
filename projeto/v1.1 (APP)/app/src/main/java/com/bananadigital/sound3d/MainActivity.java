package com.bananadigital.sound3d;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    MediaPlayer mp;
    Context c;
    Switch s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bt_connect.blueThread.setHandler(mHandler);
        c = this;
        setContentView(R.layout.activity_main);
        s = (Switch)findViewById(R.id.switch1);

        //prepareMedia();
    }
    //constant for message read
    private static final int MESSAGE_READ = 3;
    //bluetooth comunication variables
    char msgType = '*'; //c = CurrentComponent

    //message handler
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case MESSAGE_READ:
                    String readMessage = msg.obj.toString();
                    handleMsg(readMessage);
                    break;
            }
        }
    };

    String distance = "";
    char sensor = '*';
    //handle all messages from bluetooth
    void handleMsg(String r){
        char[] c = r.toCharArray();

        for(int i = 0; i < c.length; i++){
            if(Character.isDigit(c[i])){
                distance += c[i];
            }
            else if(Character.isLetter(c[i])){
                if(distance != "") {
                    playAudio(sensor, Integer.parseInt(distance));
                    distance = "";
                }
                sensor = c[i];
            }
        }
    }
    String last_file;

    public void playAudio(char s, int d){
        String file = "";

        if(s == 'b')file += "f";
        else if(s == 'a')file += "e";
        else if(s == 'b')file += "d";

        if(d <= 50) file += "1";
        else if(d <= 100) file += "2";
        else if(d <= 150) file += "3";
        else if(d <= 200) file += "4";
        else if(d <= 250) file += "5";
        else if(d <= 300) file += "6";
        else if (d < 400)file += "7";

        else file = "";

        //só para frente
        if(file.contains("f")) {
            Log.e("", "distance " + d);
            //Log.e("", "last file: " + last_file);
            mpPlay(file);
        }

    }
    String l_file;

    public void mpPlay(String file){
        l_file = file;

        if(mp == null) {
            changeAudio();
        }
    }
    void changeAudio(){
        int id = getResources().getIdentifier(l_file, "raw", getPackageName());
        if(mp != null) {
            mp.stop();
            mp.release();
        }
        mp = MediaPlayer.create(c, id);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                changeAudio();
            }
        });
    }
    public void play(View v) {
        if (mp != null) {
            mp.stop();
            mp.release();
            Log.e("", "YOU SHALL STOP");
        }
        if(s.isChecked()) {
            if (v == findViewById(R.id.button)) {
                mp = MediaPlayer.create(c, R.raw.f1);
                Log.e("", "frente");
            } else if (v == findViewById(R.id.button5)) {
                mp = MediaPlayer.create(c, R.raw.d1);
                Log.e("", "direita");
            } else if (v == findViewById(R.id.button6)) {
                mp = MediaPlayer.create(c, R.raw.e1);
                Log.e("", "esuqerda");
            }

        }

        else {
            if (v == findViewById(R.id.button)) {
                mp = MediaPlayer.create(c, R.raw.f2);
                Log.e("", "frente");
            } else if (v == findViewById(R.id.button5)) {
                mp = MediaPlayer.create(c, R.raw.d2);
                Log.e("", "direita");
            } else if (v == findViewById(R.id.button6)) {
                mp = MediaPlayer.create(c, R.raw.e2);
                Log.e("", "esuqerda");
            }
        }
        mp.start(); // no need to call prepare(); create() does that for you
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
