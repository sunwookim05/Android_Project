package com.example.pazifik.transparencyapp.datausage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pazifik.transparencyapp.R;
import com.example.pazifik.transparencyapp.util.Util;

import java.text.DecimalFormat;
import java.util.List;


public class DataItemRecyclerViewAdapter extends RecyclerView.Adapter<DataItemRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<AppDataInfo> appDataList;
    private DecimalFormat df = new DecimalFormat("#.#");

    public DataItemRecyclerViewAdapter(Context context, List<AppDataInfo> appDataList) {
        this.context = context;
        this.appDataList = appDataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_data_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.appIcon.setImageDrawable(appDataList.get(position).icon);
        holder.appName.setText(appDataList.get(position).name);
        holder.dataAmount.setText(Util.getFileSize(appDataList.get(position).transmitted));
        double progress = appDataList.get(position).progress;
        if (progress >= 0.1) {
            holder.percentage.setText(df.format(progress) + " %");
        } else {
            holder.percentage.setText("");
        }

        holder.progressBar.setMax(100);
        holder.progressBar.setProgress((int) appDataList.get(position).progress);
        holder.progressBar.setVisibility(View.VISIBLE);

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "UID: " + appDataList.get(position).uid + " , package: " + appDataList.get(position).packageName, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return appDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView dataAmount;
        TextView percentage;
        ProgressBar progressBar;
        ConstraintLayout parentLayout;

        public ViewHolder(View view) {
            super(view);
            appIcon = view.findViewById(R.id.app_icon);
            appName = view.findViewById(R.id.app_name);
            dataAmount = view.findViewById(R.id.data_amount);
            percentage = view.findViewById(R.id.percentage);
            progressBar = view.findViewById(R.id.progressBar);
            parentLayout = view.findViewById(R.id.parent_layout_list_item);
        }
    }
}
