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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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


        holder.tvItemName.setText(model.getItemName());
        holder.tvQty.setText(model.getItemQty());
        holder.tvPrice.setText(model.getItemPrice());
        holder.tvDate.setText(convertTimestampToDateTime(model.getId()));

//        holder.tvBalance.setText("Balance: RS " + model.getCustomerBalance());


    }

    @Override
    public int getItemCount() {
        return mDatalist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvItemName,tvQty,tvPrice,tvDate;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemName=itemView.findViewById(R.id.tvItemName);
            tvQty=itemView.findViewById(R.id.tvQty);
            tvPrice=itemView.findViewById(R.id.tvPrice);
            tvDate=itemView.findViewById(R.id.tvDate);
//            tvBalance=itemView.findViewById(R.id.tvBalance);
        }
    }
    public static String convertTimestampToDateTime(String timestamp) {
        try {
            // Convert the String timestamp to a long value
            long timeInMillis = Long.parseLong(timestamp);

            // Create a Date object from the timestamp
            Date date = new Date(timeInMillis);

            // Define the date and time format
            SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

            // Get the formatted date and time
            String formattedDate = dateFormatter.format(date);
            String formattedTime = timeFormatter.format(date);

            // Return the combined result
//            return formattedDate + " " + formattedTime;
            return formattedDate;

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "Invalid timestamp";
        }
    }
}
