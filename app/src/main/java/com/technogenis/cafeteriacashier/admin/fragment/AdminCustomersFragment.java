package com.technogenis.cafeteriacashier.admin.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.CustomerAdapter;
import com.technogenis.cafeteriacashier.model.CustomerModel;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.AdminStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminCustomersFragment extends Fragment {

    private static final int LOW_BALANCE_THRESHOLD = 100;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EditText etSearch;

    private CustomerAdapter adapter;
    private final List<CustomerModel> allCustomers = new ArrayList<>();
    private final List<CustomerModel> shown = new ArrayList<>();
    private final Map<String, Integer> balanceByRfid = new HashMap<>();

    private DatabaseReference customersRef, buyitemsRef;
    private ValueEventListener customersListener, buyitemsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_customers, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progressBar);
        tvEmpty = root.findViewById(R.id.tvEmpty);
        etSearch = root.findViewById(R.id.etSearch);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CustomerAdapter(requireContext(), shown, balanceByRfid, LOW_BALANCE_THRESHOLD);
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                applyFilter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        buyitemsRef = FirebaseDatabase.getInstance().getReference("buyitems");
        buyitemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HistoryModel> items = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    HistoryModel h = s.getValue(HistoryModel.class);
                    if (h != null) items.add(h);
                }
                balanceByRfid.clear();
                balanceByRfid.putAll(AdminStats.latestBalanceByRfid(items));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminCustomers", "buyitems cancelled", error.toException());
            }
        };
        buyitemsRef.addValueEventListener(buyitemsListener);

        customersRef = FirebaseDatabase.getInstance().getReference("customers");
        customersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCustomers.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    CustomerModel c = s.getValue(CustomerModel.class);
                    if (c != null) allCustomers.add(c);
                }
                applyFilter(etSearch.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminCustomers", "customers cancelled", error.toException());
                progressBar.setVisibility(View.GONE);
            }
        };
        customersRef.addValueEventListener(customersListener);
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        shown.clear();
        for (CustomerModel c : allCustomers) {
            String name = c.getCustomerName() == null ? "" : c.getCustomerName().toLowerCase(Locale.getDefault());
            String rfid = c.getCustomerRfid() == null ? "" : c.getCustomerRfid().toLowerCase(Locale.getDefault());
            if (q.isEmpty() || name.contains(q) || rfid.contains(q)) {
                shown.add(c);
            }
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (customersRef != null && customersListener != null) {
            customersRef.removeEventListener(customersListener);
        }
        if (buyitemsRef != null && buyitemsListener != null) {
            buyitemsRef.removeEventListener(buyitemsListener);
        }
        customersRef = null; customersListener = null;
        buyitemsRef = null; buyitemsListener = null;
        super.onDestroyView();
    }
}
