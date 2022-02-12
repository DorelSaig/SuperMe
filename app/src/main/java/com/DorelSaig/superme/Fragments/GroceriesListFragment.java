package com.DorelSaig.superme.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.DorelSaig.superme.Activities.EditItemActivity;
import com.DorelSaig.superme.Activities.ShareListActivity;
import com.DorelSaig.superme.Adapters.Adapter_Items;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.ItemsClickListener;
import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.Misc.Utils;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.DorelSaig.superme.SwipeToDeleteCallback;
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

    private final MyDataManager dataManager = MyDataManager.getInstance();
    private final FirebaseFirestore db = dataManager.getDbFireStore();

    private Activity currentActivity;

    private DocumentReference listRef;

    private MenuItem add_person;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_groceries_list, container, false);

        // Get The Current Loading List From dataManager.
        currentListUID = dataManager.getCurrentListUid();

        listRef = db.collection(Constants.KEY_LISTS).document(currentListUID);

        findViews(view);
        initButtons();

        panel_Toolbar_Top.setTitle(dataManager.getCurrentListTitle());
        add_person.setVisible(true);

        createRecycler();

        itemsArrayChangeListener();

        enableSwipeToDeleteAndUndo();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        add_person.setVisible(false);
    }

    private void findViews(View view) {
        fragment_LAY_frame = view.findViewById(R.id.fragment_LAY_frame);
        fragment_RECYC_items = view.findViewById(R.id.fragment_RECYC_items);

        fragment_LAY_empty = view.findViewById(R.id.fragment_LAY_empty);

        toolbar_FAB_add = currentActivity.findViewById(R.id.toolbar_FAB_add);
        panel_Toolbar_Top = currentActivity.findViewById(R.id.panel_Toolbar_Top);
        add_person = panel_Toolbar_Top.getMenu().findItem(R.id.add_person);

        ActionMenuItemView menu_chat = currentActivity.findViewById(R.id.menu_chat);
        menu_chat.setVisibility(View.VISIBLE);
    }

    private void initButtons() {
        toolbar_FAB_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        panel_Toolbar_Top.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentActivity.onBackPressed();
            }
        });

        panel_Toolbar_Top.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(currentActivity, ShareListActivity.class));
                return false;
            }
        });
    }

    private void createRecycler() {
        itemsArrayList = new ArrayList<>();
        adapter_items = new Adapter_Items(this.getContext(), itemsArrayList);

        fragment_RECYC_items.setLayoutManager(new LinearLayoutManager(this.getContext()));
        fragment_RECYC_items.setHasFixedSize(true);
        fragment_RECYC_items.setItemAnimator(new DefaultItemAnimator());
        fragment_RECYC_items.setAdapter(adapter_items);

        adapter_items.setItemClickListener(new ItemsClickListener() {
            @Override
            public void itemClicked(MyItem item, int position) {
                dataManager.setCurrentItem(item);
                startActivity(new Intent(currentActivity, EditItemActivity.class));
            }
        });

    }

    /**
     * Initiate the Swipe To Delete functionality
     */
    private void enableSwipeToDeleteAndUndo() {

        //On Item Swipe Callback
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(currentActivity) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                MyItem tempItem = adapter_items.getItem(position);

                itemsArrayList.remove(position);
                if (itemsArrayList.isEmpty()) {
                    fragment_LAY_empty.setVisibility(View.VISIBLE);
                }

                Snackbar snackbar = Utils.showErrorSnackBar(fragment_LAY_frame, tempItem.getItemTitle() + " " + getString(R.string.removed), Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.toolbar_FAB_add)
                        .setAction(getString(R.string.cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (itemsArrayList.isEmpty()) {
                                    fragment_LAY_empty.setVisibility(View.INVISIBLE);
                                }
                                itemsArrayList.add(position, tempItem);
                                //fragment_RECYC_items.scrollToPosition(position);
                                adapter_items.notifyItemInserted(position);
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                if (event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT) {
                                    listRef.collection(Constants.KEY_ITEMS).document(tempItem.getItemUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("pttt", "Item Removed From firebase");
                                        }
                                    });
                                }
                            }
                        });
                snackbar
                        .show();

                adapter_items.notifyDataSetChanged();

            }

        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(fragment_RECYC_items);
    }

    private void addItem() {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                )
                .replace(R.id.main_FRG_container, MainMarketFragment.class, null)
                .addToBackStack("tag")
                .commit();
    }

    /**
     * Listen and triggers operation whenever a list ref. of items - Gets new item added, modified or removed in firebase
     */
    private void itemsArrayChangeListener() {

        listRef.collection(Constants.KEY_ITEMS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("pttt", "FireStore Error" + error.getMessage());
                    return;
                }

                assert value != null;
                int counter = value.size();
                listRef.update(Constants.FIELD_LIST_COUNTER, counter);

                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {

                        case ADDED: {
                            MyItem newItem = dc.getDocument().toObject(MyItem.class);
                            if (itemsArrayList.isEmpty())
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