package com.DorelSaig.superme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.Misc.Utils;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.R;
import com.DorelSaig.superme.Listeners.UploadIMGListener;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

public class EditItemActivity extends AppCompatActivity {

    private final MyDataManager U_Manager = MyDataManager.getInstance();
    private final FirebaseFirestore db = U_Manager.getDbFireStore();

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
    private MyItem chosenItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        chosenItem = U_Manager.getCurrentItem();

        findViews();
        configEditFields();
        initButtons();

        fillViewComponents(chosenItem);

        //Load the chosen item information to temp item
        tempItem = new MyItem(chosenItem.getItemTitle(), 0, U_Manager.getCurrentListUid());
        tempItem.setItemImage(chosenItem.getItemImage());
        tempItem.setItemUid(chosenItem.getItemUid());
        tempItem.setItemIcon(chosenItem.getItemIcon());
    }

    /**
     * Fill the view components with the chosen item current information
     *
     * @param currentItem The current item which chosen to be edited
     */
    private void fillViewComponents(MyItem currentItem) {
        editItem_TXT_title.setText(currentItem.getItemTitle());
        editItem_EDT_amount.getEditText().setText(String.valueOf(currentItem.getAmount()));
        editItem_EDT_amount.setSuffixText(currentItem.getAmountSuffix());
        editItem_EDT_notes.getEditText().setText(currentItem.getNotes());

        if (currentItem.getAmountSuffix().equals(getString(R.string.ones))) {
            editItem_TGBTN_ones.setChecked(true);
        }

        Glide
                .with(this)
                .load(currentItem.getItemImage()) //maybe cast to URI
                .into(editItem_IMG_user);

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

    private void configEditFields() {

        InputFilter noteLengthFilter = new InputFilter.LengthFilter(50);
        InputFilter amountLengthFilter = new InputFilter.LengthFilter(5);

        editItem_EDT_notes.getEditText().setFilters(new InputFilter[]{noteLengthFilter});
        editItem_EDT_amount.getEditText().setFilters(new InputFilter[]{amountLengthFilter});
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
                if (!editItem_EDT_amount.getEditText().getText().toString().isEmpty()) {
                    tempItem.setAmount(Float.parseFloat(editItem_EDT_amount.getEditText().getText().toString()));
                } else {
                    tempItem.setAmount(0);
                }
                tempItem.setAmountSuffix(editItem_EDT_amount.getSuffixText().toString());
                tempItem.setCreatorUid("System");
                if (editItem_EDT_notes.getEditText().getText().toString().isEmpty()) {
                    tempItem.setNotes("");
                } else
                    tempItem.setNotes(editItem_EDT_notes.getEditText().getText().toString());


                storeItemInDB(tempItem);
            }
        });

        toggleButton.setSingleSelection(true);
        editItem_TGBTN_kilo.setChecked(true);

        editItem_TGBTN_kilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editItem_TGBTN_kilo.isChecked()) {
                    editItem_EDT_amount.setSuffixText(getString(R.string.kilos));
                    editItem_EDT_amount.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                }
            }
        });

        editItem_TGBTN_ones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editItem_TGBTN_ones.isChecked()) {
                    editItem_EDT_amount.setSuffixText(getString(R.string.ones));
                    editItem_EDT_amount.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }
        });
    }

    /**
     * Load ImagePicker activity to choose the category cover
     */
    private void choseCover() {
        ImagePicker.with(EditItemActivity.this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .crop(2f, 1f)
                .maxResultSize(1080, 1080)
                //Final image resolution will be less than 1080 x 1080(Optional)
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

        editItem_BAR_progress.setVisibility(View.VISIBLE);
        editItem_BTN_create.setEnabled(false);
        StorageReference storageRef = U_Manager.getStorage().getReference().child(Constants.KEY_ITEMS_IMAGE).child(tempItem.getItemUid());

        UploadIMGListener uploadIMGListener = new UploadIMGListener() {
            @Override
            public void uploadDone(String theUrl) {
                //View Indicates the process of the image uploading Done
                // by removing the progress bar indicator and making the button enabled
                editItem_BAR_progress.setVisibility(View.INVISIBLE);
                editItem_BTN_create.setEnabled(true);

                // Set the image URL to the object we created
                tempItem.setItemImage(theUrl);
            }
        };

        if (data != null) {
            Utils.imageUploading(uploadIMGListener, data, editItem_IMG_user, storageRef, EditItemActivity.this);
        } else {
            Toast.makeText(EditItemActivity.this, "Error: Null Data Received", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Function will store the given item to Firebase actually will overwrite the chosen item with
     * the updated temp item.
     *
     * @param itemToStore The updated item which we want to store instead of the chosen item we wanted to edit.
     */
    private void storeItemInDB(MyItem itemToStore) {
        db.collection(Constants.KEY_LISTS)
                .document(U_Manager.getCurrentListUid())
                .collection(Constants.KEY_ITEMS)
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
}