package com.technogenis.cafeteriacashier.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.MyPreferenceManager;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.model.HistoryModel;


public class HomeFragment extends Fragment {


    TextView tvItemName,tvQty,tvPrice,tvType,tvBalance;
    private DatabaseReference historyRef;

    private MyPreferenceManager preferenceManager;
    private HistoryModel lastHistoryItem;

    String rfid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        historyRef = FirebaseDatabase.getInstance().getReference("buyitems");
        preferenceManager = MyPreferenceManager.getInstance(getActivity());
        rfid = preferenceManager.getString("rfid");

        initView(view);

       setupFirebaseListener();



        return view;
    }

    private void initView(View view) {
        tvItemName=view.findViewById(R.id.tvItemName);
        tvQty=view.findViewById(R.id.tvQty);
        tvPrice=view.findViewById(R.id.tvPrice);
        tvType=view.findViewById(R.id.tvType);
        tvBalance=view.findViewById(R.id.tvBalance);
    }

    private void setupFirebaseListener() {
        // Attach ValueEventListener to the reference
        historyRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing list before adding new items
                HistoryModel lastItem = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HistoryModel history = snapshot.getValue(HistoryModel.class);
                    if (history != null) {
                        // Only add the item if it matches the RFID
                        if (history.getCustomerRfid().equals(rfid)) {
                            lastItem = history;
                        }
                    }
                }

//                 If we have a last item, store it in the global variable
                if (lastItem != null) {
                    lastHistoryItem = lastItem;
                    tvItemName.setText(lastHistoryItem.getItemName());
                    tvQty.setText("QTY: " + lastHistoryItem.getItemQty());
                    tvPrice.setText("Price: RS " + lastHistoryItem.getItemPrice());
                    tvType.setText("Type: " + lastHistoryItem.getCustomerPayment());
                    tvBalance.setText("Balance: RS " + lastHistoryItem.getCustomerBalance());

                    Log.d("LastItem", "Last item: " + lastHistoryItem.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors if necessary
                Log.e("FirebaseListener", "Database error: " + databaseError.getMessage());
            }
        });
    }
}