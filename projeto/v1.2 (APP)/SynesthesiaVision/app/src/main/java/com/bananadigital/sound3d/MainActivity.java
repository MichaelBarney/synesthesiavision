package com.bananadigital.sound3d;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.STREAM_MUSIC;

//Modificado para 3 Sensores

public class MainActivity extends AppCompatActivity implements SpeechRecognizerManager.OnResultListener {
    private static final String TAG = "MainActivity" ;
    private static final String TAG1 = "Teste1";
    private static final String TAG2 = "Teste2";
    private static final char DELIMITER = '\n';
    private static final int TIME_MAX = 500;
    private static final int MIN = 50 ;



    //Necessary for voice recognition
    private SpeechRecognizerManager mSpeechRecognizerManager;

    //Keyphrase to activate voice recognition
    private String KEYPHRASE = "ok google";

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
    private Button button_start;
    private Button button_disconnect;
    private Button btnAplicar;

    private EditText edtTempo;

    private int n_s = 3; //number of sensors
    private int c_s = 0; //current sensor

    float[] ds = new float[n_s];

    private CheckBox chkFrente;
    private CheckBox chkEsquerda;
    private CheckBox chkDireita;
    private CheckBox chkFrenteDireita;
    private CheckBox chkFrenteEsquerda;

    private Vibrator vibrator;
    long [] vibratorFrente = {0, 100, 50};
    long [] vibratorEsquerda = {0, 150, 50};
    long [] vibratorDireita = {0, 200, 50};


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

        //Voice Recognition
        mSpeechRecognizerManager = new SpeechRecognizerManager(this, KEYPHRASE);
        mSpeechRecognizerManager.setOnResultListner(this);

        //sound
        createSoundPool();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        chkDireita = (CheckBox) findViewById(R.id.chkDireita);
        chkEsquerda = (CheckBox) findViewById(R.id.chkEsquerda);
        chkFrente = (CheckBox) findViewById(R.id.chkFrente);

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
        button_start = (Button) findViewById(R.id.button_start);
        button_disconnect = (Button) findViewById(R.id.btn_disconnect);
        btnAplicar = (Button) findViewById(R.id.btnOk);

        button_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!init) {
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


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        soundPool.release();
    }

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
    }

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
                Log.d("TEMPO", "tempo: " + time);
            }
        };
        Log.d(TAG, "Iniciado");
    }

    int dmax = 300;
    float frequenciaEsquerda = 1.2f;
    float frequenciaDireita = 1.2f;
    float frequenciaFrente = 1.2f;

    private void playAudio(int s) {
        //calcula a itensidade proporcional de p para a distancia
        float v;
        float d = ds[s];

        //Log.d(TAG, "" + String.valueOf(d));

        if(d <= dmax) v =  1 - (d/dmax);
        else{v = 0.01f;}

        // 0 == LEFT
        // 1 == FRONT
        // 2 == RIGHT

        //LEFT
        if(s == 2  && chkEsquerda.isChecked()){

            soundPool.setVolume(soundID, v, 0);
            soundPool.setRate(soundID, frequenciaEsquerda);
            ev = v;
            Log.d("LEFT", "LEFT " + d + " " + v);
        }
        //FRONT
        else if(s == 1 && chkFrente.isChecked()){

            soundPool.setVolume(soundID, v/2, v/2);
            soundPool.setRate(soundID, frequenciaFrente);
            fv = v/2;
            Log.d("FRONT", "FRONT " + d + " " + v);
        }
        //RIGHT
        else if(s == 0 && chkDireita.isChecked()){

            soundPool.setVolume(soundID, 0, v);
            soundPool.setRate(soundID, frequenciaDireita);
            dv = v;
            Log.d("RIGHT", "RIGHT " + d + " " + v);
        }
    }


    private void saveAudio(char s, float d) {

        //Log.d(TAG, "Audio Saved");
        //LEFT


        //LEFT
        if(s == 'a'){
          //for(int i = 0; i < 4; i++)  ds[i] = d;

            ds[0] = d;


        }
        //FRONT
        else if(s == 'c'){
            ds[1] = d;
            //for(int i = 4; i < 8; i++)  ds[i] = d;
        }
        //RIGHT
        else if(s == 'e'){
            ds[2] = d;
            //for(int i = 8; i < 12; i++)  ds[i] = d;
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
                if(!distance.contains("DISCONNECTD") || !distance.contains("ISCONNECTED")) saveAudio(sensor, Float.valueOf(distance));
            /*if (sensor == 'c') {
            valor_f.setText("" + distance);
            volume_f.setText(String.format("%.02f", fv));
            } else if (sensor == 'a') {
                valor_d.setText("" + distance);
                volume_d.setText(String.format("%.02f", dv));
            } else if (sensor == 'e') {
                valor_e.setText("" + distance);
                volume_e.setText(String.format("%.02f", ev));
            }*/
            distance = "";
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

    private Boolean isPaused = false;

    @Override
    public void OnResult(ArrayList<String> commands) {

        Log.d("Reconhecimento", "Comando Recebido");

        for(String command:commands) {

            makeToast(command);
            String text = command.toLowerCase();

            if(text.contains("iniciar")) {
                if(!init) {
                    turnOnTimer();
                    timer = new Timer();
                    timer.schedule(task, 0, time);
                    init = true;
                    Log.d("Reconhecimento", "iniciado");
                }
                return;
            } else if(text.contains("desconectar")) {
                disconnect();
                Log.d("Reconhecimento", "desconectado");
                return;
            } else if(text.contains("parar") || text.contains("pausar")) {
                stopTimer();
                isPaused = true;
                init = false;
                Log.d("Reconhecimento", "Pausado/Parado");
                return;
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


}
