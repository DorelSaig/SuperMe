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
import android.widget.TextView;
import android.widget.Toast;

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.bumptech.glide.Glide;
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

public class EditItemActivity extends AppCompatActivity {

    private MyDataManager U_Manager = MyDataManager.getInstance();
    private FirebaseFirestore db = U_Manager.getDbFireStore();
    private MyUser currentUser = U_Manager.getCurrentUser();

    private FloatingActionButton editItem_FAB_profile_pic;
    private ShapeableImageView editItem_IMG_user;
    private TextView editItem_TXT_title;
    private TextInputLayout editItem_EDT_amount;
    private TextInputLayout editItem_EDT_notes;
    private MaterialButton editItem_BTN_create;
    private CircularProgressIndicator editItem_BAR_progress;

    private MaterialButtonToggleGroup toggleButton;
    private MaterialButton editItem_TGBTN_ones;
    private MaterialButton editItem_TGBTN_kilo;

    private MyItem tempItem;
    private MyItem choosenItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        choosenItem = U_Manager.getCurrentItem();

        findViews();
        configEditFields();
        initButtons();

        editItem_TXT_title.setText(choosenItem.getItemTitle());

        Glide
                .with(this)
                .load(choosenItem.getItemImage()) //maybe cast to URI
                .into(editItem_IMG_user);

        tempItem = new MyItem(choosenItem.getItemTitle(), 0, U_Manager.getCurrentListUid());
        tempItem.setItemImage(choosenItem.getItemImage());
        tempItem.setItemUid(choosenItem.getItemUid());
    }

    private void configEditFields() {

        InputFilter noteLengthFilter = new InputFilter.LengthFilter(50);
        InputFilter amountLengthFilter = new InputFilter.LengthFilter(5);

        editItem_EDT_notes.getEditText().setFilters(new InputFilter[]{noteLengthFilter});
        editItem_EDT_amount.getEditText().setFilters(new InputFilter[]{amountLengthFilter});
    }

    private void choseCover() {
        ImagePicker.with(EditItemActivity.this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .crop(2f, 1f)
                .maxResultSize(1080, 1080)
                //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        editItem_BAR_progress.setVisibility(View.VISIBLE);
        editItem_BTN_create.setEnabled(false);
        StorageReference userRef = U_Manager.getStorage().getReference().child("items_image").child(tempItem.getItemUid());

        Uri uri = data.getData();
        editItem_IMG_user.setImageURI(uri);

        // [START upload_memory]
        // Get the data from an ImageView as bytes
        editItem_IMG_user.setDrawingCacheEnabled(true);
        editItem_IMG_user.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) editItem_IMG_user.getDrawable()).getBitmap();
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
                            editItem_BAR_progress.setVisibility(View.INVISIBLE);
                            editItem_BTN_create.setEnabled(true);
                            tempItem.setItemImage(uri.toString());

                        }
                    });



                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(EditItemActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void storeItemInDB(MyItem itemToStore) {
        db.collection("lists")
                .document(U_Manager.getCurrentListUid())
                .collection("items")
                .document(itemToStore.getItemUid())
                .set(itemToStore)
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

        editItem_TXT_title = findViewById(R.id.editItem_TXT_title);

        editItem_FAB_profile_pic = findViewById(R.id.editItem_FAB_profile_pic);
        editItem_IMG_user = findViewById(R.id.editItem_IMG_user);
        editItem_EDT_amount = findViewById(R.id.editItem_EDT_amount);
        editItem_EDT_notes = findViewById(R.id.editItem_EDT_notes);
        editItem_BTN_create = findViewById(R.id.editItem_BTN_create);
        editItem_BAR_progress = findViewById(R.id.editItem_BAR_progress);

        toggleButton = findViewById(R.id.toggleButton);
        editItem_TGBTN_kilo = findViewById(R.id.editItem_TGBTN_kilo);
        editItem_TGBTN_ones = findViewById(R.id.editItem_TGBTN_ones);
    }

    private void initButtons() {
        editItem_FAB_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseCover();
            }
        });

        editItem_IMG_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                choseCover();
            }
        });

        editItem_BTN_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempItem.setAmount(Float.parseFloat(editItem_EDT_amount.getEditText().getText().toString()));
                tempItem.setAmountSuffix(editItem_EDT_amount.getSuffixText().toString());
                tempItem.setCreatorUid("System");
                tempItem.setNotes(editItem_EDT_notes.getEditText().getText().toString());


                storeItemInDB(tempItem);
            }
        });

        toggleButton.setSingleSelection(true);
        editItem_TGBTN_kilo.setChecked(true);

        editItem_TGBTN_kilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editItem_TGBTN_kilo.isChecked()) {
                    editItem_EDT_amount.setSuffixText(getString(R.string.kilos));
                    editItem_EDT_amount.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                }
            }
        });

        editItem_TGBTN_ones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editItem_TGBTN_ones.isChecked()) {
                    editItem_EDT_amount.setSuffixText(getString(R.string.ones));
                    editItem_EDT_amount.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }
        });
    }

}