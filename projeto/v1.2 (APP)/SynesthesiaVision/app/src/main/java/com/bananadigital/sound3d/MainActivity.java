package com.bananadigital.sound3d;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
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
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.STREAM_MUSIC;

//Modificado para 3 Sensores

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity" ;
    private static final String TAG1 = "Teste1";
    private static final String TAG2 = "Teste2";
    private static final char DELIMITER = '\n';
    Context c;

    SoundPool soundPool;
    SoundPool sP;
    private int sf;

    Timer timer = new Timer();
    int time = 250; //miliseconds
    TimerTask task;

    private Handler writeHandler;

    int n_s = 3; //number of sensors
    int c_s = 0; //current sensor

    float[] ds = new float[n_s];

    //Layout
    SeekBar seek;
    Button button;
    TextView ps;
    TextView total;
    TextView valor_f;
    TextView valor_e;
    TextView valor_d;
    CheckBox check;
    TextView volume_f;
    TextView volume_e;
    TextView volume_d;
    float fv;
    float ev;
    float dv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        writeHandler = ConnectBluetooth.btt.getWriteHandler();
        ConnectBluetooth.btt.setReadHandler(readHandler);
        setContentView(R.layout.activity_main);

        //sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                                .setAudioAttributes(new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_UNKNOWN)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                                        .build())
                                .setMaxStreams(10)
                                .build();
        } else {
            soundPool = new SoundPool(10, STREAM_MUSIC, 0);
        }


        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(sampleId == sf){
                    soundPool.play(sf, 0, 0, 0, -1, 1.0f); //id, //volume E// volume D // Prioridade// loop // rate
                }
            }
        });
        sf = soundPool.load(this, R.raw.bu, 2);


        //Layout
        //valor da frente
        /*valor_f = (TextView) findViewById(R.id.valor_f);
        valor_e = (TextView) findViewById(R.id.valor_e);
        valor_d = (TextView) findViewById(R.id.valor_d);

        volume_f = (TextView) findViewById(R.id.volume_f);
        volume_e = (TextView) findViewById(R.id.volume_e);
        volume_d = (TextView) findViewById(R.id.volume_d);
        */
        //Button
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                turnOnTimer();
                timer.schedule(task, 0, time);
                Log.d(TAG, "Clicked!");
            }
        });

        //CheckBox
        check = (CheckBox) findViewById(R.id.checkBox);
        //SeeBar
        seek = (SeekBar) findViewById(R.id.frequency);
        seek.setProgress(time);
        ps = (TextView) findViewById(R.id.ps);
        ps.setText("" + time);
        total = (TextView) findViewById(R.id.total);
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        soundPool.pause(sf);
    }

    private void stopTimer() {
        timer.cancel();
        timer.purge();
        timer = null;
        task.cancel();
        task = null;
        timer = new Timer();
    }

    private void turnOnTimer() {
        task = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Timer: " + c_s);
                c_s ++;
                if(c_s == n_s) {
                    c_s = 0;
                }
                playAudio(c_s);
            }
        };
    }




    int dmax = 200;
    public void playAudio(int s) {
        //calcula a itensidade proporcional de p para a distancia
        float v;
        float d = ds[s];

        Log.d(TAG, "" + String.valueOf(d));

        if(d <= dmax) v =  1 - (d/dmax);
        else{v = 0.01f;}

        // 0 == LEFT
        // 1 == FRONT
        // 2 == RIGHT

        //LEFT
        if(s == 2){
            soundPool.setVolume(sf, v, 0);
            soundPool.setRate(sf, 0.6f);
            ev = v;
            Log.d(TAG, "RIGHT " + d + " " + v);
        }
        //FRONT
        else if(s == 1){
            soundPool.setVolume(sf, v/2, v/2);
            soundPool.setRate(sf, 1.0f);
            fv = v/2;
            Log.d(TAG, "FRONT " + d + " " + v);
        }
        //RIGHT
        else if(s == 0){
            soundPool.setVolume(sf, 0, v);
            soundPool.setRate(sf, 1.4f);
            dv = v;
            Log.d(TAG, "LEFT " + d + " " + v);
        }
    }


    private void saveAudio(char s, float d) {

        //Log.d(TAG, "Audio Saved");
        //LEFT
        if(s == 'a'){
            ds[0] = d;
        }
        //FRONT
        else if(s == 'c'){
            ds[1] = d;
        }
        //RIGHT
        else if(s == 'e'){
            ds[2] = d;
        }
    }
    ////-----HANDLER---

    String distance = "";
    char sensor = '*';

    private void handleMsg(String r) {

        int inx = r.indexOf(DELIMITER);
        try {
            String sensor1 = r.substring(0, 1);
            distance = r.substring(1, inx);

            sensor = sensor1.charAt(0);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Erro na Array");
        }


        //Log.d(TAG,"[SENSOR]: " + sensor + " [DISTANCE]: "+ distance);

        if (!distance.isEmpty()) {

            try {
                saveAudio(sensor, Float.valueOf(distance));
            } catch (NumberFormatException e) {
                Log.e(TAG, " [Incorrect Number Format]: " + e + " [Number]: " + distance);
            }
            if (sensor == 'c') {
            //valor_f.setText("" + distance);
            //volume_f.setText(String.format("%.02f", fv));
            } else if (sensor == 'a') {
                //valor_d.setText("" + distance);
                //volume_d.setText(String.format("%.02f", dv));
            } else if (sensor == 'e') {
                //valor_e.setText("" + distance);
                //volume_e.setText(String.format("%.02f", ev));
            }
            distance = "";
        }


    }


    Handler readHandler = new Handler () {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String s = msg.obj.toString();

            //Log.i(TAG, "[RECV IN MAIN]: " + s);
            //handleMsg(s);
            s += DELIMITER;
            handleMsg(s);
            if (s.equals("DISCONNECT")) {
                Toast.makeText(getApplicationContext(),"Desconectado", Toast.LENGTH_SHORT).show();
                ConnectBluetooth.btt.write("DISCONNECTED");
            }
        }

    };

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
}
