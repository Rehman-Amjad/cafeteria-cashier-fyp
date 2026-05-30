package com.technogenis.cafeteriacashier.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.admin.AdminCheckListActivity;
import com.technogenis.cafeteriacashier.model.CustomerModel;
import com.technogenis.cafeteriacashier.util.HomeFormat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.MyViewHolder> {

    private final Context context;
    private final List<CustomerModel> mDatalist;
    private final Map<String, Integer> balanceByRfid;
    private final int lowBalanceThreshold;

    /** Backward-compatible: no balances shown. */
    public CustomerAdapter(Context context, List<CustomerModel> mDatalist) {
        this(context, mDatalist, Collections.emptyMap(), Integer.MIN_VALUE);
    }

    public CustomerAdapter(Context context, List<CustomerModel> mDatalist,
                           Map<String, Integer> balanceByRfid, int lowBalanceThreshold) {
        this.context = context;
        this.mDatalist = mDatalist;
        this.balanceByRfid = balanceByRfid;
        this.lowBalanceThreshold = lowBalanceThreshold;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CustomerModel model = mDatalist.get(position);
        holder.tv_name.setText(model.getCustomerName());
        holder.tv_phone.setText(model.getCustomerPhone());
        holder.tv_rfid.setText(model.getCustomerRfid());

        String rfid = model.getCustomerRfid();
        Integer balance = rfid == null ? null : balanceByRfid.get(rfid);
        if (balance != null) {
            holder.tv_balance.setVisibility(View.VISIBLE);
            holder.tv_balance.setText(context.getString(R.string.label_currency_pkr) + " "
                    + HomeFormat.groupedAmount(balance));
            int colorRes = balance < lowBalanceThreshold ? R.color.lowBalanceText : R.color.primaryColor;
            holder.tv_balance.setTextColor(ContextCompat.getColor(context, colorRes));
        } else {
            holder.tv_balance.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (rfid == null || rfid.isEmpty()) return;
            Intent intent = new Intent(context, AdminCheckListActivity.class);
            intent.putExtra("rfid", rfid);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mDatalist.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_phone, tv_rfid, tv_balance;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            tv_rfid = itemView.findViewById(R.id.tv_rfid);
            tv_balance = itemView.findViewById(R.id.tv_balance);
        }
    }
}
