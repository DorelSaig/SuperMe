package com.DorelSaig.superme.Firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.Objects.MyUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

public class MyDataManager {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore dbFireStore;
    private final FirebaseStorage storage;
    private final FirebaseDatabase realTimeDB;



    private MyUser currentUser;
    private String currentListUid;
    private String currentListCreator;
    private MyItem currentItem;
    private String currentCategoryUid;
    private String currentListTitle;
    private String token;


    private static MyDataManager single_instance = null;

    private MyDataManager(){
        firebaseAuth = FirebaseAuth.getInstance();
        dbFireStore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        realTimeDB = FirebaseDatabase.getInstance("https://superme-e69d5-default-rtdb.europe-west1.firebasedatabase.app/");
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

    //Firebase Getters
    public FirebaseFirestore getDbFireStore() {
        return dbFireStore;
    }

    public FirebaseDatabase getRealTimeDB() {
        return realTimeDB;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }


    //My Data Base Helpers
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

    public void setCurrentListUid(String currentListUid) {
        this.currentListUid = currentListUid;
    }

    public String getCurrentListTitle() {
        return currentListTitle;
    }

    public void setCurrentListTitle(String currentListTitle) {
        this.currentListTitle = currentListTitle;
    }

    public MyItem getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(MyItem currentItem) {
        this.currentItem = currentItem;
    }

    public String getCurrentCategoryUid() {
        return currentCategoryUid;
    }

    public void setCurrentCategoryUid(String currentCategory) {
        this.currentCategoryUid = currentCategory;
    }

    public void setCurrentListCreator(String creatorUid) {
        this.currentListCreator = creatorUid;
    }

    public String getCurrentListCreator() {
        return currentListCreator;
    }

    //MyDataManager Methods

    /**
     * Method will load the connected user's data from database. and update his current device token for Cloud messaging
     */
    public void loadUserFromDB() {
        // Successfully signed in

        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                 token = s;
                 DatabaseReference myRef = getRealTimeDB().getReference(Constants.KEY_UID_TO_TOKENS).child(user.getUid());
                 myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<DataSnapshot> task) {
                         if(task.isSuccessful()){
                             myRef.setValue(token);
                             Log.d("pttt", "token is : " + token);
                         }
                     }
                 });
            }
        });


        DocumentReference docRef = dbFireStore.collection(Constants.KEY_USERS).document(user.getUid());
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

    /**
     * Method which will be called whenever there is a change in the user's list of lists and need to update the database about the change
     */
    public void userListsChange() {
        DocumentReference docRef = dbFireStore.collection(Constants.KEY_USERS).document(currentUser.getUid());
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
