package com.example.pazifik.transparencyapp.connection;

import android.content.Context;
import android.net.TrafficStats;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pazifik.transparencyapp.R;
import com.example.pazifik.transparencyapp.datausage.DataUsageTask;
import com.example.pazifik.transparencyapp.util.PermissionUtil;
import com.example.pazifik.transparencyapp.util.Util;

import java.util.List;


public class ConnectionItemRecyclerViewAdapter extends RecyclerView.Adapter<ConnectionItemRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<ConnectionListItem> appConnectionList;

    public ConnectionItemRecyclerViewAdapter(Context context, List<ConnectionListItem> appConnectionList) {
        this.context = context;
        this.appConnectionList = appConnectionList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_appconnection_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final int uid = appConnectionList.get(position).uid;
        holder.appIcon.setImageDrawable(appConnectionList.get(position).icon);
        holder.appName.setText(appConnectionList.get(position).name);
        holder.remote.setText(appConnectionList.get(position).remote);
        holder.hostname.setText(appConnectionList.get(position).hostname);
        String city = appConnectionList.get(position).city;
        String countryCode = appConnectionList.get(position).countryCode;
        holder.country.setText(city + ", " + countryCode);
        if (city.equals("") && !countryCode.equals("")) {
            holder.country.setText(appConnectionList.get(position).country + " (" + countryCode + ")");
        } else if (city.equals("") && countryCode.equals("")) {
            holder.country.setText("");
        }
        holder.company.setText(appConnectionList.get(position).company);
        if (appConnectionList.get(position).state.equals("ESTABLISHED"))
            holder.parentLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        else
            holder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.light_grey));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Data Usage");
                LayoutInflater factory = LayoutInflater.from(context);
                final View alertView = factory.inflate(R.layout.app_data_usage, null);
                TextView appName = alertView.findViewById(R.id.app_name_label);
                TextView uidText = alertView.findViewById(R.id.app_uid);
                TextView dayValue = alertView.findViewById(R.id.day_value);
                TextView weekValue = alertView.findViewById(R.id.week_value);
                TextView monthValue = alertView.findViewById(R.id.month_value);
                TextView last3MonthsValue = alertView.findViewById(R.id.last3months_value);
                ImageView appIcon = alertView.findViewById(R.id.app_icon);

                appName.setText(appConnectionList.get(position).name);
                uidText.setText("UID: " + uid);
                appIcon.setImageDrawable(appConnectionList.get(position).icon);

                long dayUsage = 0;
                long weekUsage = 0;
                long monthUsage = 0;
                long last3MonthsUsage = 0;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // show Dialog if Permission is not granted yet, refer the user to 'Data Usage'
                    if (!PermissionUtil.checkPermissionUsageStatsGranted(context)) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle(R.string.dialog_title);
                        alert.setMessage(R.string.connection_app_usage_dialog_message);
                        alert.setPositiveButton(R.string.ok, null);
                        alert.show();
                        return;
                    } else {
                        dayUsage = DataUsageTask.getBytesWifiUID(context, uid, Util.getCalendarToday())[1] + DataUsageTask.getBytesMobileUID(context, uid, Util.getCalendarToday())[1];
                        weekUsage = DataUsageTask.getBytesWifiUID(context, uid, Util.getCalendarCurrentWeek())[1] + DataUsageTask.getBytesMobileUID(context, uid, Util.getCalendarCurrentWeek())[1];
                        monthUsage = DataUsageTask.getBytesWifiUID(context, uid, Util.getCalendarCurrentMonth())[1] + DataUsageTask.getBytesMobileUID(context, uid, Util.getCalendarCurrentMonth())[1];
                        last3MonthsUsage = DataUsageTask.getBytesWifiUID(context, uid, Util.getCalendarLastThreeMonths())[1] + DataUsageTask.getBytesMobileUID(context, uid, Util.getCalendarLastThreeMonths())[1];
                    }
                } else {
                    dayUsage = TrafficStats.getUidTxBytes(uid); // TrafficStats return value for time frame since device boot
                    TextView dayLabel = alertView.findViewById(R.id.day_label);
                    dayLabel.setText("Since device boot: ");
                    // Hide unnecessary views
                    TextView weekLabel = alertView.findViewById(R.id.week_label);
                    TextView monthLabel = alertView.findViewById(R.id.month_label);
                    TextView yearLabel = alertView.findViewById(R.id.last3months_label);
                    weekLabel.setVisibility(View.GONE);
                    monthLabel.setVisibility(View.GONE);
                    yearLabel.setVisibility(View.GONE);
                    weekValue.setVisibility(View.GONE);
                    monthValue.setVisibility(View.GONE);
                    last3MonthsValue.setVisibility(View.GONE);

                }
                // Set values
                dayValue.setText(Util.getFileSize(dayUsage));
                weekValue.setText(Util.getFileSize(weekUsage));
                monthValue.setText(Util.getFileSize(monthUsage));
                last3MonthsValue.setText(Util.getFileSize(last3MonthsUsage));

                builder.setView(alertView);
                builder.setPositiveButton("OK", null);
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return appConnectionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView remote;
        TextView hostname;
        TextView country;
        TextView company;
        ConstraintLayout parentLayout;

        public ViewHolder(View view) {
            super(view);
            appIcon = view.findViewById(R.id.app_icon);
            appName = view.findViewById(R.id.app_name);
            remote = view.findViewById(R.id.remote_host);
            hostname = view.findViewById(R.id.hostname_label);
            country = view.findViewById(R.id.country_label);
            company = view.findViewById(R.id.company_label);
            parentLayout = view.findViewById(R.id.parent_layout_list_item);
        }
    }


}
