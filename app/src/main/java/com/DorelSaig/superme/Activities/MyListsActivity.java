package com.DorelSaig.superme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.DorelSaig.superme.Adapter_ShoppingLists;
import com.DorelSaig.superme.DataManager;
import com.DorelSaig.superme.ListItemClickListener;
import com.DorelSaig.superme.Objects.List;
import com.DorelSaig.superme.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MyListsActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylists);

        setSupportActionBar(panel_AppBar_bottom);
        setSupportActionBar(panel_Toolbar_Top);



        findViews();
        initButtons();

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


        ArrayList<List> lists = DataManager.generateLists();

        Adapter_ShoppingLists adapter_shoppingLists = new Adapter_ShoppingLists(this, lists);

        main_LST_lists.setLayoutManager(new GridLayoutManager(this, 2));

        main_LST_lists.setHasFixedSize(true);
        main_LST_lists.setItemAnimator(new DefaultItemAnimator());
        main_LST_lists.setAdapter(adapter_shoppingLists);

        adapter_shoppingLists.setShoppingListClickListener(new ListItemClickListener() {
            @Override
            public void listItemClicked(List list, int position) {
                Toast.makeText(MyListsActivity.this, list.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void findViews() {

        panel_Toolbar_Top = findViewById(R.id.panel_Toolbar_Top);
        panel_AppBar_bottom = findViewById(R.id.panel_AppBar_bottom);
        drawer_layout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        toolbar_FAB_add = findViewById(R.id.toolbar_FAB_add);
        panel_VIEW_message = findViewById(R.id.panel_VIEW_message);
        panel_BTN_chat_back = findViewById(R.id.panel_BTN_chat_back);

        main_LST_lists = findViewById(R.id.main_LST_lists);

        header = nav_view.getHeaderView(0);
        navigation_header_container_FAB_profile_pic = (FloatingActionButton) header.findViewById(R.id.navigation_header_container_FAB_profile_pic);

    }

    private void initButtons() {

        panel_Toolbar_Top.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MyListsActivity.this, "Back",Toast.LENGTH_LONG).show();

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
                Toast.makeText(MyListsActivity.this, "Add",Toast.LENGTH_LONG).show();
            }
        });

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.item1:
                        Toast.makeText(MyListsActivity.this, "Profile",Toast.LENGTH_LONG).show();
                        break;
                    case R.id.item2:
                        AuthUI.getInstance()
                                .signOut(MyListsActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @SuppressLint("RestrictedApi")
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // user is now signed out
                                        startActivity(new Intent(MyListsActivity.this, LoginActivity.class));
                                        Toast.makeText(MyListsActivity.this,"Signed Out",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MyListsActivity.this,"profile_picture",Toast.LENGTH_SHORT).show();
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
        view.setVisibility(View.VISIBLE);
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
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }


}