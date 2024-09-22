package com.technogenis.cafeteriacashier;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.adapter.HistoryAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    TextView tvItemName,tvQty,tvPrice,tvType,tvBalance;

    private  MyPreferenceManager preferenceManager;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private HistoryAdapter mHistoryAdapter;
    private List<HistoryModel> mDataList = new ArrayList<>();
    private DatabaseReference historyRef;
    private HistoryModel lastHistoryItem;

    String rfid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        preferenceManager = MyPreferenceManager.getInstance(this);
        rfid = preferenceManager.getString("rfid");
        historyRef = FirebaseDatabase.getInstance().getReference("buyitems");

        initUI();
        setupFirebaseListener();

    }

    private void initUI() {
        recyclerView =findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        tvItemName=findViewById(R.id.tvItemName);
        tvQty=findViewById(R.id.tvQty);
        tvPrice=findViewById(R.id.tvPrice);
        tvType=findViewById(R.id.tvType);
        tvBalance=findViewById(R.id.tvBalance);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHistoryAdapter = new HistoryAdapter(this, mDataList);
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
                HistoryModel lastItem = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HistoryModel history = snapshot.getValue(HistoryModel.class);
                    if (history != null) {
                        // Only add the item if it matches the RFID
                        if (history.getCustomerrfid().equals(rfid)) {
                            mDataList.add(history);
                            lastItem = history;
                        }
                    }
                }

                // If we have a last item, store it in the global variable
                if (lastItem != null) {
                    lastHistoryItem = lastItem;
                    tvItemName.setText(lastHistoryItem.getItemname());
                    tvQty.setText("QTY: " + lastHistoryItem.getItemqty());
                    tvPrice.setText("Price: RS " + lastHistoryItem.getItemprice());
                    tvType.setText("Type: " + lastHistoryItem.getCustomerpayment());
                    tvBalance.setText("Balance: RS " + lastHistoryItem.getCustomerblance());

                    Log.d("LastItem", "Last item: " + lastHistoryItem.toString());
                }

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