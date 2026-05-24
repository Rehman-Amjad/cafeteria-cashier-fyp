package com.technogenis.cafeteriacashier.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.admin.AdminCheckListActivity;
import com.technogenis.cafeteriacashier.model.CustomerModel;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.MyViewHolder> {

    private final Context context;
    private final List<CustomerModel> mDatalist;

    public CustomerAdapter(Context context, List<CustomerModel> mDatalist) {
        this.context = context;
        this.mDatalist = mDatalist;
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

        holder.itemView.setOnClickListener(v -> {
            String rfid = model.getCustomerRfid();
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
        TextView tv_name, tv_phone, tv_rfid;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            tv_rfid = itemView.findViewById(R.id.tv_rfid);
        }
    }
}
