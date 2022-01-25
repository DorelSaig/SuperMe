package com.DorelSaig.superme.Firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.DorelSaig.superme.Objects.MyUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class MyDataManager {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore dbFireStore;
    private FirebaseStorage storage;



    private MyUser currentUser;
    private String currentListUid;


    private static MyDataManager single_instance = null;

    private MyDataManager(){
        firebaseAuth = FirebaseAuth.getInstance();
        dbFireStore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static MyDataManager getInstance() {
        return single_instance;
    }

    public static MyDataManager initHelper() {
        if (single_instance == null) {
            single_instance = new MyDataManager();
        }
        return single_instance;
    }

    public FirebaseFirestore getDbFireStore() {
        return dbFireStore;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public MyUser getCurrentUser() {
        return currentUser;
    }

    public MyDataManager setCurrentUser(MyUser currentUser) {
        this.currentUser = currentUser;
        return this;
    }

    public String getCurrentListUid() {
        return currentListUid;
    }

    public MyDataManager setCurrentListUid(String currentListUid) {
        this.currentListUid = currentListUid;
        return this;
    }



    public FirebaseStorage getStorage() {
        return storage;
    }

    public void loadUserFromDB() {
        // Successfully signed in
        FirebaseUser user = firebaseAuth.getCurrentUser();
        DocumentReference docRef = dbFireStore.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.d("pttt", "DocumentSnapshot data: " + documentSnapshot.getData());
                    MyUser loadedUser = documentSnapshot.toObject(MyUser.class);
                    setCurrentUser(loadedUser);

                } else {
                    Log.d("pttt", "No such document");
                    Log.d("pttt", user.getUid().toString());
                }

            }

        });
    }


    public void userListsChange() {
        DocumentReference docRef = dbFireStore.collection("users").document(currentUser.getUid());
        docRef.update("myListsUids", currentUser.getMyListsUids())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("pttt", "Data Updated Successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("pttt", "Error updating document", e);
                    }
                });
    }
}
