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
import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.Objects.MyCategory;
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

/**
 * Admin Activity Only.
 */
public class CreateCategoryActivity extends AppCompatActivity {

    private FloatingActionButton createList_FAB_profile_pic;
    private ShapeableImageView createList_IMG_user;
    private TextInputLayout form_EDT_name;
    private MaterialButton panel_BTN_create;
    private CircularProgressIndicator createList_BAR_progress;

    private final MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db;
    private final MyUser currentUser = MyDataManager.getInstance().getCurrentUser();

    private MyCategory tempCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_catagory);

        db = dataManager.getDbFireStore();
        findViews();
        initButtons();

        tempCategory = new MyCategory("Temp List");



    }


    private void findViews() {

        createList_FAB_profile_pic = findViewById(R.id.createList_FAB_profile_pic);
        createList_IMG_user = findViewById(R.id.createList_IMG_user);
        form_EDT_name = findViewById(R.id.form_EDT_name);
        panel_BTN_create = findViewById(R.id.panel_BTN_create);
        createList_BAR_progress = findViewById(R.id.createList_BAR_progress);

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
                tempCategory.setTitle(theTitle);


                storeCatInDB(tempCategory);
            }
        });
    }

    /**
     * Load ImagePicker activity to choose the category cover
     */
    private void choseCover() {
            ImagePicker.with(CreateCategoryActivity.this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .crop(1f, 1f)
                    .maxResultSize(1080, 1080)
                    //Final image resolution will be less than 1080 x 1080(Optional)
                    .start();

    }


    /**
     *Results From ImagePicker will be catch here
     * will place the image in the relevant Image View
     * Right after that, will catch the image bytes back from the view and store them in the Firebase Storage.
     * After successful upload will update the Object Url Field
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        createList_BAR_progress.setVisibility(View.VISIBLE);
        panel_BTN_create.setEnabled(false);


        StorageReference userRef = dataManager.getStorage().getReference().child(Constants.KEY_CATEGORY_COVERS).child(tempCategory.getCatUid());


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
                            tempCategory.setCat_cover(uri.toString());

                        }
                    });



                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(CreateCategoryActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    /**
     * Function will store the given Category to Firestore.
     *
     * @param CatToStore is the category you wish to store in the Firebase Firestore
     */
    private void storeCatInDB(MyCategory CatToStore) {
        db.collection("categories")
                .document(CatToStore.getCatUid())
                .set(CatToStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("pttt", "Category Successfully written!");
                        //startActivity(new Intent(CreateListActivity.this, MainActivity.class));
                        finish();
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("pttt", "Error adding Category document", e);
                    }
                });
    }




}