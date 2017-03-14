package com.bananadigital.sound3d;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Note Dell on 14/03/2017.
 */

public class SendPostReqThread extends Thread {

    private String requestURL;
    private String latitude;
    private String longitude;
    private Handler readHandler;
    private URL url;
    private String response;

    public SendPostReqThread(String requestURL, String latitude, String longitude, Handler handler) {
        this.requestURL = requestURL;
        this.latitude = latitude;
        this.longitude = longitude;
        this.readHandler = handler;
    }

    public void run() {
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            //writer.write(getPostDataString(postDataParams));
            writer.write("lat="+latitude+"&lon="+longitude+"");

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
            Log.d("Recebido: ", response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
