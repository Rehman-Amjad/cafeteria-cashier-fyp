package com.technogenis.cafeteriacashier.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.MyPreferenceManager;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.CustomerAdapter;
import com.technogenis.cafeteriacashier.adapter.HistoryAdapter;
import com.technogenis.cafeteriacashier.model.CustomerModel;
import com.technogenis.cafeteriacashier.model.HistoryModel;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private MyPreferenceManager preferenceManager;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CustomerAdapter mHistoryAdapter;
    private final List<CustomerModel> mDataList = new ArrayList<>();
    private DatabaseReference historyRef;
    private HistoryModel lastHistoryItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        historyRef = FirebaseDatabase.getInstance().getReference("customers");

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Ensure that the status bar is visible
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        initUI();
        setupFirebaseListener();


    }

    private void initUI() {
        recyclerView =findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHistoryAdapter = new CustomerAdapter(this, mDataList);
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
                    CustomerModel history = snapshot.getValue(CustomerModel.class);
                    if (history != null) {
                        // Only add the item if it matches the RFID
                        mDataList.add(history);
                    }
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