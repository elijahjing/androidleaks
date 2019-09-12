package com.leak.sdk.androidleack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.leak.sdk.leaklibrary.activity.HeapActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=findViewById(R.id.bottom_test);
        Button button2=findViewById(R.id.bottom_test2);

        final Intent intent=new Intent(MainActivity.this,Main2Activity.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(intent);
            }
        });
        final Intent intent2=new Intent(MainActivity.this, HeapActivity.class);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(intent2);
            }
        });
    }
}
