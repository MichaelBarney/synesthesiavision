package com.bananadigital.sound3d;

import android.annotation.TargetApi;
import android.content.Intent;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.STREAM_MUSIC;

//Modificado para 3 Sensores

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity" ;
    private static final char DELIMITER = '\n';

    //Variables
    private SoundPool soundPool;
    private int soundID;

    private Timer timer;
    private int time = 100; //miliseconds
    private TimerTask task;
    private Boolean init = false;

    private Handler writeHandler;

    private TextView tempo_total;
    private TextView tempo_sensor;

    private EditText edtTempo;

    private int n_s = 5; //number of sensors
    private int c_s = 0; //current sensor

    float[] ds = new float[n_s];
    float frente;
    float esquerda;
    float direita;
    float frenteEsquerda;
    float frenteDireita;

    private CheckBox chkFrente;
    private CheckBox chkEsquerda;
    private CheckBox chkDireita;
    private CheckBox chkFrenteDireita;
    private CheckBox chkFrenteEsquerda;

    private TextToSpeechManager mTTS;
    private SoundManager soundManager;

    private boolean logarithm;

    /*
    private Vibrator vibrator;
    long [] vibratorFrente = {0, 100, 50};
    long [] vibratorEsquerda = {0, 150, 50};
    long [] vibratorDireita = {0, 200, 50};
    */

    /*TextView ps;
    TextView valor_f;
    TextView valor_e;
    TextView valor_d;
    CheckBox check;
    TextView volume_f;
    TextView volume_e;
    TextView volume_d;*/
    private float fv;
    private float ev;
    private float dv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        writeHandler = ConnectBluetooth.btt.getWriteHandler();
        ConnectBluetooth.btt.setReadHandler(readHandler);
        setContentView(R.layout.activity_main);


        //sound
        soundManager = new SoundManager(MainActivity.this);
        soundManager.createSoundPool();
        //createSoundPool();

        mTTS = new TextToSpeechManager(MainActivity.this);

        chkDireita = (CheckBox) findViewById(R.id.chkDireita);
        chkEsquerda = (CheckBox) findViewById(R.id.chkEsquerda);
        chkFrente = (CheckBox) findViewById(R.id.chkFrente);
        chkFrenteEsquerda = (CheckBox) findViewById(R.id.chkFrenteEsquerda);
        chkFrenteDireita = (CheckBox) findViewById(R.id.chkFrenteDireita);

        edtTempo = (EditText) findViewById(R.id.edtTempo);

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
        Button button_start = (Button) findViewById(R.id.button_start);
        Button button_disconnect = (Button) findViewById(R.id.btn_disconnect);
        Button btnAplicar = (Button) findViewById(R.id.btnOk);
        Button btnfreqUnic = (Button) findViewById(R.id.freq_unic);
        Button btnfreqVar = (Button) findViewById(R.id.freq_var);
        ToggleButton toggleModo = (ToggleButton) findViewById(R.id.toggleModo);

        button_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(!init) {
                    mTTS.speak("Iniciando sonorização");
                    turnOnTimer();
                    timer = new Timer();
                    timer.schedule(task, 0, time);
                    Log.d(TAG, "Clicked!");
                }
            }
        });
        button_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        btnAplicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                if(!edtTempo.getText().toString().equals("")) {
                    time = Integer.parseInt(edtTempo.getText().toString());
                }
                Log.d("TEMPO", "Tempo ajustado para: " + time);
            }
        });

        //Seta a frequenciapara
        btnfreqUnic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frequenciaDireita = 1.0f;
                frenteEsquerda = 1.0f;
                frequenciaFrente = 1.0f;
                frequenciaFrenteDireita = 1.0f;
                frequenciaFrenteEsquerda = 1.0f;
            }
        });

        btnfreqVar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frequenciaFrente = 2.0f;
                frequenciaDireita = 1.4f;
                frequenciaEsquerda = 1.4f;
                frequenciaFrenteDireita = 1.2f;
                frequenciaFrenteEsquerda = 1.2f;
            }
        });

        toggleModo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                logarithm = isChecked;
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
        //soundPool.release();
        if(soundManager != null) soundPool.release();
    }
    /*
    protected void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createNewSoundPool(){
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
    protected void createOldSoundPool(){
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
        soundID = soundPool.load(this, R.raw.bu, 2);
    }*/

    private void stopTimer() {
        if(timer != null && task != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            task.cancel();
            task = null;
            Log.d(TAG, "Parado");
        }
    }

    private void turnOnTimer() {
        task = new TimerTask() {
            @Override
            public void run() {
                //Log.d(TAG, "Timer: " + c_s);
                c_s ++;
                if(c_s == n_s) {
                    c_s = 0;
                }
                playAudio(c_s);
                //playAudioV2();
                Log.d("TEMPO", "tempo: " + time);
            }
        };
        Log.d(TAG, "Iniciado");
    }
    float dmax = 400;
    float frequenciaFrente = 1.0f;
    float frequenciaDireita = 1.0f;
    float frequenciaEsquerda = 1.0f;
    float frequenciaFrenteDireita = 1.2f;
    float frequenciaFrenteEsquerda = 1.2f;

    private void playAudio(int s) {
        //calcula a itensidade proporcional de p para a distancia
        float v = 0;
        float d = ds[s];
        //float d = (float) Math.log(ds[s])/Math.log();
        //Log.d(TAG, "" + String.valueOf(d));
        float base = 0.1f;

        if(logarithm) {
            if (ds[s] < dmax) {
                double d1 = d / 100;
                v = (float) ((float) 1 - (2 * (Math.log(d1) / Math.log(base)) + 0.5));
            }
            else v = 0.01f;
            //Toast.makeText(this, "logaritmo", Toast.LENGTH_SHORT).show();
        } else {
            if (d < dmax) v = 1 - (d / dmax);

            if(d < 50) {
                frequenciaDireita = 2.0f;
                frequenciaEsquerda = 2.0f;
                frequenciaFrente = 2.0f;
            } else if(d < 100 && d >= 50) {
                frequenciaDireita = 1.8f;
                frequenciaEsquerda = 1.8f;
                frequenciaFrente = 1.8f;
            } else if(d >= 100) {
                frequenciaDireita = 1.6f;
                frequenciaEsquerda = 1.6f;
                frequenciaFrente = 1.6f;
            }


            else {
                v = 0.01f;
            }
        }

        //Left
        if(s == 0 && chkEsquerda.isChecked()){
            soundManager.setVolume(v, 0);
            soundManager.setRate(frequenciaEsquerda);
            //soundPool.setVolume(soundID, v, 0);
            //soundPool.setRate(soundID, frequenciaEsquerda);
            Log.d(TAG, "LEFT: D = " + d + " V = " + v);
        }
        //Top Left
        else if(s == 1 && chkFrenteEsquerda.isChecked()){
            soundManager.setVolume((v * 3) / 4, v / 4);
            soundManager.setRate(frequenciaFrenteEsquerda);
            //soundPool.setVolume(soundID, (v * 3) / 4, v / 4);
            //soundPool.setRate(soundID, frequenciaFrenteEsquerda);
        }
        //Front
        else if(s == 2 && chkFrente.isChecked()){
            soundManager.setVolume(v/2, v/2);
            soundManager.setRate(frequenciaFrente);
            //soundPool.setVolume(soundID, v/2, v/2);
            //soundPool.setRate(soundID, frequenciaFrente);
            Log.d(TAG, "FRONT: D = " + d + " V = " + v);
        }
        //Top Right
        else if(s == 3 && chkFrenteDireita.isChecked()){
            soundManager.setVolume(v / 4, (v * 3) / 4);
            soundManager.setRate(frequenciaFrenteDireita);
            //soundPool.setVolume(soundID, v / 4, (v * 3) / 4);
            //soundPool.setRate(soundID, frequenciaFrenteDireita);
        }
        //RIGHT
        else if(s == 4 && chkDireita.isChecked()){
            soundManager.setVolume(0, v);
            soundManager.setRate(frequenciaDireita);
            //soundPool.setVolume(soundID, 0, v);
            //soundPool.setRate(soundID, frequenciaDireita);
            Log.d(TAG, "RIGHT: D = " + d + " V = " + v);
        }

    }


    private void saveAudio(char s, float d) {

        //Right
        if(s == 'a'){
            ds[4] = d;
        }
        else if(s == 'b'){
            ds[3] = d;
        }
        //Front
        else if(s == 'c'){
            ds[2] = d;
        }
        else if(s == 'd'){
            ds[1] = d;
        }
        //Left
        else if(s == 'e'){
            ds[0] = d;
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
            saveAudio(sensor, Float.valueOf(distance));
        }


    }


    Handler readHandler = new Handler () {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String received = msg.obj.toString();
            if (received.equals("DISCONNECT")) {
                Toast.makeText(getApplicationContext(),"Desconectado", Toast.LENGTH_SHORT).show();
                ConnectBluetooth.btt.write("DISCONNECTED");
            } else {
                received += DELIMITER;
                handleMsg(received);
            }
        }

    };

    public void disconnect() {
        ConnectBluetooth.btt.write("DISCONNECTED");
        if(ConnectBluetooth.btt != null) {
            ConnectBluetooth.btt.interrupt();
            ConnectBluetooth.btt = null;
            startConnectBluetooth();
        }
    }


    private void startConnectBluetooth(){

        Toast.makeText(getApplicationContext(),"Desconectado", Toast.LENGTH_SHORT).show();
        writeHandler = null;
        Log.d(TAG, "WriteHandler ended");
        Intent intent = new Intent(getApplicationContext(),ConnectBluetooth.class);
        startActivity(intent);
        this.finish();
    }

    private void makeToast (String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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


}
