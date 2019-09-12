package com.leak.sdk.androidleack;

import android.annotation.SuppressLint;
import android.app.Application;

import com.leak.sdk.leaklibrary.AndroidLeak;


@SuppressLint("Registered")
public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidLeak.init(this);
    }

}
