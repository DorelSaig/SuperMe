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

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private MaterialButton panel_BTN_update;
    private TextInputLayout form_EDT_name;
    private MyDataManager userDataManager;
    private FirebaseFirestore db;

    //Profile Picture
    private FloatingActionButton signup_FAB_profile_pic;
    private CircleImageView signup_IMG_user;

    private String myDownloadUri;

    private boolean isSubmit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        findViews();
        initButtons();

        userDataManager = MyDataManager.getInstance();
        db = userDataManager.getDbFireStore();
    }

    @Override
    protected void onStop() {
        super.onStop();



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //TODO erase UID doc from DB
        userDataManager.getFirebaseAuth().signOut();
    }

    private void findViews() {

        panel_BTN_update = findViewById(R.id.panel_BTN_update);
        form_EDT_name = findViewById(R.id.form_EDT_name);

        signup_IMG_user = findViewById(R.id.signup_IMG_user);
        signup_FAB_profile_pic = findViewById(R.id.signup_FAB_profile_pic);
    }

    private void initButtons() {

        panel_BTN_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = form_EDT_name.getEditText().getText().toString();
                String userID = userDataManager.getFirebaseAuth().getCurrentUser().getUid();
                String userPhone = userDataManager.getFirebaseAuth().getCurrentUser().getPhoneNumber();
                MyUser tempMyUser = new MyUser(userID, userName, userPhone);
                tempMyUser.setProfileImgUrl(myDownloadUri);
                userDataManager.setCurrentUser(tempMyUser);
                storeUserInDB(tempMyUser);
                isSubmit = true;
            }
        });

        signup_IMG_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panel_BTN_update.setEnabled(false);
                ImagePicker.with(SignUpActivity.this)
                        .crop(1f, 1f)	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

    }

//    ActivityResultLauncher<Intent> launcher =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
//                if (result.getResultCode() == RESULT_OK) {
//                    Uri uri = result.getData().getData();
//                    // Use the uri to load the image
//                    signup_IMG_user.setImageURI(uri);
//                    uri.get
//                } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
//                    // Use ImagePicker.Companion.getError(result.getData()) to show an error
//                }
//            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        StorageReference userRef = userDataManager.getStorage().getReference().child("profile_pictures").child(userDataManager.getFirebaseAuth().getCurrentUser().getUid());

        Uri uri = data.getData();

        signup_IMG_user.setImageURI(uri);
        // [START upload_memory]
        // Get the data from an ImageView as bytes
        signup_IMG_user.setDrawingCacheEnabled(true);
        signup_IMG_user.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) signup_IMG_user.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();

        UploadTask uploadTask = userRef.putBytes(bytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                userRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        myDownloadUri = uri.toString();
                        panel_BTN_update.setEnabled(true);
                    }
                });

            }
        });
        // [END upload_memory]
    }

    private void storeUserInDB(MyUser userToStore) {
        db.collection("users")
                .document(userToStore.getUid())
                .set(userToStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("pttt", "DocumentSnapshot Successfully written!");
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
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