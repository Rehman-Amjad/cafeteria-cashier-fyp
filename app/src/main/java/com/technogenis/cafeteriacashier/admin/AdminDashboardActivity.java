package com.technogenis.cafeteriacashier.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.CustomerAdapter;
import com.technogenis.cafeteriacashier.model.CustomerModel;
import com.technogenis.cafeteriacashier.util.EdgeToEdgeHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View root;
    private CustomerAdapter mAdapter;
    private final List<CustomerModel> mDataList = new ArrayList<>();

    private DatabaseReference customersRef;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        customersRef = FirebaseDatabase.getInstance().getReference("customers");

        initUI();
    }

    private void initUI() {
        root = findViewById(R.id.main);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        EdgeToEdgeHelper.applySystemBarsPadding(root, true, true);
        recyclerView.setClipToPadding(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CustomerAdapter(this, mDataList);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CustomerModel c = snapshot.getValue(CustomerModel.class);
                    if (c != null) mDataList.add(c);
                }
                mAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(mDataList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminDashboard", "Customers query cancelled", error.toException());
                progressBar.setVisibility(View.GONE);
                Snackbar.make(root, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        };
        customersRef.addValueEventListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null) {
            customersRef.removeEventListener(listener);
        }
        listener = null;
        super.onStop();
    }
}
