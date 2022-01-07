package com.DorelSaig.superme;

import android.app.Application;

import com.DorelSaig.superme.Firebase.User_DataManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();



        //Initiate FireBase Managers
        User_DataManager.initHelper();
    }
}
