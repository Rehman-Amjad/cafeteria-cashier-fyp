package com.technogenis.cafeteriacashier.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.HistoryAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.EdgeToEdgeHelper;
import com.technogenis.cafeteriacashier.util.SafeParse;

import java.util.ArrayList;
import java.util.List;
// Requires RTDB rules: { "buyitems": { ".indexOn": ["customerrfid"] } } — see RTDB_RULES.md

public class AdminCheckListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvTotal;
    private TextView tvEmpty;
    private View root;
    private HistoryAdapter mHistoryAdapter;
    private final List<HistoryModel> mDataList = new ArrayList<>();

    private Query query;
    private ValueEventListener listener;

    private String rfid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_check_list);

        Intent intent = getIntent();
        rfid = intent != null ? intent.getStringExtra("rfid") : null;
        if (rfid == null || rfid.isEmpty()) {
            Toast.makeText(this, R.string.error_missing_customer, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();

        query = FirebaseDatabase.getInstance()
                .getReference("buyitems")
                .orderByChild("customerrfid")
                .equalTo(rfid);
    }

    private void initUI() {
        root = findViewById(R.id.main);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvTotal = findViewById(R.id.tvTotal);
        tvEmpty = findViewById(R.id.tvEmpty);

        EdgeToEdgeHelper.applySystemBarsPadding(root, true, true);
        recyclerView.setClipToPadding(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHistoryAdapter = new HistoryAdapter(this, mDataList);
        recyclerView.setAdapter(mHistoryAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (query == null) return;
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int total = 0;
                mDataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HistoryModel h = snapshot.getValue(HistoryModel.class);
                    if (h == null) continue;
                    total += SafeParse.parseIntOr(h.getItemPrice(), 0);
                    mDataList.add(h);
                }
                tvTotal.setText(getString(R.string.label_currency_pkr) + " " + total);
                mHistoryAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(mDataList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminCheckList", "Query cancelled", error.toException());
                progressBar.setVisibility(View.GONE);
                Snackbar.make(root, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        };
        query.addValueEventListener(listener);
    }

    @Override
    protected void onStop() {
        if (query != null && listener != null) {
            query.removeEventListener(listener);
        }
        listener = null;
        super.onStop();
    }
}
