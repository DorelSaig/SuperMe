package com.DorelSaig.superme.Fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.DorelSaig.superme.Adapters.Adapter_My_Items;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.ItemsClickListener;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class MyItemsFragment extends Fragment {

    private MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db = dataManager.getDbFireStore();
    private MyUser currentUser = dataManager.getCurrentUser();

    private DocumentReference myItemsRef;
    private String currentListUID;

    private Activity currentActivity;

    private RecyclerView fragment_RECYC_myItems;
    private ArrayList<MyItem> myItemsArrayList;
    private Adapter_My_Items adapter_my_items;

    private RelativeLayout fragment_LAY_myItems_empty;


    public MyItemsFragment() {
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_my_items, container, false);

        currentListUID = dataManager.getCurrentListUid();


        fragment_RECYC_myItems = view.findViewById(R.id.fragment_RECYC_myItems);
        fragment_LAY_myItems_empty = view.findViewById(R.id.fragment_LAY_myItems_empty);

        myItemsArrayList = new ArrayList<>();
        adapter_my_items = new Adapter_My_Items(this.getContext(), myItemsArrayList);


        fragment_RECYC_myItems.setLayoutManager(new GridLayoutManager(currentActivity, 3));
        fragment_RECYC_myItems.setHasFixedSize(true);
        fragment_RECYC_myItems.setItemAnimator(new DefaultItemAnimator());
        fragment_RECYC_myItems.setAdapter(adapter_my_items);

        itemsArrayChangeListener();

        adapter_my_items.setItemClickListener(new ItemsClickListener() {
            @Override
            public void itemClicked(MyItem item, int position) {
                Toast.makeText(getContext(), item.getItemTitle() + "position: " + position, Toast.LENGTH_SHORT).show();
                DocumentReference docRef = db.collection(getString(R.string.key_lists)).document(currentListUID).collection(getString(R.string.key_items)).document(item.getItemUid());
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d("pttt", "item already in the list");
                            Snackbar snackbar = Snackbar //TODO Make general method
                                    .make(fragment_RECYC_myItems, "מוצר זה קיים ברשימה", Snackbar.LENGTH_LONG);
                            snackbar.setAnchorView(R.id.toolbar_FAB_add);
                            snackbar.setBackgroundTint(Color.parseColor("#F9E1DC"));
                            snackbar.setActionTextColor(Color.RED);
                            snackbar.show();
                        } else {
                            docRef.set(item)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("pttt", "DocumentSnapshot Successfully written!");
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
                });
            }
        });


        return view;
    }


    private void itemsArrayChangeListener() {

        db.collection(getString(R.string.key_users)).document(currentUser.getUid())
                .collection("myItems").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            if (myItemsArrayList.isEmpty())
                                fragment_LAY_myItems_empty.setVisibility(View.INVISIBLE);
                            myItemsArrayList.add(newItem);


                            Log.d("pttt", "Your Item Added To firebase");
//                            adapter_my_items.notifyItemInserted(0);
                            break;
                        }
                        case MODIFIED: {
                            MyItem newItem = dc.getDocument().toObject(MyItem.class);
                            for (int i = 0; i < myItemsArrayList.size(); i++) {
                                if (myItemsArrayList.get(i).getItemUid().equals(newItem.getItemUid())) {
                                    myItemsArrayList.set(i, newItem);
                                    //adapter_items.notifyItemChanged(i);
                                }
                            }
                            Log.d("pttt", "Your Item Changed in firebase");
                            break;
                        }
                        case REMOVED: {
                            MyItem newItem = dc.getDocument().toObject(MyItem.class);
                            for (int i = 0; i < myItemsArrayList.size(); i++) {
                                if (myItemsArrayList.get(i).getItemUid().equals(newItem.getItemUid())) {
                                    myItemsArrayList.remove(i);
                                    if (myItemsArrayList.isEmpty())
                                        fragment_LAY_myItems_empty.setVisibility(View.VISIBLE);
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
                adapter_my_items.notifyDataSetChanged();
            }
        });
    }


}