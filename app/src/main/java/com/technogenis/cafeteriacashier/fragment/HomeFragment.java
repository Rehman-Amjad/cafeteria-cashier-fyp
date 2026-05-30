package com.technogenis.cafeteriacashier.fragment;

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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.DashboardActivity;
import com.technogenis.cafeteriacashier.MyPreferenceManager;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.HistoryAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.HomeFormat;
import com.technogenis.cafeteriacashier.util.SafeParse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int LOW_BALANCE_THRESHOLD = 100;
    private static final int RECENT_LIMIT = 3;
    private static final String PAYMENT_CASH = "Cash";

    private TextView tvGreeting, tvName, tvRfid, tvBalance;
    private TextView tvSpentValue, tvOrdersValue, tvSplitValue;
    private TextView tvEmptyRecent;
    private MaterialCardView cardLowBalance;
    private MaterialButton btnViewAll;
    private RecyclerView rvRecent;
    private View root;

    private final List<HistoryModel> recentList = new ArrayList<>();
    private HistoryAdapter recentAdapter;

    private String rfid;
    private Query buyitemsQuery;
    private ValueEventListener buyitemsListener;
    private DatabaseReference customerRef;
    private ValueEventListener customerListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        tvGreeting = root.findViewById(R.id.tvGreeting);
        tvName = root.findViewById(R.id.tvName);
        tvRfid = root.findViewById(R.id.tvRfid);
        tvBalance = root.findViewById(R.id.tvBalance);
        tvSpentValue = root.findViewById(R.id.tvSpentValue);
        tvOrdersValue = root.findViewById(R.id.tvOrdersValue);
        tvSplitValue = root.findViewById(R.id.tvSplitValue);
        tvEmptyRecent = root.findViewById(R.id.tvEmptyRecent);
        cardLowBalance = root.findViewById(R.id.cardLowBalance);
        btnViewAll = root.findViewById(R.id.btnViewAllPurchases);
        rvRecent = root.findViewById(R.id.rvRecent);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecent.setNestedScrollingEnabled(false);
        recentAdapter = new HistoryAdapter(requireContext(), recentList);
        rvRecent.setAdapter(recentAdapter);

        btnViewAll.setOnClickListener(v -> {
            if (requireActivity() instanceof DashboardActivity) {
                ((DashboardActivity) requireActivity()).showPurchaseHistory();
            }
        });

        applyGreeting();

        rfid = MyPreferenceManager.getInstance(requireContext()).getString("rfid");
        if (rfid == null || rfid.isEmpty()) {
            // No session — outer activity will route to login on next resume.
            return;
        }
        tvRfid.setText(getString(R.string.label_rfid) + ": " + rfid);

        attachCustomerListener();
        attachBuyitemsListener();
    }

    private void applyGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int[] greetings = {
                R.string.greeting_morning,
                R.string.greeting_afternoon,
                R.string.greeting_evening
        };
        tvGreeting.setText(getString(greetings[HomeFormat.greetingIndex(hour)]));
    }

    private void attachCustomerListener() {
        customerRef = FirebaseDatabase.getInstance().getReference("customers").child(rfid);
        customerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("customername").getValue(String.class);
                tvName.setText(name != null && !name.isEmpty() ? name : rfid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Customer read cancelled", error.toException());
                tvName.setText(rfid);
            }
        };
        customerRef.addValueEventListener(customerListener);
    }

    private void attachBuyitemsListener() {
        buyitemsQuery = FirebaseDatabase.getInstance()
                .getReference("buyitems")
                .orderByChild("customerrfid")
                .equalTo(rfid);

        buyitemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalSpent = 0;
                int orders = 0;
                int cardCount = 0;
                int cashCount = 0;
                long latestId = -1L;
                String latestBalance = null;

                List<HistoryModel> all = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    HistoryModel h = snap.getValue(HistoryModel.class);
                    if (h == null) continue;
                    orders++;
                    totalSpent += SafeParse.parseIntOr(h.getItemPrice(), 0);
                    if (PAYMENT_CASH.equalsIgnoreCase(h.getCustomerPayment())) {
                        cashCount++;
                    } else {
                        cardCount++;
                    }
                    long id = SafeParse.parseLongOr(h.getId(), 0L);
                    if (id > latestId) {
                        latestId = id;
                        latestBalance = h.getCustomerBalance();
                    }
                    all.add(h);
                }

                // Balance hero
                int balance = SafeParse.parseIntOr(latestBalance, 0);
                tvBalance.setText(getString(R.string.label_currency_pkr) + " "
                        + HomeFormat.groupedAmount(balance));

                // Low-balance banner
                cardLowBalance.setVisibility(
                        balance < LOW_BALANCE_THRESHOLD ? View.VISIBLE : View.GONE);

                // Stats
                tvSpentValue.setText(getString(R.string.label_currency_pkr) + " "
                        + HomeFormat.groupedAmount(totalSpent));
                tvOrdersValue.setText(String.valueOf(orders));
                tvSplitValue.setText(cardCount + " / " + cashCount);

                // Recent 3 (newest first)
                Collections.sort(all, new Comparator<HistoryModel>() {
                    @Override
                    public int compare(HistoryModel a, HistoryModel b) {
                        long ia = SafeParse.parseLongOr(a.getId(), 0L);
                        long ib = SafeParse.parseLongOr(b.getId(), 0L);
                        return Long.compare(ib, ia);
                    }
                });
                recentList.clear();
                for (int i = 0; i < all.size() && i < RECENT_LIMIT; i++) {
                    recentList.add(all.get(i));
                }
                recentAdapter.notifyDataSetChanged();
                tvEmptyRecent.setVisibility(recentList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Buyitems query cancelled", error.toException());
                if (root != null) {
                    Snackbar.make(root, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        };
        buyitemsQuery.addValueEventListener(buyitemsListener);
    }

    @Override
    public void onDestroyView() {
        if (buyitemsQuery != null && buyitemsListener != null) {
            buyitemsQuery.removeEventListener(buyitemsListener);
        }
        if (customerRef != null && customerListener != null) {
            customerRef.removeEventListener(customerListener);
        }
        buyitemsQuery = null;
        buyitemsListener = null;
        customerRef = null;
        customerListener = null;
        super.onDestroyView();
    }
}
