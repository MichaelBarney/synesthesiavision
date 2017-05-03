package com.bananadigital.sound3d;

import android.os.Handler;
import android.os.Message;
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
 * Class for get weather forecast.
 */
class WeatherForecast {

    private String latitude;
    private String longitude;
    private Handler handler;

    WeatherForecast(Handler handler) {
        this.handler = handler;
    }

    /**
     * Set the coordinates which come from the GPSTracker
     *
     * @param      latitude   The latitude
     * @param      longitude  The longitude
     */
    void setCoordinates(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the weather on specified location.
     *
     * @return     The weather.
     */
    String getWeather() {

        //Creates a new Thread to get the weather, because its consume large time.
        new Thread() {
            public void run() {
                URL url;
                String response = "";
                try{
                    Log.d("WeatherForecast", "weather: New prevision");

                    //Connects to Webserver
                    url = new URL("http://sweetglass.azurewebsites.net/weather");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    //Set the connection timeout to get out if has no response from server.
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));


                    writer.write("lat=" + latitude + "&lon=" + longitude + "");

                    writer.flush();
                    writer.close();
                    os.close();
                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            response += line;
                        }
                    } else {
                        response = "";
                    }
                    Log.d("Recebido: ", response);
                } catch(Exception e) {
                    e.printStackTrace();
                    getWeather();
                }
                sendToHandler(response);

            }


        }.start();
        return "";
    }

    /**
     * Sends to a received weather forecast to handler.
     *
     * @param      response  The response
     */
    private void sendToHandler(String response) {
        Message msg = Message.obtain();
        msg.obj = response;
        handler.sendMessage(msg);
    }
}
