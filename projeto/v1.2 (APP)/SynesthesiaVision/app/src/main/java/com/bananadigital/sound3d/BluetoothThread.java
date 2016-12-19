package com.bananadigital.sound3d;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Michael Barney Jr on 18/03/2015.
 */
public class BluetoothThread extends Thread {
    //private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    private static final int SUCCESS_CONNECT = 0;
    private static final int FAILLED_CONNECT = 1;
    private static final int CONNECTING = 2;
    private static final int MESSAGE_READ = 3;

    BluetoothSocket socket = null;
    Handler mHandler;
    BluetoothAdapter BA;

    //Streams
    InputStream tmpIn = null;
    OutputStream tmpOut = null;

    public BluetoothThread(BluetoothDevice device, Handler h, BluetoothAdapter b) {
        mHandler = h;
        BA = b;
        mHandler.obtainMessage(CONNECTING).sendToTarget();
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            socket =  device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) { }

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
    }




    public void run() {
        // Cancel discovery because it will slow down the connection
        BA.cancelDiscovery();

        try {
            socket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mHandler.obtainMessage(FAILLED_CONNECT).sendToTarget();
                Log.e("tag", "FAILLED");
                socket.close();
            } catch (IOException closeException) { }
            return;
        }
        mHandler.obtainMessage(SUCCESS_CONNECT).sendToTarget();
        Log.d("tag", "CONNECTED");


        //AFTER CONNECTION

        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        while (true) {
            try {
                // Read from the InputStream
                bytes = tmpIn.read(buffer);            //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes);
                // Send the obtained bytes to the UI Activity via handler
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, readMessage).sendToTarget();

            } catch (IOException e) {
                break;
            }
        }
    }

    /** Will cancel an in-progress connection, and close the socket */

    public void write(byte[] bytes) {
        try {
            tmpOut.write(bytes);
        } catch (IOException e) { }
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) { }
    }

    public void setHandler(Handler h){
        this.mHandler = h;
        Log.e("thread ", "HANDLER Set");
    }
}
