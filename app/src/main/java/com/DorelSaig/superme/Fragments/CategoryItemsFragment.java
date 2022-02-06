package com.DorelSaig.superme.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import com.DorelSaig.superme.Activities.CreateCatItem;
import com.DorelSaig.superme.Activities.EditItemActivity;
import com.DorelSaig.superme.Adapters.Adapter_My_Items;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.ItemsClickListener;
import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class CategoryItemsFragment extends Fragment {

    private MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db = dataManager.getDbFireStore();
    private MyUser currentUser = dataManager.getCurrentUser();

    private String currentListUID;
    private String currentCategoryUID;

    private Activity currentActivity;

    private RecyclerView fragment_RECYC_items;
    private ArrayList<MyItem> itemsArrayList;
    private Adapter_My_Items adapter_items;

    private FloatingActionButton toolbar_FAB_add;


    public CategoryItemsFragment() {
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_category_items, container, false);

        currentListUID = dataManager.getCurrentListUid();
        currentCategoryUID = dataManager.getCurrentCategoryUid();

        fragment_RECYC_items = view.findViewById(R.id.fragment_RECYC_items);
        toolbar_FAB_add = currentActivity.findViewById(R.id.toolbar_FAB_add);

        toolbar_FAB_add.setVisibility(View.INVISIBLE);

        toolbar_FAB_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(currentActivity, "from Fragment Add", Toast.LENGTH_LONG).show();
                addItem();
            }
        });



        if(currentUser.getUid().equals(Constants.EDITOR)){
            toolbar_FAB_add.setVisibility(View.VISIBLE);
        }


        itemsArrayList = new ArrayList<>();
        adapter_items = new Adapter_My_Items(this.getContext(), itemsArrayList);

        fragment_RECYC_items.setLayoutManager(new GridLayoutManager(currentActivity, 3));
        fragment_RECYC_items.setHasFixedSize(true);
        fragment_RECYC_items.setItemAnimator(new DefaultItemAnimator());
        fragment_RECYC_items.setAdapter(adapter_items);

        itemsArrayChangeListener();

        adapter_items.setItemClickListener(new ItemsClickListener() {
            @Override
            public void itemClicked(MyItem item, int position) {
                DocumentReference docRef = db.collection(Constants.KEY_LISTS).document(currentListUID).collection(Constants.KEY_ITEMS).document(item.getItemUid());
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d("pttt", "item already in the list");
                            Snackbar snackbar = Snackbar //TODO Make general method
                                    .make(fragment_RECYC_items, "מוצר זה קיים ברשימה", Snackbar.LENGTH_LONG);
                            snackbar.setAnchorView(R.id.toolbar_FAB_add);
                            snackbar.setBackgroundTint(Color.parseColor("#F9E1DC"));
                            snackbar.setActionTextColor(Color.RED);
                            snackbar.show();
                        } else{
                            Toast.makeText(getContext(), item.getItemTitle() + "position: " + position, Toast.LENGTH_SHORT).show();
                            dataManager.setCurrentItem(item);
                            startActivity(new Intent(currentActivity, EditItemActivity.class));
                        }
                    }
                });
            }
        });


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        toolbar_FAB_add.setVisibility(View.VISIBLE);
    }

    private void addItem() {
        Log.d("pttt", "Add Item To Category");
        startActivity(new Intent(currentActivity, CreateCatItem.class));
    }

    private void itemsArrayChangeListener() {

        db.collection(Constants.KEY_CATEGORIES).document(currentCategoryUID)
                .collection(Constants.KEY_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            MyItem newItem = dc.getDocument().toObject(MyItem.class);
                            //listRef.update("items_Counter", FieldValue.increment(1));

                            itemsArrayList.add(newItem);


                            Log.d("pttt", "Your Item Added To firebase");
//                            adapter_my_items.notifyItemInserted(0);
                            break;
                        }
                        case MODIFIED: {
                            MyItem newItem = dc.getDocument().toObject(MyItem.class);
                            for (int i = 0; i < itemsArrayList.size(); i++) {
                                if (itemsArrayList.get(i).getItemUid().equals(newItem.getItemUid())) {
                                    itemsArrayList.set(i, newItem);
                                    //adapter_items.notifyItemChanged(i);
                                }
                            }
                            Log.d("pttt", "Your Item Changed in firebase");
                            break;
                        }
                        case REMOVED: {
                            MyItem newItem = dc.getDocument().toObject(MyItem.class);
                            for (int i = 0; i < itemsArrayList.size(); i++) {
                                if (itemsArrayList.get(i).getItemUid().equals(newItem.getItemUid())) {
                                    itemsArrayList.remove(i);

                                    //listRef.update("items_Counter", FieldValue.increment(-1));
                                    //adapter_items.notifyItemRemoved(i);
                                }
                            }
                            Log.d("pttt", "Your Item Removed From firebase");
                            break;
                        }
                        default:
                            break;
                    }


                }
                adapter_items.notifyDataSetChanged();
            }
        });
    }


}