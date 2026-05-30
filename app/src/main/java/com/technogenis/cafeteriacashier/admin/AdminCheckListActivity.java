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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.HistoryAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.AdminStats;
import com.technogenis.cafeteriacashier.util.EdgeToEdgeHelper;
import com.technogenis.cafeteriacashier.util.HomeFormat;
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

    private TextView tvCustName, tvCustBalance, tvCustSpent, tvCustOrders, tvCustSplit;

    private Query query;
    private ValueEventListener listener;
    private DatabaseReference customerRef;
    private ValueEventListener customerListener;

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

        Toolbar toolbar = findViewById(R.id.Toolbar);
        EdgeToEdgeHelper.applySystemBarsPadding(toolbar, true, false, false, true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCustName = findViewById(R.id.tvCustName);
        tvCustBalance = findViewById(R.id.tvCustBalance);
        tvCustSpent = findViewById(R.id.tvCustSpent);
        tvCustOrders = findViewById(R.id.tvCustOrders);
        tvCustSplit = findViewById(R.id.tvCustSplit);

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
                List<HistoryModel> all = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HistoryModel h = snapshot.getValue(HistoryModel.class);
                    if (h == null) continue;
                    total += SafeParse.parseIntOr(h.getItemPrice(), 0);
                    mDataList.add(h);
                    all.add(h);
                }
                String pkr = getString(R.string.label_currency_pkr) + " ";
                tvTotal.setText(pkr + total);

                int[] split = AdminStats.cardCashSplit(all);
                java.util.Map<String, Integer> bal = AdminStats.latestBalanceByRfid(all);
                Integer balance = bal.get(rfid);
                tvCustBalance.setText(getString(R.string.label_balance) + ": " + pkr
                        + HomeFormat.groupedAmount(balance != null ? balance : 0));
                tvCustSpent.setText(getString(R.string.stat_total_spent) + ": " + pkr
                        + HomeFormat.groupedAmount(total));
                tvCustOrders.setText(getString(R.string.stat_orders) + ": " + all.size());
                tvCustSplit.setText(getString(R.string.stat_card_cash) + ": "
                        + split[0] + "/" + split[1]);

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

        customerRef = FirebaseDatabase.getInstance().getReference("customers").child(rfid);
        customerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("customername").getValue(String.class);
                tvCustName.setText(name != null && !name.isEmpty() ? name : rfid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvCustName.setText(rfid);
            }
        };
        customerRef.addValueEventListener(customerListener);
    }

    @Override
    protected void onStop() {
        if (query != null && listener != null) {
            query.removeEventListener(listener);
        }
        if (customerRef != null && customerListener != null) {
            customerRef.removeEventListener(customerListener);
        }
        listener = null;
        customerListener = null;
        super.onStop();
    }
}
