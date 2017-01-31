package com.bananadigital.sound3d;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.AsyncTask;
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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.media.AudioManager.STREAM_MUSIC;

//Modificado para 3 Sensores

public class MainActivity extends AppCompatActivity implements
        RecognitionListener {
    private static final String TAG = "MainActivity" ;
    private static final String TAG1 = "Teste1";
    private static final String TAG2 = "Teste2";
    private static final char DELIMITER = '\n';
    private static final int TIME_MAX = 500;
    private static final int MIN = 50 ;


    //Necessary for voice recognition

    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PHONE_SEARCH = "phones";
    private static final String MENU_SEARCH = "menu";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "ok google";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    //Variables
    private SoundPool soundPool;
    private int sf;

    private Timer timer;
    private int time = 250; //miliseconds
    private TimerTask task;

    private Handler writeHandler;

    private TextView tempo_total;
    private TextView tempo_sensor;

    private int n_s = 3; //number of sensors
    private int c_s = 0; //current sensor

    float[] ds = new float[n_s];

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

    private ImageButton btnSpeak;
    private TextView txtSpeechInput;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private SpeechRecognizerManager mSpeechManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //writeHandler = ConnectBluetooth.btt.getWriteHandler();
        //ConnectBluetooth.btt.setReadHandler(readHandler);
        setContentView(R.layout.activity_main);

        //Recognition
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(MENU_SEARCH, R.string.menu_caption);
        captions.put(DIGITS_SEARCH, R.string.digits_caption);
        captions.put(PHONE_SEARCH, R.string.phone_caption);
        captions.put(FORECAST_SEARCH, R.string.forecast_caption);


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
        Button button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                turnOnTimer();
                timer = new Timer();
                timer.schedule(task, 0, time);
                Log.d(TAG, "Clicked!");
            }
        });


        SeekBar frequency = (SeekBar) findViewById(R.id.frequency);
        frequency.setMax(TIME_MAX);
        frequency.setProgress(time);
        tempo_sensor.setText("" + time);
        tempo_total.setText("" + ((float)(time * n_s)/1000));
        frequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < MIN) time = progress + 100;
                else time = progress;
                tempo_sensor.setText("" + time);
                tempo_total.setText("" + ((float)(time * n_s)/1000));
                stopTimer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopTimer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                timer = new Timer();
                timer.schedule(task, time);
            }
        });

        runRecognizerSetup();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    //((TextView) findViewById(R.id.caption_text)).setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }


    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        switch (text) {
            case KEYPHRASE:
                switchSearch(MENU_SEARCH);
                break;
            case DIGITS_SEARCH:
                switchSearch(DIGITS_SEARCH);
                break;
            case PHONE_SEARCH:
                switchSearch(PHONE_SEARCH);
                break;
            case FORECAST_SEARCH:
                switchSearch(FORECAST_SEARCH);
                break;
            default:
                ((TextView) findViewById(R.id.result_text)).setText(text);
                break;
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeToast(text);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

        String caption = getResources().getString(captions.get(searchName));
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

        // Create language model search
        File languageModel = new File(assetsDir, "weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);

        // Phonetic search
        File phoneticModel = new File(assetsDir, "en-phone.dmp");
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error) {
        //((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    private void stopTimer() {
        if(timer != null && task != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            task.cancel();
            task = null;
        }
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
    private void playAudio(int s) {
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
                saveAudio(sensor, Float.valueOf(distance));
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
            }
            received += DELIMITER;
            handleMsg(received);
        }

    };

    public void disconnect(View view) {
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
