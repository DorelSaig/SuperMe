package com.DorelSaig.superme.Fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DorelSaig.superme.Adapters.Adapter_My_Categories;
import com.DorelSaig.superme.CategoryClickListener;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Objects.MyCategory;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class CategoriesFragment extends Fragment {

    private MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db = dataManager.getDbFireStore();
    private MyUser currentUser = dataManager.getCurrentUser();

    private String currentListUID;

    private Activity currentActivity;

    private RecyclerView fragment_RECYC_categories;
    private ArrayList<MyCategory> myCategoriesArrayList;
    private Adapter_My_Categories adapter_my_categories;

    private MaterialToolbar panel_Toolbar_Top;

    public CategoriesFragment() {
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_categories, container, false);


        fragment_RECYC_categories = view.findViewById(R.id.fragment_RECYC_categories);
        panel_Toolbar_Top = currentActivity.findViewById(R.id.panel_Toolbar_Top);
        panel_Toolbar_Top.setTitle("מאגר המוצרים:");

        myCategoriesArrayList = new ArrayList<>();
        adapter_my_categories = new Adapter_My_Categories(this.getContext(), myCategoriesArrayList);

        fragment_RECYC_categories.setLayoutManager(new GridLayoutManager(currentActivity, 3));
        fragment_RECYC_categories.setHasFixedSize(true);
        fragment_RECYC_categories.setItemAnimator(new DefaultItemAnimator());
        fragment_RECYC_categories.setAdapter(adapter_my_categories);

        itemsArrayChangeListener();

        adapter_my_categories.setCategoryClickListener(new CategoryClickListener() {
            @Override
            public void categoryClicked(MyCategory category, int position) {
                dataManager.setCurrentCategoryUid(category.getCatUid());
                panel_Toolbar_Top.setTitle(category.getTitle());

                getParentFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.main_FRG_container, CategoryItemsFragment.class, null)
                        .addToBackStack("tag")
                        .commit();
            }

        });


        return view;
    }

    private void itemsArrayChangeListener() {

        db.collection("categories").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("pttt", "FireStore Error" + error.getMessage());
                    return;
                }

                assert value != null;
                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED: {
                            MyCategory newCategory = dc.getDocument().toObject(MyCategory.class);
                            //listRef.update("items_Counter", FieldValue.increment(1));

                            myCategoriesArrayList.add(newCategory);


                            Log.d("pttt", "Category Added To firebase");
//                            adapter_my_items.notifyItemInserted(0);
                            break;
                        }
                        case MODIFIED: {
                            MyCategory newCategory = dc.getDocument().toObject(MyCategory.class);
                            for (int i = 0; i < myCategoriesArrayList.size(); i++) {
                                if (myCategoriesArrayList.get(i).getCatUid().equals(newCategory.getCatUid())) {
                                    myCategoriesArrayList.set(i, newCategory);

                                    //adapter_items.notifyItemChanged(i);
                                }
                            }
                            Log.d("pttt", "Category Changed in firebase");
                            break;
                        }
                        case REMOVED: {
                            MyCategory newCategory = dc.getDocument().toObject(MyCategory.class);
                            for (int i = 0; i < myCategoriesArrayList.size(); i++) {
                                if (myCategoriesArrayList.get(i).getCatUid().equals(newCategory.getCatUid())) {
                                    myCategoriesArrayList.remove(i);

                                    //listRef.update("items_Counter", FieldValue.increment(-1));
                                    //adapter_items.notifyItemRemoved(i);
                                }
                            }
                            Log.d("pttt", "Category Removed From firebase");
                            break;
                        }
                        default:
                            break;
                    }


                }
                adapter_my_categories.notifyDataSetChanged();
            }
        });
    }

}