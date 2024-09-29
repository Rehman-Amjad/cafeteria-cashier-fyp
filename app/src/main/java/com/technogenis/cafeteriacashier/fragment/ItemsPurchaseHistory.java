package com.technogenis.cafeteriacashier.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.MyPreferenceManager;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.HistoryAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;

import java.util.ArrayList;
import java.util.List;


public class ItemsPurchaseHistory extends Fragment {

    private MyPreferenceManager preferenceManager;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private HistoryAdapter mHistoryAdapter;
    private final List<HistoryModel> mDataList = new ArrayList<>();
    private DatabaseReference historyRef;
    private HistoryModel lastHistoryItem;

    TextView tvTotal;

    int totalPrice = 0;

    String rfid;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase database reference
        historyRef = FirebaseDatabase.getInstance().getReference("buyitems");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_items_purchase_history, container, false);

        preferenceManager = MyPreferenceManager.getInstance(getActivity());
        rfid = preferenceManager.getString("rfid");

        initUI(view);
        setupFirebaseListener();

        return view;
    }

    private void initUI(View view) {
        recyclerView =view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvTotal = view.findViewById(R.id.tvTotal);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mHistoryAdapter = new HistoryAdapter(getActivity(), mDataList);
        recyclerView.setAdapter(mHistoryAdapter);
    }
    private void setupFirebaseListener() {
        // Attach ValueEventListener to the reference
        historyRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing list before adding new items
                mDataList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HistoryModel history = snapshot.getValue(HistoryModel.class);
                    if (history != null) {
                        // Only add the item if it matches the RFID
                        if (history.getCustomerRfid().equals(rfid)) {
                            totalPrice += Integer.parseInt(history.getItemPrice());
                            mDataList.add(history);
                        }
                    }
                }

                tvTotal.setText("PKR "+ totalPrice);
                // Notify the adapter that data has changed
                mHistoryAdapter.notifyDataSetChanged();

                // Hide progress bar and show RecyclerView once data is loaded
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors if necessary
                Log.e("FirebaseListener", "Database error: " + databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}