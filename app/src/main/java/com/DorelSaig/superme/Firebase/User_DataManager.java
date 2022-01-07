package com.DorelSaig.superme.Firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class User_DataManager {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore dbFireStore = FirebaseFirestore.getInstance();


    private static User_DataManager single_instance = null;

    private User_DataManager(){
        firebaseAuth = FirebaseAuth.getInstance();
        dbFireStore = FirebaseFirestore.getInstance();
    }

    public static User_DataManager getInstance() {
        return single_instance;
    }

    public static User_DataManager initHelper() {
        if (single_instance == null) {
            single_instance = new User_DataManager();
        }
        return single_instance;
    }

    public FirebaseFirestore getDbFireStore() {
        return dbFireStore;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }
}
