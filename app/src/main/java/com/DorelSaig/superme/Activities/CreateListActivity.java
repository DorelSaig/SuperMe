package com.DorelSaig.superme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Objects.MyList;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class CreateListActivity extends AppCompatActivity {

    private FloatingActionButton createList_FAB_profile_pic;
    private ShapeableImageView createList_IMG_user;
    private TextInputLayout form_EDT_name;
    private MaterialButton panel_BTN_create;
    private CircularProgressIndicator createList_BAR_progress;

    private final MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db;
    private final MyUser currentUser = MyDataManager.getInstance().getCurrentUser();

    private MyList tempList;
    private StorageReference userRef;

    private boolean isSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);

        isSubmit = false;

        findViews();
        initButtons();

        tempList = new MyList("No Title", currentUser.getUid());
        userRef = dataManager.getStorage().getReference().child("lists_covers").child(tempList.getListUid());

        db = dataManager.getDbFireStore();

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
                dataManager.userListsChange(tempList.getListUid());

                storeListInDB(tempList);
                isSubmit = true;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!isSubmit){
            userRef.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("pttt", "onFailure: "+ e.getMessage());
                }
            });
        }

    }

    private void choseCover() {
        ImagePicker.with(CreateListActivity.this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .crop(2f, 1f)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        createList_BAR_progress.setVisibility(View.VISIBLE);
        panel_BTN_create.setEnabled(false);



        Uri uri = data.getData();
        createList_IMG_user.setImageURI(uri);

        // [START upload_memory]
        // Get the data from an ImageView as bytes
        createList_IMG_user.setDrawingCacheEnabled(true);
        createList_IMG_user.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) createList_IMG_user.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();


        UploadTask uploadTask = userRef.putBytes(bytes);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    userRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            createList_BAR_progress.setVisibility(View.INVISIBLE);
                            panel_BTN_create.setEnabled(true);
                            tempList.setImage_cover(uri.toString());

                        }
                    });

                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(CreateListActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // [END upload_memory]
    }



    private void storeListInDB(MyList listToStore) {
        db.collection("lists")
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

    private void findViews() {

        createList_FAB_profile_pic = findViewById(R.id.createList_FAB_profile_pic);
        createList_IMG_user = findViewById(R.id.createList_IMG_user);
        form_EDT_name = findViewById(R.id.form_EDT_name);
        panel_BTN_create = findViewById(R.id.panel_BTN_create);
        createList_BAR_progress = findViewById(R.id.createList_BAR_progress);

    }
}