package com.technogenis.cafeteriacashier.admin.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.ItemSalesAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.model.ItemSalesModel;
import com.technogenis.cafeteriacashier.util.AdminStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminReportsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private MaterialButton btnSortRevenue, btnSortQty;

    private ItemSalesAdapter adapter;
    private final List<ItemSalesModel> data = new ArrayList<>();
    private boolean sortByRevenue = true;

    private DatabaseReference buyitemsRef;
    private ValueEventListener buyitemsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_reports, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        tvEmpty = root.findViewById(R.id.tvEmpty);
        btnSortRevenue = root.findViewById(R.id.btnSortRevenue);
        btnSortQty = root.findViewById(R.id.btnSortQty);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ItemSalesAdapter(requireContext(), data);
        recyclerView.setAdapter(adapter);

        btnSortRevenue.setOnClickListener(v -> { sortByRevenue = true; sortAndRefresh(); });
        btnSortQty.setOnClickListener(v -> { sortByRevenue = false; sortAndRefresh(); });

        buyitemsRef = FirebaseDatabase.getInstance().getReference("buyitems");
        buyitemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HistoryModel> items = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    HistoryModel h = s.getValue(HistoryModel.class);
                    if (h != null) items.add(h);
                }
                data.clear();
                data.addAll(AdminStats.itemSales(items));
                sortAndRefresh();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminReports", "buyitems cancelled", error.toException());
            }
        };
        buyitemsRef.addValueEventListener(buyitemsListener);
    }

    private void sortAndRefresh() {
        Collections.sort(data, new Comparator<ItemSalesModel>() {
            @Override
            public int compare(ItemSalesModel a, ItemSalesModel b) {
                return sortByRevenue
                        ? Integer.compare(b.getRevenue(), a.getRevenue())
                        : Integer.compare(b.getQtySold(), a.getQtySold());
            }
        });
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (buyitemsRef != null && buyitemsListener != null) {
            buyitemsRef.removeEventListener(buyitemsListener);
        }
        buyitemsRef = null; buyitemsListener = null;
        super.onDestroyView();
    }
}
