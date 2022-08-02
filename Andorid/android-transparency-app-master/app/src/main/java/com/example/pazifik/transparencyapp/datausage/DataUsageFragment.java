package com.example.pazifik.transparencyapp.datausage;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pazifik.transparencyapp.LoggingState;
import com.example.pazifik.transparencyapp.R;
import com.example.pazifik.transparencyapp.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

public class DataUsageFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = LoggingState.tagDataUsage;
    private static final boolean debug = LoggingState.debugDataUsage;

    public static final int REQUEST_READ_PHONE_STATE = 111;
    public static final int REQUEST_USAGE_STATS = 222;

    private Context mContext;
    private AsyncTask asyncTask = null;
    private int spinnerItemPos = 0;
    private boolean isSpinnerLoaded = false;

    public DataUsageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters
     *
     * @param title title at the top of the screen
     * @return
     */
    public static DataUsageFragment newInstance(String title) {
        DataUsageFragment fragment = new DataUsageFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        if (debug) Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (debug) Log.d(TAG, "onCreateView is called");
        return inflater.inflate(R.layout.fragment_data_usage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (debug) Log.d(TAG, "onViewCreated is called");

        if (getArguments() != null) {
            String title = getArguments().getString("title");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
        }

        Context context = view.getContext();

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_data);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Spinner
        Spinner spinner = view.findViewById(R.id.spinner);
        List<String> list = new ArrayList<>();
        list.add((String) getText(R.string.day));
        list.add((String) getText(R.string.week));
        list.add((String) getText(R.string.month));
        list.add((String) getText(R.string.tree_months));
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Refresh Symbol
        ImageView refresh = view.findViewById(R.id.refresh_symbol);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    useTrafficStats();
                } else {
                    useNetworkStatsManager();
                }
            }
        });

        // Data Usage
        if (savedInstanceState == null) {
            if (debug) Log.d(TAG, "onViewCreated: savedInstanceState is NULL");
            // if current API Level < 23 (Marshmallow), use TrafficStats, else NetworkStatsManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                spinner.setVisibility(View.GONE);
                useTrafficStats();
            } else {
                useNetworkStatsManager();
            }
        } else {
            if (debug) Log.d(TAG, "onViewCreated: savedInstanceState NOT NULL");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (debug) Log.d(TAG, "onActivityResult: called");
        if (requestCode == REQUEST_USAGE_STATS) {
            if (!PermissionUtil.checkPermissionUsageStatsGranted(mContext)) {
                Toast.makeText(mContext, R.string.networkstats_usage_dialog_message_cancel, Toast.LENGTH_LONG).show();
            } else {
                useNetworkStatsManager();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (debug) Log.d(TAG, "onRequestPermissionsResult: called");
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE: {
                if (debug) Log.d(TAG, "onRequestPermissionsResult: case REQUEST_READ_PHONE_STATE");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do the task you need to do.
                    if (debug) Log.d(TAG, "onRequestPermissionsResult: GRANTED");
                } else {
                    if (debug) Log.d(TAG, "onRequestPermissionsResult: DENIED");
                    // permission denied, disable the functionality that depends on this permission.
                    Toast.makeText(mContext, R.string.networkstats_phonestats_dialog_message_cancel, Toast.LENGTH_SHORT).show();
                }
                asyncTask = new DataUsageTask(mContext, spinnerItemPos).execute();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (debug) Log.d(TAG, "onItemSelected: called");
        if (isSpinnerLoaded) {
            if (debug) Log.d(TAG, "onItemSelected: spinnerLoaded first time");
//            Toast.makeText(parent.getContext(), "Selected Option : " + position, Toast.LENGTH_SHORT).show();
//            Toast.makeText(parent.getContext(), "Selected Option : " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
            if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
                asyncTask.cancel(true);
            asyncTask = new DataUsageTask(mContext, position).execute();
            spinnerItemPos = position;
        }
        isSpinnerLoaded = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void useTrafficStats() {
        if (debug) Log.d(TAG, "using TrafficStats");

        long mStartRX = TrafficStats.getTotalRxBytes();
        long mStartTX = TrafficStats.getTotalTxBytes();
        if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(R.string.trafficstats_dialog_title);
            alert.setMessage(R.string.trafficstats_dialog_message);
            alert.show();
        } else {
            asyncTask = new DataUsageTask(mContext, spinnerItemPos).execute();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void useNetworkStatsManager() {
        if (debug) Log.d(TAG, "using NetworkStatsManager");

        // Dialog to request access to usage stats (PACKAGE_USAGE_STATS Permission)
        if (!PermissionUtil.checkPermissionUsageStatsGranted(mContext)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(R.string.dialog_title);
            alert.setMessage(R.string.networkstats_usage_dialog_message);
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivityForResult(intent, REQUEST_USAGE_STATS);
                }
            });
            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    Toast.makeText(mContext, R.string.networkstats_usage_dialog_message_cancel, Toast.LENGTH_LONG).show();
                }
            });
            alert.show();
        } else {

            PermissionUtil.checkPermission(mContext, Manifest.permission.READ_PHONE_STATE, new PermissionUtil.PermissionAskListener() {
                @Override
                public void onPermissionAsk() {
                    if (debug) Log.d(TAG, "onPermissionAsk: called");
                    // Dialog to request READ_PHONE_STATS Permission
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setTitle(R.string.dialog_title);
                    alert.setMessage(R.string.networkstats_phonestats_dialog_message);
                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                        }
                    });
                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            Toast.makeText(mContext, R.string.networkstats_phonestats_dialog_message_cancel, Toast.LENGTH_LONG).show();
                            asyncTask = new DataUsageTask(mContext, spinnerItemPos).execute();
                        }
                    });
                    alert.show();
                }

                @Override
                public void onPermissionPreviouslyDenied() {
                    if (debug) Log.d(TAG, "onPermissionPreviouslyDenied: called");
                    //show a dialog explaining permission and then request permission
                    Toast.makeText(mContext, R.string.networkstats_phonestats_dialog_message_cancel, Toast.LENGTH_SHORT).show();
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                    asyncTask = new DataUsageTask(mContext, spinnerItemPos).execute();
                }

                @Override
                public void onPermissionDisabled() {
                    if (debug) Log.d(TAG, "onPermissionDisabled: called");
                    Toast.makeText(mContext, R.string.networkstats_phonestats_dialog_message_cancel, Toast.LENGTH_SHORT).show();
                    asyncTask = new DataUsageTask(mContext, spinnerItemPos).execute();
                }

                @Override
                public void onPermissionGranted() {
                    if (debug) Log.d(TAG, "onPermissionGranted: called");
                    asyncTask = new DataUsageTask(mContext, spinnerItemPos).execute();
                }
            });
        }
    }
}
