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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.MyPreferenceManager;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.model.HistoryModel;

public class HomeFragment extends Fragment {

    private TextView tvItemName, tvQty, tvPrice, tvType, tvBalance;
    private View root;

    private Query query;
    private ValueEventListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        tvItemName = root.findViewById(R.id.tvItemName);
        tvQty = root.findViewById(R.id.tvQty);
        tvPrice = root.findViewById(R.id.tvPrice);
        tvType = root.findViewById(R.id.tvType);
        tvBalance = root.findViewById(R.id.tvBalance);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String rfid = MyPreferenceManager.getInstance(requireContext()).getString("rfid");
        if (rfid == null || rfid.isEmpty()) {
            // No session — outer activity will route to login on next resume.
            return;
        }
        query = FirebaseDatabase.getInstance()
                .getReference("buyitems")
                .orderByChild("customerrfid")
                .equalTo(rfid);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HistoryModel last = null;
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    HistoryModel h = s.getValue(HistoryModel.class);
                    if (h != null) last = h;
                }
                if (last != null) {
                    tvItemName.setText(last.getItemName());
                    tvQty.setText(getString(R.string.label_qty) + ": " + last.getItemQty());
                    tvPrice.setText(getString(R.string.label_price) + ": "
                            + getString(R.string.label_currency_pkr) + " " + last.getItemPrice());
                    tvType.setText(getString(R.string.label_payment_via) + ": " + last.getCustomerPayment());
                    tvBalance.setText(getString(R.string.label_balance) + ": "
                            + getString(R.string.label_currency_pkr) + " " + last.getCustomerBalance());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Query cancelled", error.toException());
                if (root != null) {
                    Snackbar.make(root, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
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
