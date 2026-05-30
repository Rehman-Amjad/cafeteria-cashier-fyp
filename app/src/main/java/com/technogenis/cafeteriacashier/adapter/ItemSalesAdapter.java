package com.technogenis.cafeteriacashier.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.model.ItemSalesModel;
import com.technogenis.cafeteriacashier.util.HomeFormat;

import java.util.List;

public class ItemSalesAdapter extends RecyclerView.Adapter<ItemSalesAdapter.VH> {

    private final Context context;
    private final List<ItemSalesModel> data;

    public ItemSalesAdapter(Context context, List<ItemSalesModel> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_sales_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ItemSalesModel m = data.get(position);
        holder.tvName.setText(m.getName());
        holder.tvQtySold.setText(context.getString(R.string.label_sold) + ": " + m.getQtySold());
        holder.tvRevenue.setText(context.getString(R.string.label_currency_pkr) + " "
                + HomeFormat.groupedAmount(m.getRevenue()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvQtySold, tvRevenue;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQtySold = itemView.findViewById(R.id.tvQtySold);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
        }
    }
}
