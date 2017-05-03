package com.bananadigital.sound3d;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class ConnectBluetooth extends AppCompatActivity implements ListView.OnItemClickListener{

    //Bluetooth variables
    private static final String TAG = "ConnectBluetooth";
    private static final int PERMISSION_CODE = 2;
    public static int ENABLE_BLUETOOTH = 1;
    public static BluetoothThread btt;
    private BluetoothAdapter btAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView list_bt;

    //Views Variables
    private TextView statusConnection;

    //Variables
    private String devAddress;
    private String message = "";

    //Permissions needed for the App works
    private String[] permissions = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
    };

    //Utils
    private MediaPlayer mPlayer;

    //For SharedPreferences
    public static final String Storage = "storage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_connect);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) message = bundle.getString("status");
        else message = "connect";

        PermissionManager.checkPermission(this, permissions, PERMISSION_CODE );
        findViews();
        checkBluetoothConnection();

        playSound(R.raw.synesthesia_sound);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        playSound(R.raw.finalizar);
        if(mPlayer != null){
            mPlayer.release();
        }
    }

    private void findViews() {
        list_bt = (ListView) findViewById(R.id.btList);
        statusConnection = (TextView) findViewById(R.id.connect_text);
        list_bt.setOnItemClickListener(this);
    }

    /**
     * Check the bluetooth connection and 
     */
    private void checkBluetoothConnection() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!btAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ENABLE_BLUETOOTH);
        } else {

            if(!message.equals("disconnected")) autoConnect();

            getDevices();
        }
    }


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
     * Conecta automaticamente se houver um endereço de dispositivo já salvo anteriormente
     */
    private void autoConnect(){

        SharedPreferences bt_name = getSharedPreferences(Storage, 0);
        if(bt_name.contains("bt_address")){
            String address = bt_name.getString("bt_address", "*");
            Log.d(TAG, address);
            list_bt.setEnabled(false);
            btt = new BluetoothThread(address, mHandler);
            btt.start();
        }
    }

    /**
     * Salva o endereço do dispositivo pressionado caso haja uma conexão bem sucedida
     * @param address
     */
    private void saveAddress(String address){
        SharedPreferences bt_name = getSharedPreferences(Storage, 0);
        SharedPreferences.Editor editor = bt_name.edit();
        editor.putString("bt_address", address);

        // Commit the edits!
        editor.commit();

        //Log.d("saved", address);
    }

    /**
     * Responsável por fazer uma requisição de ativação de bluetooth e tomar as medidas necessárias
     * caso esta seja aceita ou recusada
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ENABLE_BLUETOOTH) {
            switch (resultCode) {
                case RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), "Erro! Bluetooth não ativo!",
                            Toast.LENGTH_SHORT).show();
                    close();
                    break;

                case RESULT_OK:
                    
                    getDevices();
                    break;
            }
        }
    }

    private void close() {

        this.finish();
        mPlayer.release();
    }

    /**
     * Função que conecta ao dispositivo que foi pressionado no listview
     * @param adapterView
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        String item = (String) list_bt.getAdapter().getItem(position);
        String devName = item.substring(0, item.indexOf("\n"));
        devAddress = item.substring(item.indexOf("\n") + 1, item.length());


        Log.d(TAG, devName);
        Log.d(TAG, devAddress);

        //Toast.makeText(getApplicationContext(),devAddress,Toast.LENGTH_SHORT).show();

        btt = new BluetoothThread(devAddress, mHandler);

        // Run the thread
        btt.start();


        list_bt.setEnabled(false);
    }

    /**
     * Responsável por manusear as mensagens vindas da Thread do Bluetooth
     */
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {

            String s = (String) message.obj;

            switch (s) {
                case "CONNECTED": {
                    //saveAddress(adress);
                    statusConnection.setText(R.string.a_conexao);
                    //btt.write("CONNECTED");
                    playSound(R.raw.bluetooth_confirma);
                    saveAddress(devAddress);
                    Toast.makeText(getApplicationContext(),"Conectado", Toast.LENGTH_SHORT).show();
                    startMain();
                    break;
                } case "CONNECTING": {
                    Toast.makeText(getApplicationContext(),"Conectando", Toast.LENGTH_SHORT).show();
                    statusConnection.setText(R.string.conectando);
                    break;
                } case "DISCONNECTED": {
                    list_bt.setEnabled(true);
                    break;
                } case "CONNECTION FAILED": {
                    list_bt.setEnabled(true);
                    statusConnection.setText(R.string.f_conexao);
                    playSound(R.raw.bluetooth_erro);
                    Toast.makeText(ConnectBluetooth.this, "Falha na Conexão", Toast.LENGTH_SHORT).show();
                    btt = null;
                    break;
                }
            }
        }
    };

    private void startMain() {

        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("Timer", "Timer ok");
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                close();
            }
        }.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Log.d("Permissoes", "Permissões garantidas!");
            }else{
                //Displaying another toast if permission is not granted
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permissões negadas");
                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        close();
                    }
                });

                AlertDialog dlg = builder.create();
                dlg.show();
            }
        }
    }

    public void getDevices() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list_bt.setAdapter(adapter);
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }
}
