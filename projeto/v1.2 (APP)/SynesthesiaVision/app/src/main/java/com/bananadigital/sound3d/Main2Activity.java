package com.bananadigital.sound3d;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class Main2Activity extends AppCompatActivity {

    private Button request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        request = (Button) findViewById(R.id.requestPost);

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PostRequest.createPost("http://sweetglass.azurewebsites.net/weather", "-8.058945", "-34.950434");
                String received;
                received = performPostCall("http://sweetglass.azurewebsites.net/weather", "-8.058945", "-34.950434");
                Log.d("Main2", received);
                Toast.makeText(Main2Activity.this, received, Toast.LENGTH_SHORT).show();
            }
        });

    }
    private String performPostCall(String requestURL1, String lat, String lon) {
        String received = "";

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        try {
            received = sendPostReqAsyncTask.execute(lat, lon,requestURL1).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return received;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };



}
