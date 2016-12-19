package com.bananadigital.sound3d;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Timer";

    Context c;

    SoundPool soundPool;
    private int sf;

    Timer timer = new Timer();
    int time = 250; //miliseconds
    TimerTask task;

    int n_s = 5; //number of sensors
    int c_s = 0; //current sensor

    int[] ds = new int[n_s];

    boolean running = false;

    //Layout
    SeekBar seek;
    Button button;
    Button setValue;
    EditText ps;
    TextView total;
    TextView valor_f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        bt_connect.blueThread.setHandler(mHandler);
        c = this;
        setContentView(R.layout.activity_main);

        //sound
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(sampleId == sf){
                    soundPool.play(sf, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }
            }
        });
        sf = soundPool.load(this, R.raw.bu, 2);

        //timer
        turnOnTimer();

        //Layout
        //valor da frente
        valor_f = (TextView) findViewById(R.id.valor_f);

        //EditText
        ps = (EditText) findViewById(R.id.ps);
        total = (TextView) findViewById(R.id.total);

        //Button
        button = (Button) findViewById(R.id.button);
        setValue = (Button) findViewById(R.id.setValue);
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(time != 0 && !running) {
                    timer.schedule(task, 0, time);
                    running = true;
                } else if (time == 0) {
                    Toast.makeText(getApplicationContext(), "Valor Incorreto",
                                                                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Aplicação já iniciada",
                                                                        Toast.LENGTH_LONG).show();
                }
            }
        });

        setValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timer = ps.getText().toString();
                time = Integer.parseInt(timer);
                seek.setProgress(time);
                stopTimer();
                turnOnTimer();
            }
        });

        //SeekBar
        seek = (SeekBar) findViewById(R.id.frequency);
        seek.setProgress(time);
        ps.setText("" + time);
        total.setText("" + ((float)(time * n_s)/1000));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress != 0) {
                    time = progress;
                    ps.setText("" + time);
                    total.setText("" + ((float) (time * n_s) / 1000));
                } else {
                    Toast.makeText(getApplicationContext(), "Valor Inválido",
                                                                        Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopTimer();
                Log.d(TAG, "Started");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                turnOnTimer();
                Log.d(TAG, "Stopped");
            }
        });
    }

    private void stopTimer() {
        timer.cancel();
        timer.purge();
        timer = null;
        task.cancel();
        task = null;
        running = false;
        timer = new Timer();
    }

    private void turnOnTimer() {
        task = new TimerTask() {
            @Override
            public void run() {
                //Log.e("", "Timer: " + c_s);
                c_s ++;
                if(c_s == n_s) c_s = 0;
                playAudio(c_s);
            }
        };
    }


    int dmax = 200;
    public void playAudio(int s) {
        //calcula a itensidade proporcional de p para a ditancia
        float v;
        int d = ds[s];

        if(d <= dmax){v = (float) 1 - ((float) d/dmax);}
        else{v = 0.01f;}

        /*//calcula o volume e rate para cada lado
        float vr = (v/(n_s - 1)) * s;
        float vl = v - vr;
        float rate =  (((float) 1 / (n_s - 1))*s) + 0.5f;
        soundPool.setVolume(sf, vl, vr);
        soundPool.setRate(sf, rate);
        Log.e("", "volume: " + v);
        Log.e("", "volume left: " + vl);
        Log.e("", "volume right: " + vr);
        Log.e("", "rate: " + rate);
        */


        //LEFT
        if(s == 0){
            soundPool.setVolume(sf, v, 0);
            soundPool.setRate(sf, 1.4f);
            Log.d(TAG, "LEFT: D = " + d + " V = " + v);
        }
        //Top Left
        else if(s == 1){
            soundPool.setVolume(sf, (v * 3) / 4, v / 4);
            soundPool.setRate(sf, 1.2f);
        }
        //Front
        else if(s == 2){
            soundPool.setVolume(sf, v/2, v/2);
            soundPool.setRate(sf, 1.0f);
            Log.d(TAG, "FRONT: D = " + d + " V = " + v);
        }
        //Top Right
        else if(s == 3){
            soundPool.setVolume(sf, v / 4, (v * 3) / 4);
            soundPool.setRate(sf, 0.8f);
        }
        //RIGHT
        else if(s == 4){
            soundPool.setVolume(sf, 0, v);
            soundPool.setRate(sf, 0.6f);
            Log.d(TAG, "RIGHT: D = " + d + " V = " + v);
        }
    }


    public void saveAudio(char s, int d) {
        //LEFT - RIGHT
        if(s == 'a'){
            ds[4] = d;
        }
        else if(s == 'b'){
            ds[3] = d;
        }
        else if(s == 'c'){
            ds[2] = d;
        }
        else if(s == 'd'){
            ds[1] = d;
        }
        else if(s == 'e'){
            ds[0] = d;
        }
    }
    ////-----HANDLER---

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
        for (char aC : c) {
            if (Character.isDigit(aC)) {
                distance += aC;
            } else if (Character.isLetter(aC)) {
                if (distance != "") {
                    saveAudio(sensor, Integer.parseInt(distance));
                    if (sensor == 'c') {
                        valor_f.setText("" + distance);
                    }
                    distance = "";
                }
                sensor = aC;
            }
        }
    }



    ///// ---------EXTRAS----------------
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
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        soundPool.pause(sf);
        Log.d(TAG, "PAUSED");
        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.
    }
}
