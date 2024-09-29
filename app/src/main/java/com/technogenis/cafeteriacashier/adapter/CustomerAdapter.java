package com.technogenis.cafeteriacashier.adapter;

import android.annotation.SuppressLint;
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
import com.technogenis.cafeteriacashier.model.HistoryModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.MyViewHolder>{


    private Context context;
    private List<CustomerModel> mDatalist;

    public CustomerAdapter(Context context, List<CustomerModel> mDatalist) {
        this.context = context;
        this.mDatalist = mDatalist;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myview= LayoutInflater.from(context).inflate(R.layout.customer_list,parent,false);


        return new MyViewHolder(myview);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        CustomerModel model = mDatalist.get(position);


        holder.tv_name.setText(model.getCustomerName());
        holder.tv_phone.setText(model.getCustomerPhone());
        holder.tv_rfid.setText(model.getCustomerRfid());

//        holder.tvBalance.setText("Balance: RS " + model.getCustomerBalance());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminCheckListActivity.class);
            intent.putExtra("rfid",model.getCustomerRfid());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return mDatalist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tv_name,tv_phone,tv_rfid;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name=itemView.findViewById(R.id.tv_name);
            tv_phone=itemView.findViewById(R.id.tv_phone);
            tv_rfid=itemView.findViewById(R.id.tv_rfid);
//            tvBalance=itemView.findViewById(R.id.tvBalance);
        }
    }
}
