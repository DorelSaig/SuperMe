package com.DorelSaig.superme.Fragments;

import android.app.ProgressDialog;
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

import com.DorelSaig.superme.Adapter_ShoppingLists;
import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.ListItemClickListener;
import com.DorelSaig.superme.Objects.MyList;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class HomeListsFragment extends Fragment {

    private MyDataManager dataManager = MyDataManager.getInstance();
    private FirebaseFirestore db = dataManager.getDbFireStore();
    private MyUser currentUser = dataManager.getCurrentUser();


    private RecyclerView fragment_RECYC_lists;
    private ArrayList<MyList> listsArrayList;
    private Adapter_ShoppingLists adapter_shoppingLists;

    private ProgressDialog progressDialog;


    public HomeListsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home_lists, container, false);

        fragment_RECYC_lists = view.findViewById(R.id.fragment_RECYC_lists);

//        progressDialog = new ProgressDialog(this.getContext());
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Fetching Data...");
//
//        progressDialog.show();


        //listsArrayList = DataManager.generateLists();

        listsArrayList = new ArrayList<>();

        adapter_shoppingLists = new Adapter_ShoppingLists(this.getContext(), listsArrayList);

        fragment_RECYC_lists.setLayoutManager(new GridLayoutManager(this.getContext(), 2));

        fragment_RECYC_lists.setHasFixedSize(true);
        fragment_RECYC_lists.setItemAnimator(new DefaultItemAnimator());
        fragment_RECYC_lists.setAdapter(adapter_shoppingLists);

        listsArrayChangeListener();

        adapter_shoppingLists.setShoppingListClickListener(new ListItemClickListener() {
            @Override
            public void listItemClicked(MyList list, int position) {
                Toast.makeText(getContext(), list.getTitle() + "position: " + position, Toast.LENGTH_SHORT).show();
                dataManager.setCurrentListUid(list.getListUid());

                getParentFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.main_FRG_container, GroceriesListFragment.class, null)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    // ---> RecycleView Lists Listener
    private void listsArrayChangeListener() {
        db.collection("lists")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (currentUser.getMyListsUids().isEmpty()) {
//                            progressDialog.dismiss();
                            return;
                        }
                        if (error != null) {
//                            if (progressDialog.isShowing())
//                                progressDialog.dismiss();
                            Log.e("FireStore Error", error.getMessage());
                            return;
                        }

                        assert value != null;
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED: {
                                    MyList newList = dc.getDocument().toObject(MyList.class);
                                    if (currentUser.getMyListsUids().contains(newList.getListUid())) {
                                        listsArrayList.add(newList);
                                    }
                                    Log.d("pttt", "List Added To firebase");
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
                                    Log.d("pttt", "List Changed To firebase");
                                    break;
                                }
                                case REMOVED: {
                                    MyList newList = dc.getDocument().toObject(MyList.class);
                                    for (int i = 0; i < listsArrayList.size(); i++) {
                                        if (listsArrayList.get(i).getListUid().equals(newList.getListUid())) {
                                            listsArrayList.remove(i);
                                            adapter_shoppingLists.notifyItemRemoved(i);
                                            break;
                                        }
                                    }
                                    Log.d("pttt", "List Removed To firebase");
                                    break;
                                }
                                default: {

                                    break;
                                }
                            }
                            adapter_shoppingLists.notifyDataSetChanged();
//                            if (progressDialog.isShowing())
//                                progressDialog.dismiss();
                        }
                    }


                });
    }
}