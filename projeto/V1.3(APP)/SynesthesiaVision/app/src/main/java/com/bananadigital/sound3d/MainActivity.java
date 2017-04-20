package com.bananadigital.sound3d;

import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";


    //Used for prints message with TAG on Android Monitor.
    private static final String TAG = "MainActivity" ;

    //Delimiter used in handleMsg.
    private static final char DELIMITER = '\n';
    private static final int MAX = 10; //max frequency for frequencySound
    private static final int MIN = 1;

    private static String received;

    //Utils
    private Timer timer;
    private TimerTask task;
    private MediaPlayer mPlayer;
    private Vibrator vibrator;


    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;

    //private Handler writeHandler;

    //Variables
    private int frequencySound_ms = 100; //miliseconds
    private int frequencySound = 1;
    private Boolean init = true;
    private int number_sensor = 5; //number of sensors
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
    private Button btnApply;
    private Button btnWeather;
    private Button btnIncreaseFrequency;
    private Button btnDecraseFrequency;
    private TextView txtFrequency;
    private SeekBar seekFrequency;
    private EditText edtWeather;

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
        //ConnectBluetooth.btt.setReadHandler(readHandler);

        //Create a filter and add it a callback to manage events with bluetooh, like when device is disconnected
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ACTION_USB_PERMISSION);
        filter1.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter1.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(broadcastReceiver, filter1);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);


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
            soundManager.createSoundPool();
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
        unregisterReceiver(mReceiver);
        unregisterReceiver(broadcastReceiver);
        if(mTTS != null) mTTS.destroyTTS();
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

        txtFrequency = (TextView) findViewById(R.id.txtFrequency);
        txtFrequency.setText("1/10 Hz");

        txtFront = (TextView) findViewById(R.id.txtFront);
        txtLeft = (TextView) findViewById(R.id.txtLeft);
        txtRight = (TextView) findViewById(R.id.txtRight);

        txtFront.setText("");
        txtRight.setText("");
        txtLeft.setText("");

        /*
        chkRight = (CheckBox) findViewById(R.id.chkRight);
        chkLeft = (CheckBox) findViewById(R.id.chkLeft);
        chkFront = (CheckBox) findViewById(R.id.chkFront);


        button_start = (Button) findViewById(R.id.button_start);
        button_disconnect = (Button) findViewById(R.id.btn_disconnect);
        btnAplicar = (Button) findViewById(R.id.btnOk);
        btnTempo = (Button) findViewById(R.id.btnTempo);
        */

        //edtTempo = (EditText) findViewById(R.id.edtTempo);

    }

    /**
     * Creates instances of another classes.
     */
    private void createInstances() {
        mTTS = new TextToSpeechManager(this);
        mTTS.createTTS();
        mGPS = new GPSTracker(this);
        mWeatherForecast = new WeatherForecast(weatherHandler);
        soundManager = new SoundManager(this);
        soundManager.createSoundPool();
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


        //Set the frequencySound_ms choosed on interface
        /*btnAplicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                if(!edtTempo.getText().toString().equals("")) {

                    //Get frequencySound_ms from EditText.
                    frequencySound_ms = Integer.parseInt(edtTempo.getText().toString());
                    
                    //If frequencySound_ms is too large, the frequencySound_ms is set to the maximum frequencySound_ms.
                    if(frequencySound_ms > 500) frequencySound_ms = 500;
                    button_start.performClick();
                    button_start.performClick();
                }
                Log.d("TEMPO", "Tempo ajustado para: " + frequencySound_ms);
            }
        });*/
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
        else if(sensor == 2 && chkFront.isChecked()){
            soundManager.setVolume(volume/2, volume/2);
            soundManager.setRate(frequencyFront);
            Log.d(TAG, "FRONT: D = " + distance + " V = " + volume);
        }
        //Right Sensor
        else if(sensor == 4 && chkRight.isChecked()){
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
            distance_sensor[4] = distance;
            txtRight.setText(String.valueOf(distance));
        }
        //Front
        else if(sensor == 'b'){
            distance_sensor[2] = distance;
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
    private Boolean canGetWeather = true;
    private int count = 0;

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
                if(canGetWeather) {
                    count++;
                    Log.d("On Main", "Weather Forecast acquisicion received, count: " + count);
                    canGetWeather = false;
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
        /*if(ConnectBluetooth.btt != null) {
            ConnectBluetooth.btt.interrupt();
            ConnectBluetooth.btt = null;
            Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show();
            startConnectBluetooth();
        }*/
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
                //Do something if disconnected
                //ConnectBluetooth.btt.interrupt();
                //Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();
            }
            //else if...
        }
    };


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                serialPort.close();
            }
        }
    };

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data;
            try {
                received = new String(arg0, "UTF-8");
                Log.d("Serial", "RECEIVED:" + received);
                //data = data.concat("/n");
                //data += '\n';
                //handleMsg(received);
                tvAppend(txtFront, received);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };
    public void onClickStart() {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341 || deviceVID == 4292 ){
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }


    }


    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
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
