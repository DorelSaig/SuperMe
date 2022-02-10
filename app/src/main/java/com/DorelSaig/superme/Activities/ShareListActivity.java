package com.DorelSaig.superme.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DorelSaig.superme.Adapters.Adapter_Contacts;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.Misc.Utils;
import com.DorelSaig.superme.Objects.MyContact;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.DorelSaig.superme.SwipeToDeleteCallback;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;


public class ShareListActivity extends AppCompatActivity {

    private final MyDataManager dataManager = MyDataManager.getInstance();
    private final FirebaseFirestore db = dataManager.getDbFireStore();
    private final MyUser currentUser = dataManager.getCurrentUser();
    //private final MyList currentList = dataManager.getCurrentList();
    private final String currentListUid = dataManager.getCurrentListUid();
    private DocumentReference userRef;
    private DocumentReference listRef;


    private RecyclerView share_RECYC_contacts;
    private TextInputLayout addContact_EDT_phone;
    private MaterialButton addContact_BTN_search, addContact_BTN_add;
    private CountryCodePicker addContact_CCP_picker;
    private LottieAnimationView addContact_LOT_share;
    private TextView addContact_TXT_main_user;

    private ArrayList<MyContact> contactsArrayList;
    private Adapter_Contacts adapter_contacts;
    private ListenerRegistration contactsListener;

    private static final int CONTACT_PERMISSION_CODE = 1;
    private static final int CONTACT_PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_list);

        findViews();
        initButtons();
        initViewSettings();

        createRecycler();
        contactsArrayChangeListener();

        if (currentUser.getUid().equals(dataManager.getCurrentListCreator())) {
            enableSwipeToDeleteAndUndo();
        }
    }

    private void findViews() {
        share_RECYC_contacts = findViewById(R.id.share_RECYC_contacts);

        addContact_EDT_phone = findViewById(R.id.addContact_EDT_phone);

        addContact_BTN_search = findViewById(R.id.addContact_BTN_search);

        addContact_CCP_picker = findViewById(R.id.addContact_CCP_picker);

        addContact_CCP_picker.registerCarrierNumberEditText(addContact_EDT_phone.getEditText());

        addContact_BTN_add = findViewById(R.id.addContact_BTN_add);

        addContact_LOT_share = findViewById(R.id.addContact_LOT_share);




    }

    private void initButtons() {
        addContact_BTN_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkContactPermission()) {
                    //permission granted
                    pickContactIntent();
                } else {
                    //permission not granted, request
                    requestContactPermission();
                }
            }
        });

        addContact_BTN_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addContactToList();
                } catch (NumberParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initViewSettings() {

        //Put get the current Country Selected in the CCP and put the compatible example
        Phonenumber.PhoneNumber example  = PhoneNumberUtil.getInstance().getExampleNumberForType(addContact_CCP_picker.getSelectedCountryNameCode(), PhoneNumberUtil.PhoneNumberType.MOBILE);
        addContact_EDT_phone.setPlaceholderText(String.valueOf(example.getNationalNumber()));

        //Listen to any change in the CCP and update the placeholder with the compatible example
        addContact_CCP_picker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                Phonenumber.PhoneNumber example  = PhoneNumberUtil.getInstance().getExampleNumberForType(addContact_CCP_picker.getSelectedCountryNameCode(), PhoneNumberUtil.PhoneNumberType.MOBILE);
                addContact_EDT_phone.setPlaceholderText(String.valueOf(example.getNationalNumber()));
                Log.d("pttt","CCP Formatter:"  + example);
            }
        });

        //Creating a compatible validator for the phone number using the CCP library built in Validator
        addContact_EDT_phone.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!addContact_CCP_picker.isValidFullNumber()){
                    addContact_EDT_phone.setError("Not A valid number");
                }else{
                    addContact_EDT_phone.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void createRecycler() {
        contactsArrayList = new ArrayList<>();
        adapter_contacts = new Adapter_Contacts(this, contactsArrayList);

        share_RECYC_contacts.setLayoutManager(new LinearLayoutManager(this));
        share_RECYC_contacts.setHasFixedSize(true);
        share_RECYC_contacts.setItemAnimator(new DefaultItemAnimator());
        share_RECYC_contacts.setAdapter(adapter_contacts);
    }

    /**
     * Add Listener To The Contacts Recycler
     * Means, Every change in the list contacts list will update the recycler.
     */
    private void contactsArrayChangeListener() {
        CollectionReference collectionReference = db.collection(Constants.KEY_LISTS).document(currentListUid).collection(Constants.KEY_CONTACTS);
        contactsListener = collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (currentUser.getMyListsUids().isEmpty()) {
                    return;
                }
                if (error != null) {
                    Log.e("FireStore Error", error.getMessage());
                    return;
                }

                assert value != null;
                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {

                        case ADDED: {
                            MyContact myContact = dc.getDocument().toObject(MyContact.class);
                            if (contactsArrayList.isEmpty())
                                addContact_LOT_share.setVisibility(View.INVISIBLE);
                            contactsArrayList.add(myContact);
                            Log.d("pttt", "Contact Added To firebase");
                            break;
                        }

                        case MODIFIED: {
                            MyContact myContact = dc.getDocument().toObject(MyContact.class);
                            for (int i = 0; i < contactsArrayList.size(); i++) {
                                if (contactsArrayList.get(i).getPhone().equals(myContact.getPhone())) {
                                    contactsArrayList.set(i, myContact);
                                    adapter_contacts.notifyItemChanged(i);
                                    break;
                                }

                            }
                            contactsArrayList.add(myContact);
                            Log.d("pttt", "Contact Changed in firebase");
                            break;
                        }

                        case REMOVED: {
                            MyContact myContact = dc.getDocument().toObject(MyContact.class);
                            for (int i = 0; i < contactsArrayList.size(); i++) {
                                if (contactsArrayList.get(i).getPhone().equals(myContact.getPhone())) {
                                    contactsArrayList.remove(i);
                                    adapter_contacts.notifyItemRemoved(i);
                                    break;
                                }
                            }
                            Log.d("pttt", "Contact Removed in firebase");
                            break;
                        }

                        default: {
                            break;
                        }
                    }
                    adapter_contacts.notifyDataSetChanged();
                }
            }


        });
    }

    String theUid;

    /**
     * Add Contact to list by Phone Number
     */
    private void addContactToList() throws NumberParseException {
        /*
         * Get the input from Text field & Country Code Picker
         */
        Log.d("pttt","CCP Formatter:"  + addContact_CCP_picker.getFullNumberWithPlus());


        if(addContact_CCP_picker.isValidFullNumber()) {
            String fullNumber = addContact_CCP_picker.getFullNumberWithPlus();

            /*
             * Get Uid by phone number
             */
            dataManager.getRealTimeDB().getReference().child(Constants.KEY_PHONE_TO_UID).child(fullNumber).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @SuppressLint("Range")
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.getResult().exists()) {
                        //User Found In DB
                        theUid = String.valueOf(task.getResult().getValue());

                        Log.d("pttt", theUid);
                        //MyContact myContact = getContact(theUid);
                        userRef = db.collection(Constants.KEY_USERS).document(theUid);
                        listRef = db.collection(Constants.KEY_LISTS).document(dataManager.getCurrentListUid());

                        /*
                         * Get User by UID
                         */
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                MyUser theUserToAdd = documentSnapshot.toObject(MyUser.class);
                                Log.d("pttt", theUserToAdd.toString());
                                MyContact contact = new MyContact(theUserToAdd.getUid(), theUserToAdd.getName(), theUserToAdd.getPhoneNumber(), theUserToAdd.getProfileImgUrl());
                                Log.d("pttt", contact.toString());


                                /*
                                 * Update who the list is shared with
                                 */
                                listRef.update(Constants.FIELD_LIST_SHARED_WITH_LIST, FieldValue.arrayUnion(theUid)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        /*
                                         * Update Contact's array of lists.
                                         */
                                        userRef.update(Constants.FIELD_USER_MY_LISTS, FieldValue.arrayUnion(dataManager.getCurrentListUid())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                /*
                                                 * Add The Contact to the list's Contacts collection
                                                 */
                                                listRef.collection(Constants.KEY_CONTACTS).document(theUid).set(contact).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Log.d("pttt", "DocumentSnapshot Successfully written!");
                                                    }
                                                });
                                            }
                                        });

                                    }
                                });
                            }
                        });
                    } else {
                        Snackbar snackbar = Utils.showErrorSnackBar(share_RECYC_contacts, "אופס, מספר זה לא רשום אצלנו", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
            });
        } else {
            Snackbar snackbar = Utils.showErrorSnackBar(share_RECYC_contacts, "המספר שהוכנס אינו תקין, נסה שנית", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    /**
     * Initiate the Swipe To Delete functionality
     */
    private void enableSwipeToDeleteAndUndo() {
        //On Item Swipe Callback
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                MyContact contactToRemove = adapter_contacts.getItem(position);
                theUid = contactToRemove.getUid();
                listRef = db.collection(Constants.KEY_LISTS).document(currentListUid);
                contactsArrayList.remove(position);
                if (contactsArrayList.isEmpty()) {
                    addContact_LOT_share.setVisibility(View.VISIBLE);
                }

                Snackbar snackbar = Utils.showErrorSnackBar(share_RECYC_contacts, contactToRemove.getName() + getString(R.string.removed), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (contactsArrayList.isEmpty()) {
                                    addContact_LOT_share.setVisibility(View.INVISIBLE);
                                }
                                contactsArrayList.add(position, contactToRemove);
                                //fragment_RECYC_items.scrollToPosition(position);
                                adapter_contacts.notifyItemInserted(position);
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                if (event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT) {

                                    listRef.update(Constants.FIELD_LIST_SHARED_WITH_LIST, FieldValue.arrayRemove(theUid));
                                    db.collection(Constants.KEY_USERS).document(theUid).update(Constants.FIELD_USER_MY_LISTS, FieldValue.arrayRemove(currentListUid));
                                    listRef.collection(Constants.KEY_CONTACTS).document(theUid).delete();

                                }
                            }
                        });
                snackbar
                        .show();


                adapter_contacts.notifyDataSetChanged();

            }

        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(share_RECYC_contacts);
    }

    private MyContact getContact(String theUid) {
        MyContact contact = new MyContact("", "", "", "");
        db.collection(Constants.KEY_USERS).document(theUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                MyUser theUserToAdd = documentSnapshot.toObject(MyUser.class);
                theUserToAdd.toString();
                contact.setName(theUserToAdd.getName()).setPhone(theUserToAdd.getPhoneNumber()).setImgUrl(theUserToAdd.getProfileImgUrl());


            }
        });
        return contact;
    }


    private boolean checkContactPermission() {
        //Check if conatact Read permission granted
        boolean result = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS) == (PackageManager.PERMISSION_GRANTED);
        return result; // true if permission granted
    }

    private void requestContactPermission() {
        String[] permission = {Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this, permission, CONTACT_PERMISSION_CODE);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getData() != null) {
                Cursor cursor1, cursor2;
                Uri uri = result.getData().getData();

                cursor1 = getContentResolver().query(uri, null, null, null, null);
                if (cursor1.getCount() > 0) {
                    if (cursor1.moveToNext()) {
                        //getContactdetails
                        @SuppressLint("Range") String contactID = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));

                        @SuppressLint("Range") String hasPhone = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (Integer.parseInt(hasPhone) > 0) {
                            cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID, null, null);
                            while (cursor2.moveToNext()) {
                                String contactNumber = cursor2.getString(cursor2.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                //contactNumber = contactNumber.substring(1);
                                addContact_EDT_phone.getEditText().setText(contactNumber);
                            }
                            cursor2.close();
                        }
                    }
                }
                cursor1.close();
            }
        }

    });

    private void pickContactIntent() {
        //intent to pick contact
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        activityResultLauncher.launch(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //handle [permission request result
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContactIntent();
            } else {
                Toast.makeText(this, "Permission Denied ...", Toast.LENGTH_SHORT).show();
            }
        }
    }


}