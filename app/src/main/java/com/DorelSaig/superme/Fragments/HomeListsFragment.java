package com.DorelSaig.superme.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.DorelSaig.superme.Activities.CreateListActivity;
import com.DorelSaig.superme.Adapters.Adapter_ShoppingLists;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.ListItemClickListener;
import com.DorelSaig.superme.Objects.MyList;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class HomeListsFragment extends Fragment {

    private MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db = dataManager.getDbFireStore();
    private MyUser currentUser = dataManager.getCurrentUser();
    private ListenerRegistration listsListener;

    private RecyclerView fragment_RECYC_lists;
    private ArrayList<MyList> listsArrayList;
    private Adapter_ShoppingLists adapter_shoppingLists;

    private FloatingActionButton toolbar_FAB_add;
    private MaterialToolbar panel_Toolbar_Top;

    private Activity currentActivity;


    public HomeListsFragment() {
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home_lists, container, false);

        findViews(view);

        panel_Toolbar_Top.setTitle("הרשימות שלי"); //Todo Strings

        createRecycler();
        listsArrayChangeListener();




        return view;
    }

    private void findViews(View view) {
        fragment_RECYC_lists = view.findViewById(R.id.fragment_RECYC_lists);
        toolbar_FAB_add = currentActivity.findViewById(R.id.toolbar_FAB_add);
        panel_Toolbar_Top = currentActivity.findViewById(R.id.panel_Toolbar_Top);
        ActionMenuItemView menu_chat = currentActivity.findViewById(R.id.menu_chat);
        menu_chat.setVisibility(View.INVISIBLE);
    }

    private void createRecycler() {
        listsArrayList = new ArrayList<>();
        adapter_shoppingLists = new Adapter_ShoppingLists(this.getContext(), listsArrayList);

        fragment_RECYC_lists.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        fragment_RECYC_lists.setHasFixedSize(true);
        fragment_RECYC_lists.setItemAnimator(new DefaultItemAnimator());
        fragment_RECYC_lists.setAdapter(adapter_shoppingLists);

        adapter_shoppingLists.setShoppingListClickListener(new ListItemClickListener() {
            @Override
            public void listItemClicked(MyList list, int position) {
                dataManager.setCurrentListUid(list.getListUid());
                dataManager.setCurrentListTitle(list.getTitle());

                getParentFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.main_FRG_container, GroceriesListFragment.class, null)
                        .addToBackStack("tag")
                        .commit();
            }

            @Override
            public void listItemLongClick(MyList myList, int position) {

                new MaterialAlertDialogBuilder(currentActivity)
                        .setTitle("שימ/י לב!")
                        .setMessage("האם למחוק את הרשימה " + myList.getTitle())
                        .setNegativeButton("לא", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeList(myList);
                            }
                        })
                        .show();
            }
        });
    }

    private void removeList(MyList myList) {
        db.collection(getString(R.string.key_lists)).document(myList.getListUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                CollectionReference usersCollect = db.collection("users");
                for (String uid : myList.getSharedWithUidsList()){
                    usersCollect.document(uid).update("myListsUids", FieldValue.arrayRemove(myList.getListUid()));
                }
                usersCollect.document(myList.getCreatorUid()).update("myListsUids", FieldValue.arrayRemove(myList.getListUid()));
                currentUser.removeFromListsUids(myList.getListUid());
                Task<Void> listCover = dataManager.getStorage().getReference().child("lists_covers").child(myList.getListUid()).delete();

                Toast.makeText(currentActivity, myList.getTitle() + " נמחק בהצלחה ", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        toolbar_FAB_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(currentActivity, "Add From Lists Home",Toast.LENGTH_LONG).show();
                startActivity(new Intent(currentActivity, CreateListActivity.class));
            }
        });



    }


    // ---> RecycleView Lists Listener
    private void listsArrayChangeListener() {

        CollectionReference collectionReference= db.collection(getString(R.string.key_lists));
        listsListener =  collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                    MyList newList = dc.getDocument().toObject(MyList.class);
                                    Log.d("pttt", currentUser.getMyListsUids().toString());

                                    if (currentUser.getMyListsUids().contains(newList.getListUid())) {
                                        listsArrayList.add(newList);
                                        Log.d("pttt", "List Added To firebase");
                                    }

                                    break;
                                }
                                case MODIFIED: {
                                    MyList newList = dc.getDocument().toObject(MyList.class);
                                    for (int i = 0; i < listsArrayList.size(); i++) {
                                        if (listsArrayList.get(i).getListUid().equals(newList.getListUid())) {
                                            listsArrayList.set(i, newList);
                                            adapter_shoppingLists.notifyItemChanged(i);
                                            break;
                                        }
                                    }

                                    Log.d("pttt", String.valueOf(newList.getSharedWithUidsList().contains(currentUser.getUid())));
                                    if(newList.getSharedWithUidsList().contains(currentUser.getUid())) {
                                        if(!listsArrayList.contains(newList)) {
                                            listsArrayList.add(newList);
                                        }
                                    }else {
                                        if(listsArrayList.contains(newList)){
                                            listsArrayList.remove(newList);
                                        }
                                    }
                                    Log.d("pttt", "List Changed To firebase");
                                    break;
                                }
                                case REMOVED: {
                                    MyList newList = dc.getDocument().toObject(MyList.class);
                                    for (int i = 0; i < listsArrayList.size(); i++) {
                                        if (listsArrayList.get(i).getListUid().equals(newList.getListUid())) {
                                            listsArrayList.remove(i);
                                            adapter_shoppingLists.notifyItemRemoved(i);
                                            Log.d("pttt", "List Removed To firebase");
                                            break;
                                        }
                                    }
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                            adapter_shoppingLists.notifyDataSetChanged();
                        }
                    }


                });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listsListener.remove();
    }
}