package com.DorelSaig.superme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.Misc.Utils;
import com.DorelSaig.superme.Objects.MyList;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.DorelSaig.superme.Listeners.UploadIMGListener;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

public class CreateListActivity extends AppCompatActivity {

    private FloatingActionButton createList_FAB_profile_pic;
    private ShapeableImageView createList_IMG_user;
    private TextInputLayout form_EDT_name;
    private MaterialButton panel_BTN_create;
    private CircularProgressIndicator createList_BAR_progress;

    private final MyDataManager dataManager = MyDataManager.getInstance();
    private final FirebaseFirestore db = dataManager.getDbFireStore();
    private final MyUser currentUser = MyDataManager.getInstance().getCurrentUser();

    private MyList tempList;
    private StorageReference storageRef;

    private boolean isSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);

        isSubmit = false;

        findViews();
        initButtons();

        tempList = new MyList("No Title", currentUser.getUid());

        storageRef = dataManager.getStorage()
                .getReference()
                .child(Constants.KEY_LIST_COVERS)
                .child(tempList.getListUid());

    }

    private void findViews() {

        createList_FAB_profile_pic = findViewById(R.id.createCat_FAB_profile_pic);
        createList_IMG_user = findViewById(R.id.createCat_IMG_user);
        form_EDT_name = findViewById(R.id.form_EDT_name);
        panel_BTN_create = findViewById(R.id.panel_BTN_create);
        createList_BAR_progress = findViewById(R.id.createCat_BAR_progress);

    }

    private void initButtons() {
        createList_FAB_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseCover();
            }
        });

        createList_IMG_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                choseCover();
            }
        });

        panel_BTN_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String theTitle = form_EDT_name.getEditText().getText().toString();
                tempList.setTitle(theTitle);
                currentUser.addToListUid(tempList.getListUid());
                dataManager.userListsChange();

                storeListInDB(tempList);
                isSubmit = true;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isSubmit) {
            storageRef.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("pttt", "onFailure: " + e.getMessage());
                }
            });
        }

    }

    /**
     * Load ImagePicker activity to choose the category cover
     */
    private void choseCover() {
        ImagePicker.with(CreateListActivity.this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .crop(2f, 1f)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    /**
     * Results From ImagePicker will be catch here
     * will place the image in the relevant Image View
     * Right after that, will catch the image bytes back from the view and update them in the Firebase Storage.
     * After successful upload will update the Object Url Field
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        createList_BAR_progress.setVisibility(View.VISIBLE);
        panel_BTN_create.setEnabled(false);

        UploadIMGListener uploadIMGListener = new UploadIMGListener() {
            @Override
            public void uploadDone(String theUrl) {
                //View Indicates the process of the image uploading Done
                // by removing the progress bar indicator and making the button enabled
                createList_BAR_progress.setVisibility(View.INVISIBLE);
                panel_BTN_create.setEnabled(true);

                // Set the image URL to the object we created
                tempList.setImage_cover(theUrl);

            }
        };

        if (data != null) {
            Utils.imageUploading(uploadIMGListener, data, createList_IMG_user, storageRef, CreateListActivity.this);
        } else {
            Toast.makeText(CreateListActivity.this, "Error: Null Data Received", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Function will store the given List to Firestore.
     *
     * @param listToStore is the list you wish to store in the Firebase Firestore
     */
    private void storeListInDB(MyList listToStore) {
        db.collection(Constants.KEY_LISTS)
                .document(listToStore.getListUid())
                .set(listToStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("pttt", "DocumentSnapshot Successfully written!");
                        //startActivity(new Intent(CreateListActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("pttt", "Error adding document", e);
                    }
                });
    }


}