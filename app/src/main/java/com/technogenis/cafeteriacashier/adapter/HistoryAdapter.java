package com.technogenis.cafeteriacashier.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.model.HistoryModel;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder>{


    private Context context;
    private List<HistoryModel> mDatalist;

    public HistoryAdapter(Context context, List<HistoryModel> mDatalist) {
        this.context = context;
        this.mDatalist = mDatalist;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myview= LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);


        return new MyViewHolder(myview);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        HistoryModel model = mDatalist.get(position);


        holder.tvItemName.setText("Item Name: " + model.getItemname());
        holder.tvQty.setText("QTY: " + model.getItemqty());
        holder.tvPrice.setText("Price: RS " + model.getItemprice());
        holder.tvType.setText("Type: " + model.getCustomerpayment());
        holder.tvBalance.setText("Balance: RS " + model.getCustomerblance());


    }

    @Override
    public int getItemCount() {
        return mDatalist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvItemName,tvQty,tvPrice,tvType,tvBalance;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemName=itemView.findViewById(R.id.tvItemName);
            tvQty=itemView.findViewById(R.id.tvQty);
            tvPrice=itemView.findViewById(R.id.tvPrice);
            tvType=itemView.findViewById(R.id.tvType);
            tvBalance=itemView.findViewById(R.id.tvBalance);
        }
    }
}
