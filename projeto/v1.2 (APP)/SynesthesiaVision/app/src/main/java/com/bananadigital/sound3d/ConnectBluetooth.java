package com.bananadigital.sound3d;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class ConnectBluetooth extends AppCompatActivity implements ListView.OnItemClickListener{


    private static final String TAG = "ConnectBluetooth";
    public static BluetoothThread btt;
    private BluetoothAdapter btAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView list_bt;
    public static int ENABLE_BLUETOOTH = 1;
    private ImageView img;
    private TextView statusConnection;

    //Used for voice recognition
    private SpeechRecognizerManager mSpeechRecognizerManager;
    //Keyphrase to activate voice recognition
    private static final String KEYPHRASE = "ok google";

    public static final String Storage = "storage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_connect);

        if(btt != null) {
            btt.interrupt();
            btt = null;
            Log.d(TAG, "Connection Ended.");
        }

        list_bt = (ListView) findViewById(R.id.btList);
        img = (ImageView) findViewById(R.id.connect_img);
        statusConnection = (TextView) findViewById(R.id.connect_text);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        list_bt.setOnItemClickListener(this);

        if(!btAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ENABLE_BLUETOOTH);
        }
        if(btAdapter.isEnabled()) {

            autoConnect();

            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        /*  Cria um modelo para a lista e o adiciona à tela.
            Se houver dispositivos pareados, adiciona cada um à lista.
         */
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            list_bt.setAdapter(adapter);
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    adapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mSpeechRecognizerManager.destroy();
    }

    /**
     * Conecta automaticamente se houver um endereço de dispositivo já salvo anteriormente
     */
    private void autoConnect(){

        SharedPreferences bt_name = getSharedPreferences(Storage, 0);
        String address = bt_name.getString("bt_address", "*");
        Log.d(TAG, address);
        Toast.makeText(this, "Conectando ao dispositivo", Toast.LENGTH_SHORT).show();
        if(address != "*"){
            list_bt.setEnabled(false);
            BluetoothDevice bt = btAdapter.getRemoteDevice(address);
            btt = new BluetoothThread(address, mHandler);
            btt.start();
        }
    }

    /**
     * Salva o endereço do dispositivo pressionado caso haja uma conexão bem sucedida
     * @param address
     */
    void saveAddress(String address){
        SharedPreferences bt_name = getSharedPreferences(Storage, 0);
        SharedPreferences.Editor editor = bt_name.edit();
        editor.putString("bt_address", address);

        // Commit the edits!
        editor.commit();

        Log.d("saved", address);
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
                    if (btAdapter.isEnabled()) {
                        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
                        list_bt.setAdapter(adapter);
                        if (pairedDevices.size() > 0) {
                            for (BluetoothDevice device : pairedDevices) {
                                adapter.add(device.getName() + "\n" + device.getAddress());
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void close() {

        this.finish();
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
        String devAddress = item.substring(item.indexOf("\n") + 1, item.length());


        Log.d(TAG, devName);
        Log.d(TAG, devAddress);

        //Toast.makeText(getApplicationContext(),devAddress,Toast.LENGTH_SHORT).show();

        btt = new BluetoothThread(devAddress, mHandler);

        // Run the thread
        btt.start();


        list_bt.setEnabled(false);
        saveAddress(devAddress);

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
                    img.setImageDrawable(getResources().getDrawable(R.drawable.check));
                    statusConnection.setText(R.string.a_conexao);
                    btt.write("CONNECTED");
                    Toast.makeText(getApplicationContext(),"Conectado", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    close();

                    break;
                } case "CONNECTING": {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.loading));
                    img.setContentDescription(getString(R.string.conectando));
                    Toast.makeText(getApplicationContext(),"Conectando", Toast.LENGTH_SHORT).show();
                    statusConnection.setText(R.string.conectando);
                    break;
                } case "DISCONNECTED": {
                    list_bt.setEnabled(true);
                    break;
                } case "CONNECTION FAILED": {
                    list_bt.setEnabled(true);
                    img.setImageDrawable(getResources().getDrawable(R.drawable.cross));
                    img.setContentDescription(getString(R.string.f_conexao));
                    statusConnection.setText(R.string.f_conexao);
                    Toast.makeText(ConnectBluetooth.this, "Falha na Conexão", Toast.LENGTH_SHORT).show();
                    btt = null;
                    break;
                }
            }
        }
    };
}
