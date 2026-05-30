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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.AdminStats;
import com.technogenis.cafeteriacashier.util.HomeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminOverviewFragment extends Fragment {

    private TextView tvRevenue, tvCustomers, tvTransactions, tvSplit, tvTodaySales;

    private DatabaseReference customersRef, buyitemsRef;
    private ValueEventListener customersListener, buyitemsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_overview, container, false);
        tvRevenue = root.findViewById(R.id.tvAdminRevenue);
        tvCustomers = root.findViewById(R.id.tvAdminTotalCustomers);
        tvTransactions = root.findViewById(R.id.tvAdminTransactions);
        tvSplit = root.findViewById(R.id.tvAdminSplit);
        tvTodaySales = root.findViewById(R.id.tvAdminTodaySales);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        customersRef = FirebaseDatabase.getInstance().getReference("customers");
        customersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvCustomers.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminOverview", "customers cancelled", error.toException());
            }
        };
        customersRef.addValueEventListener(customersListener);

        buyitemsRef = FirebaseDatabase.getInstance().getReference("buyitems");
        buyitemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HistoryModel> items = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    HistoryModel h = s.getValue(HistoryModel.class);
                    if (h != null) items.add(h);
                }
                String pkr = getString(R.string.label_currency_pkr) + " ";
                tvRevenue.setText(pkr + HomeFormat.groupedAmount(AdminStats.totalRevenue(items)));
                tvTransactions.setText(String.valueOf(AdminStats.transactionCount(items)));
                int[] split = AdminStats.cardCashSplit(items);
                tvSplit.setText(split[0] + " / " + split[1]);

                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                long dayStart = c.getTimeInMillis();
                long dayEnd = dayStart + 24L * 60L * 60L * 1000L;
                tvTodaySales.setText(pkr
                        + HomeFormat.groupedAmount(AdminStats.salesOnDay(items, dayStart, dayEnd)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminOverview", "buyitems cancelled", error.toException());
            }
        };
        buyitemsRef.addValueEventListener(buyitemsListener);
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
