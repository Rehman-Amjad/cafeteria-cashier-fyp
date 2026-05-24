package com.technogenis.cafeteriacashier.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.MyPreferenceManager;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.HistoryAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.SafeParse;

import java.util.ArrayList;
import java.util.List;

public class ItemsPurchaseHistory extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvTotal, tvEmpty;
    private View root;
    private HistoryAdapter mAdapter;
    private final List<HistoryModel> mDataList = new ArrayList<>();

    private Query query;
    private ValueEventListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_items_purchase_history, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progressBar);
        tvTotal = root.findViewById(R.id.tvTotal);
        tvEmpty = root.findViewById(R.id.tvEmpty);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setClipToPadding(false);
        mAdapter = new HistoryAdapter(requireContext(), mDataList);
        recyclerView.setAdapter(mAdapter);

        String rfid = MyPreferenceManager.getInstance(requireContext()).getString("rfid");
        if (rfid == null || rfid.isEmpty()) return;

        query = FirebaseDatabase.getInstance()
                .getReference("buyitems")
                .orderByChild("customerrfid")
                .equalTo(rfid);

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
                mAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(mDataList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PurchaseHistory", "Query cancelled", error.toException());
                progressBar.setVisibility(View.GONE);
                Snackbar.make(root, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        };
        query.addValueEventListener(listener);
    }

    @Override
    public void onDestroyView() {
        if (query != null && listener != null) {
            query.removeEventListener(listener);
        }
        listener = null;
        query = null;
        super.onDestroyView();
    }
}
