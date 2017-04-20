package com.bananadigital.sound3d;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import static com.bananadigital.sound3d.R.id.edtTempo;
import static com.bananadigital.sound3d.R.id.seekFront;

/**
 * Created by Jonathan on 02/04/2017.
 */

public class W4All extends AppCompatActivity {

    //Used for prints message with TAG on Android Monitor.
    private static final String TAG = "MainActivity" ;

    //Delimiter used in handleMsg.
    private static final char DELIMITER = '\n';

    private static String received;

    //Utils
    private Timer timer;
    private TimerTask task;
    private MediaPlayer mPlayer;
    private Vibrator vibrator;


    //private Handler writeHandler;

    //Variables
    private int frequency = 100; //milliseconds
    private Boolean init = true;
    private int number_sensor = 5; //number of sensors
    private int current_sensor = 0; //current sensor
    private float[] distance_sensor = new float[number_sensor];
    private float frequenciaFrente = 1.0f;
    private float frequenciaDireita = 1.0f;
    private float frequenciaEsquerda = 1.0f;
    private float frequenciaFrenteEsquerda = 1.0f;
    private float frequenciaFrenteDireita = 1.0f;

    //Views variables
    private CheckBox chkFrente;
    private CheckBox chkEsquerda;
    private CheckBox chkFrenteEsquerda;
    private CheckBox chkDireita;
    private CheckBox chkFrenteDireita;
    private Button button_start;
    private Button btnAplicar;
    private EditText edtTempo;


    private TextView txtLeft;
    private TextView txtTopLeft;
    private TextView txtFront;
    private TextView txtTopRight;
    private TextView txtRight;


    private SeekBar seekFront;
    private SeekBar seekLeft;
    private SeekBar seekRight;
    private SeekBar seekTopRight;
    private SeekBar seekTopLeft;


    //Instace of classes
    private TextToSpeechManager mTTS;
    private SoundManager soundManager;

    //Patterns for vibration
    private long[] patternOff = {0, 200, 200, 200};
    private long[] patternOn = {0, 200, 200, 200, 200, 200};


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seekbar_testes);

        button_start = (Button) findViewById(R.id.button_start);
        btnAplicar = (Button) findViewById(R.id.btnOk);
        final Button btn_disconnect = (Button) findViewById(R.id.btn_disconnect);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        mTTS = new TextToSpeechManager(this);
        mTTS.createTTS();

        soundManager = new SoundManager(this);
        soundManager.createSoundPool();

        chkDireita = (CheckBox) findViewById(R.id.chkRight);
        chkEsquerda = (CheckBox) findViewById(R.id.chkLeft);
        chkFrente = (CheckBox) findViewById(R.id.chkFront);
        chkFrenteDireita = (CheckBox) findViewById(R.id.chkTopRitght);
        chkFrenteEsquerda = (CheckBox) findViewById(R.id.chkTopLeft);

        edtTempo = (EditText) findViewById(R.id.edtTempo);

        final TextView frequency1 = (TextView) findViewById(R.id.frequency);
        frequency1.setText("10.0 hz");
        seekLeft = (SeekBar) findViewById(R.id.seekLeft);
        seekFront = (SeekBar) findViewById(R.id.seekFront);
        seekRight = (SeekBar) findViewById(R.id.seekRight);
        seekTopLeft = (SeekBar) findViewById(R.id.seekTopLeft);
        seekTopRight = (SeekBar) findViewById(R.id.seekTopRight);

        txtFront = (TextView) findViewById(R.id.distanceFront);
        txtLeft = (TextView) findViewById(R.id.distanceLeft);
        txtRight = (TextView) findViewById(R.id.distanceRight);
        txtTopLeft = (TextView) findViewById(R.id.distanceTopLeft);
        txtTopRight = (TextView) findViewById(R.id.distanceTopRight);



        btnAplicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                if(!edtTempo.getText().toString().equals("")) {

                    //Get time from EditText.
                    frequency = (int) (float)(1/(Float.parseFloat(edtTempo.getText().toString()))*1000);
                    float freq = Float.parseFloat(edtTempo.getText().toString());

                    frequency1.setText(""+ freq+ " hz");
                    Log.d("On Main", "Frequencia : " + frequency);
                    Log.d("On Main", "Frequencia em ms: " + frequency);

                    //If time is too large, the time is set to the maximum time.
                    if(!init) {
                        btn_disconnect.performClick();
                        button_start.performClick();
                    }
                }
                Log.d("TEMPO", "Tempo ajustado para: " + frequency);
            }
        });

        button_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                //If init = true, the sound starts else, its stop
                if(init) {
                    createTimer();
                    soundManager.resume();
                    timer = new Timer();
                    timer.schedule(task, 0, frequency);
                    Log.d(TAG, "Clicked!");
                    init = false;
                    //vibrator.vibrate(patternOn, -1);    //Vibrates three times indicating that sound starts
                }
            }
        });

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!init) {
                    soundManager.pause();
                    init = true;
                }
            }
        });

        seekLeft.setMax(499);
        seekTopLeft.setMax(499);
        seekFront.setMax(499);
        seekTopRight.setMax(499);
        seekRight.setMax(499);


        seekLeft.setProgress(499);
        seekTopLeft.setProgress(499);
        seekFront.setProgress(499);
        seekTopRight.setProgress(499);
        seekRight.setProgress(499);

        saveData('a', (seekFront.getProgress()+1));
        saveData('b', (seekFront.getProgress()+1));
        saveData('c', (seekFront.getProgress()+1));
        saveData('d', (seekFront.getProgress()+1));
        saveData('e', (seekFront.getProgress()+1));

        txtFront.setText((seekFront.getProgress()+1) + "/" + (seekFront.getMax()+1) +" cm");
        txtLeft.setText((seekLeft.getProgress()+1)+ "/" + (seekLeft.getMax()+1) + " cm");
        txtRight.setText((seekRight.getProgress()+1) + "/" + (seekRight.getMax()+1) +" cm");
        txtTopRight.setText((seekTopRight.getProgress()+1)+ "/" + (seekTopRight.getMax()+1) + " cm");
        txtTopLeft.setText((seekTopLeft.getProgress()+1)+ "/" + (seekTopLeft.getMax()+1) + " cm");


        seekRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                saveData('a', i+1);
                txtRight.setText((i+1) + "/" + (seekRight.getMax()+1) +" cm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekTopRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                saveData('b', i+1);
                txtTopRight.setText((i+1) + "/" + (seekTopRight.getMax()+1) +" cm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekFront.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                saveData('c', i+1);
                txtFront.setText((i+1) + "/" + (seekFront.getMax()+1) +" cm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekTopLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                saveData('d', i+1);
                txtTopLeft.setText((i+1) + "/" + (seekTopLeft.getMax()+1) +" cm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                saveData('e', i+1);
                txtLeft.setText((i+1) + "/" + (seekLeft.getMax()+1) +" cm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


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

    private void createTimer() {
        task = new TimerTask() {
            @Override
            public void run() {


                if(current_sensor == number_sensor) {
                    current_sensor = 0;
                }
                playAudio(current_sensor);  //Plays audio for specified sensor during the time.
                Log.d("TEMPO", "tempo: " + frequency);
                current_sensor ++;
            }
        };
        Log.d(TAG, "Iniciado");
    }

    private void saveData(char sensor, float distance) {

        //Right
        if(sensor == 'a'){
            distance_sensor[4] = distance;
        }
        else if(sensor == 'b') {
            distance_sensor[3] = distance;
        }
        //Front
        else if(sensor == 'c'){
            distance_sensor[2] = distance;
        }
        else if(sensor == 'd') {
            distance_sensor[1] = distance;
        }
        //Left
        else if(sensor == 'e'){
            distance_sensor[0] = distance;
        }

        Log.d("On main", "Sensor: " + sensor + " Distance: " + distance);
    }

    private void playAudio(int sensor) {

        float volume;
        float distance = distance_sensor[sensor];
        float dmax = 400;

        if (distance < dmax) volume = 1 - (distance / dmax);
        else volume = 0.01f;

        //Change frequency when distance is less than a specified value
        if(distance < 50) {
            frequenciaDireita = 2.0f;
            frequenciaEsquerda = 2.0f;
            frequenciaFrente = 2.0f;
            frequenciaFrenteDireita = 2.0f;
            frequenciaFrenteEsquerda = 2.0f;
        } else if(distance >= 50 && distance < 100) {
            frequenciaDireita = 1.8f;
            frequenciaEsquerda = 1.8f;
            frequenciaFrente = 1.8f;
            frequenciaFrenteEsquerda = 1.8f;
            frequenciaFrenteDireita = 1.8f;
        } else if(distance >= 100) {
            frequenciaDireita = 1.6f;
            frequenciaEsquerda = 1.6f;
            frequenciaFrente = 1.6f;
            frequenciaFrenteDireita = 1.6f;
            frequenciaFrenteEsquerda = 1.6f;
        } else {
            volume = 0.01f;
        }

        //Left Sensor
        if(sensor == 0 && chkEsquerda.isChecked()){
            soundManager.setVolume(volume, 0);                          //Set the volume according to the sensor passed to function.
            soundManager.setRate(frequenciaEsquerda);                   //Set the specified frequency.
            Log.d(TAG, "LEFT: D = " + distance + " V = " + volume);
        }
        else if(sensor == 1 && chkFrenteEsquerda.isChecked()){
            soundManager.setVolume( (volume * 3) / 4, volume / 4);
            soundManager.setRate(frequenciaFrenteEsquerda);
        }
        //Front Sensor
        else if(sensor == 2 && chkFrente.isChecked()){
            soundManager.setVolume(volume/2, volume/2);
            soundManager.setRate(frequenciaFrente);
            Log.d(TAG, "FRONT: D = " + distance + " V = " + volume);
        }

        //Top Right
        else if(sensor == 3 && chkFrenteDireita.isChecked()){
            soundManager.setVolume( volume / 4, (volume * 3) / 4);
            soundManager.setRate(frequenciaFrenteDireita);
        }

        //Right Sensor
        else if(sensor == 4 && chkDireita.isChecked()){
            soundManager.setVolume(0, volume);
            soundManager.setRate(frequenciaDireita);
            Log.d(TAG, "RIGHT: D = " + distance + " V = " + volume);
        }

    }
}
