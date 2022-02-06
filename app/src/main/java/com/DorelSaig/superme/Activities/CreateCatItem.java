package com.DorelSaig.superme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class CreateCatItem extends AppCompatActivity {

    private final MyDataManager dataManager = MyDataManager.getInstance();
    private final FirebaseFirestore db = dataManager.getDbFireStore();
    private final MyUser currentUser = dataManager.getCurrentUser();

    private FloatingActionButton createItem_FAB_profile_pic;
    private ShapeableImageView createItem_IMG_user;
    private TextInputLayout createItem_EDT_title;
    private TextInputLayout createItem_EDT_amount;
    private TextInputLayout createItem_EDT_notes;
    private MaterialButton createItem_BTN_create;
    private CircularProgressIndicator createItem_BAR_progress;

    private MaterialButtonToggleGroup toggleButton;
    private MaterialButton createItem_TGBTN_ones;
    private MaterialButton createItem_TGBTN_kilo;

    private MyItem tempItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cat_item);

        findViews();
        configEditFields();
        initButtons();
        Log.d("pttt", "On Create CatItem");
        tempItem = new MyItem("No Title", 0, dataManager.getCurrentListUid());
    }

    private void findViews() {

        createItem_FAB_profile_pic = findViewById(R.id.createItem_FAB_profile_pic);
        createItem_IMG_user = findViewById(R.id.createItem_IMG_user);
        createItem_EDT_title = findViewById(R.id.createItem_EDT_title);
        createItem_EDT_amount = findViewById(R.id.createItem_EDT_amount);
        createItem_EDT_notes = findViewById(R.id.createItem_EDT_notes);
        createItem_BTN_create = findViewById(R.id.createItem_BTN_create);
        createItem_BAR_progress = findViewById(R.id.createItem_BAR_progress);

        toggleButton = findViewById(R.id.toggleButton);
        createItem_TGBTN_kilo = findViewById(R.id.createItem_TGBTN_kilo);
        createItem_TGBTN_ones = findViewById(R.id.createItem_TGBTN_ones);
    }

    /**
     * Config the Edit-fields filters - Max Length
     */
    private void configEditFields() {

        InputFilter noteLengthFilter = new InputFilter.LengthFilter(50);
        InputFilter amountLengthFilter = new InputFilter.LengthFilter(5);
        InputFilter titleLengthFilter = new InputFilter.LengthFilter(20);

        createItem_EDT_notes.getEditText().setFilters(new InputFilter[]{noteLengthFilter});
        createItem_EDT_amount.getEditText().setFilters(new InputFilter[]{amountLengthFilter});
        createItem_EDT_title.getEditText().setFilters(new InputFilter[]{titleLengthFilter});
    }

    private void initButtons() {
        createItem_FAB_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseCover();
            }
        });

        createItem_IMG_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseCover();
            }
        });

        createItem_BTN_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String theTitle = createItem_EDT_title.getEditText().getText().toString();
                tempItem.setItemTitle(theTitle);
                tempItem.setCreatorUid(currentUser.getUid());


                storeItemInDB(tempItem);
            }
        });

        toggleButton.setSingleSelection(true);
        createItem_TGBTN_kilo.setChecked(true);

        createItem_TGBTN_kilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createItem_TGBTN_kilo.isChecked()) {
                    createItem_EDT_amount.setSuffixText(getString(R.string.kilos));
                    createItem_EDT_amount.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                }
            }
        });

        createItem_TGBTN_ones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createItem_TGBTN_ones.isChecked()) {
                    createItem_EDT_amount.setSuffixText(getString(R.string.ones));
                    createItem_EDT_amount.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }
        });
    }

    /**
     * Load ImagePicker activity to choose the category cover
     */
    private void choseCover() {
        ImagePicker.with(CreateCatItem.this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .crop(5f, 4f)
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

        createItem_BAR_progress.setVisibility(View.VISIBLE);
        createItem_BTN_create.setEnabled(false);
        StorageReference userRef = dataManager.getStorage().getReference().child("system_items_image").child(tempItem.getItemUid());

        Uri uri = data.getData();
        createItem_IMG_user.setImageURI(uri);

        // [START upload_memory]
        // Get the data from an ImageView as bytes
        createItem_IMG_user.setDrawingCacheEnabled(true);
        createItem_IMG_user.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) createItem_IMG_user.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();


        UploadTask uploadTask = userRef.putBytes(bytes);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    userRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            createItem_BAR_progress.setVisibility(View.INVISIBLE);
                            createItem_BTN_create.setEnabled(true);
                            tempItem.setItemImage(uri.toString());

                        }
                    });


                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(CreateCatItem.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * Function will store the given item to Firestore.
     *
     * @param itemToStore is the item you wish to store in the Firebase Firestore
     */
    private void storeItemInDB(@NonNull MyItem itemToStore) {
        db.collection("categories")
                .document(dataManager.getCurrentCategoryUid())
                .collection("items")
                .document(itemToStore.getItemUid())
                .set(itemToStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("pttt", "DocumentSnapshot Successfully written!");
                        //startActivity(new Intent(CreateListActivity.this, MainActivity.class));
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