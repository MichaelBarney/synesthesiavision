package com.bananadigital.sound3d;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    //public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";

    //Used for prints message with TAG on Android Monitor.
    private static final String TAG = "MainActivity" ;

    //Delimiter used in handleMsg.
    private static final char DELIMITER = '\n';
    private static final int MAX = 10; //max frequency for frequencySound
    private static final int MIN = 1;

    //private static String received;

    //Utils
    private Timer timer;
    private TimerTask task;
    private MediaPlayer mPlayer;
    private Vibrator vibrator;

    private boolean canGetWeather = true;
    private int count = 0;
    private boolean mTTS_Spoke = true;

    /*
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;*/

    //private Handler writeHandler;

    //Variables
    private int frequencySound_ms = 100; //miliseconds
    private int frequencySound = 1;
    private boolean init = true;
    private int number_sensor = 3; //number of sensors
    private int current_sensor = 0; //current sensor
    private float[] distance_sensor = new float[number_sensor];
    private float frequencyFront = 1.0f;
    private float frequencyRight = 1.0f;
    private float frequencyLeft = 1.0f;

    //Views variables
    private CheckBox chkFront;
    private CheckBox chkLeft;
    private CheckBox chkRight;
    private Button btnStart;
    private Button btnDisconnect;
    private Button btnWeather;
    private Button btnIncreaseFrequency;
    private Button btnDecraseFrequency;
    private TextView txtFrequency;

    private TextView txtLeft;
    private TextView txtRight;
    private TextView txtFront;

    //Instace of classes
    private WeatherForecast mWeatherForecast;
    private TextToSpeechManager mTTS;
    private SoundManager soundManager;
    private GPSTracker mGPS;

    //Patterns for vibration
    private long[] patternOff = {0, 200, 200, 200};
    private long[] patternOn = {0, 200, 200, 200, 200, 200};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        //Keep the screen on indeterminately
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //writeHandler = ConnectBluetooth.btt.getWriteHandler();

        //Sets the readHandler to manage the received messages.
        ConnectBluetooth.btt.setReadHandler(readHandler);

        //Create a filter and add it a callback to manage events with bluetooh, like when device is disconnected
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

        /*IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ACTION_USB_PERMISSION);
        filter1.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter1.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(broadcastReceiver, filter1);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);*/


        createInstances();
        findViews();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playSound(R.raw.bluetooth_confirma);
        if(!mGPS.canGetLocation()) mGPS.showSettingsAlert();
        if(soundManager == null) {
            soundManager = new SoundManager(this);
        }

    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        playSound(R.raw.finalizar);
        if(soundManager != null) {
            soundManager.destroySoundPool();
            soundManager = null;
        }
        if(mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGPS != null) mGPS.stopUsingGPS();
        if(mTTS != null) mTTS.destroyTTS();
        unregisterReceiver(mReceiver);

    }

    /**
     * Find the views on xml file 
     */
    private void findViews() {

        chkFront = (CheckBox) findViewById(R.id.chkFront);
        chkLeft = (CheckBox) findViewById(R.id.chkLeft);
        chkRight = (CheckBox) findViewById(R.id.chkRight);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnWeather = (Button) findViewById(R.id.btnWeather);
        btnIncreaseFrequency = (Button) findViewById(R.id.btnIncrease);
        btnDecraseFrequency = (Button) findViewById(R.id.btnDeacrease);

        btnStart.setContentDescription(getResources().getString(R.string.description_btn_start));
        btnDisconnect.setContentDescription(getResources().getString(R.string.description_btn_disconnect));
        btnWeather.setContentDescription(getResources().getString(R.string.description_btn_weather));
        btnIncreaseFrequency.setContentDescription(getResources().getString(R.string.description_btn_increase_frequency));
        btnDecraseFrequency.setContentDescription(getResources().getString(R.string.description_btn_decrease_frequency));


        txtFrequency = (TextView) findViewById(R.id.txtFrequency);
        txtFrequency.setText(MIN + "/" + MAX);

        txtFront = (TextView) findViewById(R.id.txtFront);
        txtLeft = (TextView) findViewById(R.id.txtLeft);
        txtRight = (TextView) findViewById(R.id.txtRight);

        txtFront.setText("");
        txtRight.setText("");
        txtLeft.setText("");

    }

    /**
     * Creates instances of another classes.
     */
    private void createInstances() {
        Context mContext = getApplicationContext();
        mTTS = new TextToSpeechManager(mContext);
        mGPS = new GPSTracker(mContext);
        mWeatherForecast = new WeatherForecast(weatherHandler);
        soundManager = new SoundManager(mContext);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Sets the buttons listener for click action buttons.
     */
    private void setListeners() {

        //Get weather prevision when the button is clicked.
        btnWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Calls the function that will pick up the weather forecast
                getWeatherForecast();
                Log.d("weather", "weather acquired");
            }
        });

        btnWeather.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTTS.speak(getResources().getString(R.string.description_btn_weather_long));
                return false;
            }
        });


        //Start the sound when the button is clicked.
        btnStart.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                //If init = true, the sound starts else, its stop
                if(init) {
                    frequencySound_ms = (int) (float)(1/frequencySound); //hz to s

                    Log.d(TAG, "Sound Frequency: " + frequencySound_ms);

                    mTTS.speak("Iniciando sonorização");
                    createTimer();
                    soundManager.resume();
                    timer = new Timer();
                    timer.schedule(task, 0, frequencySound_ms);
                    Log.d(TAG, "Clicked!");
                    init = false;
                    vibrator.vibrate(patternOn, -1);    //Vibrates three times indicating that sound starts
                } else {
                    soundManager.pause();
                    mTTS.speak("Pausando Sonorização");
                    init = true;
                    vibrator.vibrate(patternOff, -1);   //Vibrate one frequencySound_ms indicating that sound stop
                }
            }
        });

        btnStart.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTTS.speak(getResources().getString(R.string.descritption_btn_start_long));
                return false;
            }
        });

        //Disconnect the bluetooth socket with glasses
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        btnIncreaseFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(frequencySound < MAX) {
                    frequencySound += 1;
                    txtFrequency.setText((frequencySound) + "/"+MAX+" Hz");
                    if(!init) {
                        stopTimer();
                        btnStart.performClick();
                    }
                } else {
                    mTTS.speak("Frequencia maxima atingida");

                }
                Log.d("Main", "Frequency: " + frequencySound);
            }
        });

        btnIncreaseFrequency.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTTS.speak(getResources().getString(R.string.description_btn_increase_long));
                return false;
            }
        });

        btnDecraseFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(frequencySound > MIN){
                    frequencySound -= 1;
                    txtFrequency.setText((frequencySound) + "/"+MAX+" Hz");
                    if(!init) {
                        stopTimer();
                        btnStart.performClick();
                    }
                } else {
                    mTTS.speak("Frequencia minima atingida");
                }
                Log.d("Main", "Frequency: " + frequencySound);
            }
        });

        btnDecraseFrequency.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTTS.speak(getResources().getString(R.string.description_btn_decrease_long));
                return false;
            }
        });
    }

    /**
     * Determines if the internet has connectivity.
     *
     * @return     True if online, False otherwise.
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Gets the weather forecast.
     */
    private void getWeatherForecast(){
        if(mGPS.canGetLocation() && isOnline()){

            //Get latitude from GPS
            double lat = mGPS.getLatitude();

            //Get Longitude from GPS
            double lon = mGPS.getLongitude();

            //Converts to String
            String latitude = String.valueOf(lat);
            String longitude = String.valueOf(lon);

            //Sets coordinates to get weather forecast on the user's location
            mWeatherForecast.setCoordinates(latitude, longitude);

            //Start the weather forecast
            mWeatherForecast.getWeather();
        } else if(!mGPS.canGetLocation()) {
            mTTS.speak("GPS Desativado, por favor ative!");
            mGPS.showSettingsAlert();
        } else if(!isOnline()){
            mTTS.speak("Por favor ative a rede de dados.");
        }
    }

    /**
     * Play sound indicating actions performed on users interface, like when bluetooth connection is ok.
     *
     * @param      file  The file to play
     */
    private void playSound(int file) {
        try {
            if(mPlayer != null) mPlayer = null;
            mPlayer = MediaPlayer.create(this, file);
            mPlayer.start();
        } catch (Exception e) {
            Log.d("SOM", "Erro na execução do som");
            e.printStackTrace();
        }
    }

    /**
     * Stops the timer which is responsable to creates the 3D sound.
     */
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

    /**
     * Creates a timer which is responsable to creates the 3D sound.
     */
    private void createTimer() {
        task = new TimerTask() {
            @Override
            public void run() {
            
                current_sensor ++;
                if(current_sensor == number_sensor) {
                    current_sensor = 0;
                }
                playAudio(current_sensor);  //Plays audio for specified sensor during the frequencySound_ms.
                //Log.d("TEMPO", "tempo: " + frequencySound_ms);
            }
        };
        //Log.d(TAG, "Iniciado");
    }

    /**
     * Generates the 3D sound 
     *
     * @param      sensor     Value of sensor
     */
    private void playAudio(int sensor) {
        
        float volume;
        float distance = distance_sensor[sensor];
        float dmax = 400;

        if (distance < dmax) volume = 1 - (distance / dmax);
        else volume = 0.01f;

        //Change frequency when distance is less than a specified value
        if(distance < 50) {
            frequencyRight = 2.0f;
            frequencyLeft = 2.0f;
            frequencyFront = 2.0f;
        } else if(distance >= 50 && distance < 100) {
            frequencyRight = 1.8f;
            frequencyLeft = 1.8f;
            frequencyFront = 1.8f;
        } else if(distance >= 100) {
            frequencyRight = 1.6f;
            frequencyLeft = 1.6f;
            frequencyFront = 1.6f;
        } else {
            volume = 0.01f;
        }

        //Left Sensor
        if(sensor == 0 && chkLeft.isChecked()){
            soundManager.setVolume(volume, 0);                          //Set the volume according to the sensor passed to function.
            soundManager.setRate(frequencyLeft);                   //Set the specified frequency.
            Log.d(TAG, "LEFT: D = " + distance + " V = " + volume);
        }
        //Front Sensor
        else if(sensor == 1 && chkFront.isChecked()){
            soundManager.setVolume(volume/2, volume/2);
            soundManager.setRate(frequencyFront);
            Log.d(TAG, "FRONT: D = " + distance + " V = " + volume);
        }
        //Right Sensor
        else if(sensor == 2 && chkRight.isChecked()){
            soundManager.setVolume(0, volume);
            soundManager.setRate(frequencyRight);
            Log.d(TAG, "RIGHT: D = " + distance + " V = " + volume);
        }

    }

    /**
     * Saves a distance which comes from the glass.
     *
     * @param      sensor    The sensor which value will be saved.
     * @param      distance  Save specified sensors distance.   
     */
    private void saveData(char sensor, float distance) {

        //Log.d("MainActivity", "Data Saved: "+ sensor + " " + distance);
        //Right
        if(sensor == 'a'){
            distance_sensor[2] = distance;
            txtRight.setText(String.valueOf(distance));
        }
        //Front
        else if(sensor == 'b'){
            distance_sensor[1] = distance;
            txtFront.setText(String.valueOf(distance));
        }
        //Left
        else if(sensor == 'c'){
            distance_sensor[0] = distance;
            txtLeft.setText(String.valueOf(distance));
        }
    }

    /**
     * Handler that will handle the data which comes through Bluetooth socket.
     */
    Handler readHandler = new Handler () {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //Get message which has sent by BluetoothThread
            String received = msg.obj.toString();
            received = received.toLowerCase();
            received = received.trim();
            received += DELIMITER;
            //if(received.contains("turnon")) turnOn();
            if(received.contains("getweather")) {
                //getWeatherForecast();
                if(canGetWeather && mTTS_Spoke) {
                    count++;
                    Log.d("On Main", "Weather Forecast acquisicion received, count: " + count);
                    canGetWeather = false;
                    mTTS_Spoke = false;
                    new CountDownTimer(5000, 5000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            canGetWeather = true;
                        }
                    }.start();
                }
            }
            //Add the delimiter to string received
            else if(received.contains("disconnected")) {
                disconnect();
            } else {
                handleMsg(received);
            }

        }

    };

    /*
    private void turnOn() {
    }*/

    /**
     * Handler that will handle the data which comes through WeatherForecast
     */
    Handler weatherHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //Get message from class Message and turn into a String.
            String received = msg.obj.toString();

            //Allow to user get another weather forecast
            mTTS_Spoke = true;

            //Make TTS speak the weather forecast.
            mTTS.speak(received);
        }
    };

    /**
     * Filtrate data which come from readHandler and save it in saveData
     *
     * @param      received     String received by handler and will be filtered.
     */
    private void handleMsg(String received) {

        int inx = received.indexOf(DELIMITER);
        //Get the first character responsable for indentifier the sensor
        String sensor1 = received.substring(0, 1);
        //Get the distance after character
        String distance = "";
        try{
            distance = received.substring(1, inx);
        } catch(Exception e) {
            e.printStackTrace();
        }

        //Get the primary character at message, which corresponds to sensor which has sent the distance
        char sensor = sensor1.charAt(0);

        //Log.d(TAG,"[SENSOR]: " + sensor + " [DISTANCE]: "+ distance);

        //If have any data, it will save.
        if(Character.isLetter(sensor) && Character.isDigit(distance.charAt(0))) {
            if (!distance.isEmpty() && !distance.contains("DISCONNECTED")) {
                saveData(sensor, Float.valueOf(distance));
            }
        }
    }


    /**
     * Disconnect bluetooth socket and return to connection interface.
     */
    public void disconnect() {
        //ConnectBluetooth.btt.write("DISCONNECTED");
        if(ConnectBluetooth.btt != null) {
            ConnectBluetooth.btt.interrupt();
            ConnectBluetooth.btt = null;
            Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show();
            startConnectBluetooth();
        }
        txtFront.setText("");
    }

    /**
     * Starts the ConnectBluetooth class.
     */
    private void startConnectBluetooth(){

        Toast.makeText(getApplicationContext(),"Desconectado", Toast.LENGTH_SHORT).show();
        //writeHandler = null;
        //Log.d(TAG, "WriteHandler ended");
        Intent intent = new Intent(getApplicationContext(),ConnectBluetooth.class);
        intent.putExtra("status", "disconnected");
        startActivity(intent);
        this.finish();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //If bluetooth is disconnected then interrupt the socket and init the ConnectBluetooth activity
                ConnectBluetooth.btt.interrupt();
                Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();
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
