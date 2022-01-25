package com.DorelSaig.superme;

import android.app.Application;

import com.DorelSaig.superme.Firebase.MyDataManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();



        //Initiate FireBase Managers
        MyDataManager.initHelper();
    }
}
