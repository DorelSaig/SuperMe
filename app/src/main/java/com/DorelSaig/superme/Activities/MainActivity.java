package com.DorelSaig.superme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Fragments.HomeListsFragment;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView main_LST_lists;
    private FloatingActionButton toolbar_FAB_add;
    private BottomAppBar panel_AppBar_bottom;
    private DrawerLayout drawer_layout;
    private NavigationView nav_view;
    private MaterialToolbar panel_Toolbar_Top;
    private RelativeLayout panel_VIEW_message;
    private MaterialButton panel_BTN_chat_back;

    private boolean isUp = false;

    private View header;
    private FloatingActionButton navigation_header_container_FAB_profile_pic;
    private MaterialTextView header_TXT_username;
    private CircleImageView header_IMG_user;
    private CircularProgressIndicator header_BAR_progress;


    private MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db = dataManager.getDbFireStore();
    private MyUser currentUser = dataManager.getCurrentUser();

    private FragmentContainerView main_FRG_container;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);

        setSupportActionBar(panel_AppBar_bottom);
        setSupportActionBar(panel_Toolbar_Top);


        findViews();
        initButtons();

        if(MyDataManager.getInstance().getCurrentUser()!=null){
        Log.d("pttt", "User Loaded Name From Login: " + MyDataManager.getInstance().getCurrentUser().getName().toString());
        } else {

            Log.d("pttt", "Not Loaded User");
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            finish();
        }


        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent  = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }



        MaterialShapeDrawable navViewBack = (MaterialShapeDrawable) nav_view.getBackground();
        navViewBack.setShapeAppearanceModel(
                navViewBack.getShapeAppearanceModel().toBuilder().setTopLeftCorner(CornerFamily.ROUNDED, 60.0F)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 60.0F).build()
        );


        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_FRG_container, HomeListsFragment.class, null)
                .commit();



        Log.d("pttt", "User Loaded From Lists: " + dataManager.getCurrentUser().getName().toString());
        header_TXT_username.setText(dataManager.getCurrentUser().getName().toString());
        StorageReference bring = dataManager.getStorage().getReference().child(dataManager.getCurrentUser().getUid().toString());

        Uri myUri =  Uri.parse(dataManager.getCurrentUser().getProfileImgUrl());

        Glide.with(MainActivity.this)
                .load(myUri)
                .into(header_IMG_user);

    }



    @Override
    protected void onStart() {
        super.onStart();

    }


    private void findViews() {

        panel_Toolbar_Top = findViewById(R.id.panel_Toolbar_Top);
        panel_AppBar_bottom = findViewById(R.id.panel_AppBar_bottom);
        drawer_layout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        toolbar_FAB_add = findViewById(R.id.toolbar_FAB_add);
        panel_VIEW_message = findViewById(R.id.panel_VIEW_message);
        panel_BTN_chat_back = findViewById(R.id.panel_BTN_chat_back);

        //main_LST_lists = findViewById(R.id.main_LST_lists);
        main_FRG_container = findViewById(R.id.main_FRG_container);

        header = nav_view.getHeaderView(0);
        navigation_header_container_FAB_profile_pic = (FloatingActionButton) header.findViewById(R.id.navigation_header_container_FAB_profile_pic);
        header_TXT_username = header.findViewById(R.id.header_TXT_username);
        header_IMG_user = header.findViewById(R.id.header_IMG_user);
        header_BAR_progress = header.findViewById(R.id.header_BAR_progress);
    }

    private void initButtons() {

        panel_Toolbar_Top.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Back",Toast.LENGTH_LONG).show();

            }
        });

        panel_AppBar_bottom.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer_layout.openDrawer(drawer_layout.getForegroundGravity());
            }
        });

        toolbar_FAB_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Add",Toast.LENGTH_LONG).show();
                createNewList();
            }
        });

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.item1:
                        Toast.makeText(MainActivity.this, "Profile",Toast.LENGTH_LONG).show();
                        break;
                    case R.id.item2:
                        AuthUI.getInstance()
                                .signOut(MainActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @SuppressLint("RestrictedApi")
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // user is now signed out
                                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                        Toast.makeText(MainActivity.this,"Signed Out",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                        break;
                }
                return true;
            }

        });

        navigation_header_container_FAB_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"profile_picture",Toast.LENGTH_SHORT).show();
                changePic();
            }
        });

        header_IMG_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePic();
            }
        });

        panel_AppBar_bottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_chat:
                        if(isUp){
                            slideDown(panel_VIEW_message);
                            toolbar_FAB_add.setVisibility(View.VISIBLE);
                        } else {
                            slideUp(panel_VIEW_message);
                            toolbar_FAB_add.setVisibility(View.INVISIBLE);
                        }
                        isUp = !isUp;
                        break;
                }
                return false;
            }
        });

        panel_BTN_chat_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideDown(panel_VIEW_message);
                toolbar_FAB_add.setVisibility(View.VISIBLE);
                isUp = !isUp;
            }
        });


    }


    private void createNewList() {
        startActivity(new Intent(MainActivity.this, CreateListActivity.class));
    }

    private void changePic() {
        ImagePicker.with(MainActivity.this)
                .crop(1f, 1f)   			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)
                //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        header_BAR_progress.setVisibility(View.VISIBLE);

        StorageReference userRef = dataManager.getStorage().getReference().child("profile_pictures").child(dataManager.getFirebaseAuth().getCurrentUser().getUid());

        Uri uri = data.getData();
        header_IMG_user.setImageURI(uri);

        // [START upload_memory]
        // Get the data from an ImageView as bytes
        header_IMG_user.setDrawingCacheEnabled(true);
        header_IMG_user.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) header_IMG_user.getDrawable()).getBitmap();
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

                            currentUser.setProfileImgUrl(uri.toString());
                            dataManager.getDbFireStore().collection("users")
                                    .document(dataManager.getCurrentUser().getUid())
                                    .update("profileImgUrl", uri.toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                Toast.makeText(MainActivity.this, "Image Save in Database Successfully...", Toast.LENGTH_SHORT).show();
                                            }else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                        }
                    });

                    header_BAR_progress.setVisibility(View.INVISIBLE);

                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle unsuccessful uploads
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                // ...
//
//                header_BAR_progress.setVisibility(View.INVISIBLE);
//                Glide.with(MainActivity.this)
//                        .load(bytes)
//                        .into(header_IMG_user);
//
//            }
//        });
        // [END upload_memory]
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bottom_app_bar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.menu_chat:
                Toast.makeText(this, "chat Click", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    //TODO Util function
    // slide the view from below itself to the current position
    public void slideUp(View view){
        //view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillEnabled(false);
        view.startAnimation(animate);

    }


}