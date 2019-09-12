package com.leak.sdk.androidleack;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Main2Activity extends AppCompatActivity {
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button button = findViewById(R.id.test);
        //handler.sendEmptyMessageDelayed(0,10000);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             startAsyncWork();
            }
        });
    }



    @SuppressLint("StaticFieldLeak")
    void startAsyncWork() {
        Runnable work = new Runnable() {
            @Override
            public void run() {
                // Do some slow work in background
                SystemClock.sleep(20000);
            }
        };
        new Thread(work).start();
    }
}
