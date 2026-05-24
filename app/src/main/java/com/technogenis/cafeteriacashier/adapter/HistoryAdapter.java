package com.technogenis.cafeteriacashier.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.SafeParse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

    private final Context context;
    private final List<HistoryModel> mDatalist;

    public HistoryAdapter(Context context, List<HistoryModel> mDatalist) {
        this.context = context;
        this.mDatalist = mDatalist;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HistoryModel model = mDatalist.get(position);
        holder.tvItemName.setText(model.getItemName());
        holder.tvQty.setText(model.getItemQty());
        holder.tvPrice.setText(model.getItemPrice());
        holder.tvDate.setText(formatTimestamp(model.getId()));
    }

    @Override
    public int getItemCount() {
        return mDatalist.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvQty, tvPrice, tvDate;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    private static String formatTimestamp(String timestamp) {
        long millis = SafeParse.parseLongOr(timestamp, 0L);
        if (millis <= 0L) return "—";
        return DATE_FORMAT.format(new Date(millis));
    }
}
