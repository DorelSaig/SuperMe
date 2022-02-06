package com.DorelSaig.superme.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.DorelSaig.superme.Activities.CreateCategoryActivity;
import com.DorelSaig.superme.Activities.CreateNewItemActivity;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainMarketFragment extends Fragment {

    private FragmentManager fragmentManager;
    private Activity currentActivity;


    private FloatingActionButton toolbar_FAB_add;
    private MaterialToolbar panel_Toolbar_Top;




    public MainMarketFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentActivity = getActivity();





    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_main_market, container, false);


        findViews(view);
        initButtons();

        fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_LAY_myItems_frame, MyItemsFragment.class, null)
                .add(R.id.fragment_LAY_categories_frame, CategoriesFragment.class, null)
                .commit();


        return view;
    }

    private void initButtons() {

        panel_Toolbar_Top.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentActivity.onBackPressed();
            }
        });

        toolbar_FAB_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyDataManager.getInstance().getCurrentUser().getUid().equals(Constants.EDITOR)){
                    addCategory();
                } else {
                    addItem();
                }

            }
        });
    }

    private void findViews(View view) {
        toolbar_FAB_add = currentActivity.findViewById(R.id.toolbar_FAB_add);
        panel_Toolbar_Top = currentActivity.findViewById(R.id.panel_Toolbar_Top);
    }

    private void addItem() {
        startActivity(new Intent(currentActivity, CreateNewItemActivity.class));
    }

    private void addCategory() {
        startActivity(new Intent(currentActivity, CreateCategoryActivity.class));
    }


}