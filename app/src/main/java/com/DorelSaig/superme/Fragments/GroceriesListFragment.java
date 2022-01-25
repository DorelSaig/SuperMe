package com.DorelSaig.superme.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.DorelSaig.superme.Activities.CreateNewItemActivity;
import com.DorelSaig.superme.Activities.MainActivity;
import com.DorelSaig.superme.Adapter_Items;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.ItemsClickListener;
import com.DorelSaig.superme.ListListener;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.Objects.MyList;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class GroceriesListFragment extends Fragment {

    private MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db = dataManager.getDbFireStore();
    private MyUser currentUser = dataManager.getCurrentUser();

    private Activity currentActivity;

    private DocumentReference listRef;

    private FrameLayout fragment_LAY_frame;
    private RecyclerView fragment_RECYC_items;
    private ArrayList<MyItem> itemsArrayList;
    private Adapter_Items adapter_items;

    private RelativeLayout fragment_LAY_empty;

    private FloatingActionButton toolbar_FAB_add;
    private MaterialToolbar panel_Toolbar_Top;

    private String currentListUID;


    public GroceriesListFragment() {
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_groceries_list, container, false);

        // Get The Current Loading List From dataManager.
        currentListUID = dataManager.getCurrentListUid();
        listRef = db.collection("lists").document(currentListUID);

        findViews(view);
        initButtons();
        // FindViews


        //Recycler Initiate
        itemsArrayList = new ArrayList<>();

        adapter_items = new Adapter_Items(this.getContext(), itemsArrayList);

        fragment_RECYC_items.setLayoutManager(new LinearLayoutManager(this.getContext()));
        fragment_RECYC_items.setHasFixedSize(true);
        fragment_RECYC_items.setItemAnimator(new DefaultItemAnimator());
        fragment_RECYC_items.setAdapter(adapter_items);

        itemsArrayChangeListener();

        adapter_items.setItemClickListener(new ItemsClickListener() {
            @Override
            public void itemClicked(MyItem item, int position) {
                Toast.makeText(getContext(), item.getItemTitle() + "position: " + position, Toast.LENGTH_SHORT).show();

            }
        });


        //On Item Swipe Callback
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                MyItem tempItem = adapter_items.getItem(position);

                itemsArrayList.remove(position);
                if(itemsArrayList.isEmpty()){
                    fragment_LAY_empty.setVisibility(View.VISIBLE);
                }

                Snackbar snackbar = Snackbar
                        .make(fragment_LAY_frame, "מוצר הוסר מהרשימה", Snackbar.LENGTH_LONG)
                        .setAction("בטל", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(itemsArrayList.isEmpty()){
                                    fragment_LAY_empty.setVisibility(View.INVISIBLE);
                                }
                                itemsArrayList.add(position,tempItem);
                                //fragment_RECYC_items.scrollToPosition(position);
                                adapter_items.notifyItemInserted(position);
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                if (event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT) {
                                    listRef.collection("items").document(tempItem.getItemUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("pttt", "Item Removed From firebase");
                                        }
                                    });
                                }
                            }
                        });

                snackbar.setAnchorView(R.id.toolbar_FAB_add);
                snackbar.setBackgroundTint(Color.parseColor("#F9E1DC"));
                snackbar.setActionTextColor(Color.RED);
                snackbar.show();


                adapter_items.notifyDataSetChanged();

            }
        }).attachToRecyclerView(fragment_RECYC_items);




        return view;
    }

    private void initButtons() {


        toolbar_FAB_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(currentActivity, "from Fragment Add", Toast.LENGTH_LONG).show();
                addItem();
            }
        });

        panel_Toolbar_Top.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(currentActivity, "Back",Toast.LENGTH_LONG).show();
                currentActivity.onBackPressed();
            }
        });

    }

    private void findViews(View view) {
        fragment_LAY_frame = view.findViewById(R.id.fragment_LAY_frame);
        fragment_RECYC_items = view.findViewById(R.id.fragment_RECYC_items);

        fragment_LAY_empty = view.findViewById(R.id.fragment_LAY_empty);

        toolbar_FAB_add = currentActivity.findViewById(R.id.toolbar_FAB_add);
        panel_Toolbar_Top = currentActivity.findViewById(R.id.panel_Toolbar_Top);

    }

    private void addItem() {
        startActivity(new Intent(currentActivity, CreateNewItemActivity.class));

    }

    private void itemsArrayChangeListener() {

        listRef.collection("items").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("pttt", "FireStore Error" + error.getMessage());
                    return;
                }

                int counter = value.size();
                listRef.update("items_Counter", counter);


                assert value != null;
                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED: {
                            MyItem newItem = dc.getDocument().toObject(MyItem.class);
                            //listRef.update("items_Counter", FieldValue.increment(1));
                            if(itemsArrayList.isEmpty())
                                fragment_LAY_empty.setVisibility(View.INVISIBLE);
                            itemsArrayList.add(newItem);


                            Log.d("pttt", "Item Added To firebase");
                            //adapter_items.notifyItemInserted(0);
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
                            Log.d("pttt", "Item Changed in firebase");
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
                            Log.d("pttt", "Item Removed From firebase");
                            break;
                        }
                        default:
                            break;
                    }
                    adapter_items.notifyDataSetChanged();

                }

            }
        });
    }


}