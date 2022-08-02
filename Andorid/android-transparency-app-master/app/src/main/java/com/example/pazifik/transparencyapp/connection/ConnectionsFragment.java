package com.example.pazifik.transparencyapp.connection;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.example.pazifik.transparencyapp.LoggingState;
import com.example.pazifik.transparencyapp.R;

public class ConnectionsFragment extends Fragment {
    private static final String TAG = LoggingState.tagConnection;
    private static final boolean debug = LoggingState.debugConnection;

    private Context mContext;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private boolean isAutoRefreshEnabled;

    public ConnectionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters
     *
     * @param title title at the top of the screen
     * @return
     */
    public static ConnectionsFragment newInstance(String title) {
        ConnectionsFragment fragment = new ConnectionsFragment();
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
        return inflater.inflate(R.layout.fragment_connections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (debug) Log.d(TAG, "onViewCreated is called");

        if (getArguments() != null) {
            String title = getArguments().getString("title");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
        }

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_connection);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Refresh Symbol
        final ImageView refresh = view.findViewById(R.id.refresh_symbol);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConnectionTask(mContext).execute();
                refresh.setEnabled(false);
                refresh.setImageAlpha(60);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!isAutoRefreshEnabled){
                            refresh.setEnabled(true);
                            refresh.setImageAlpha(255);
                        }
                    }
                }, 4000);
            }
        });

        // Retrieve Connections by starting AsyncTask
        new ConnectionTask(mContext).execute();

        mRunnable = new Runnable() {
            public void run() {
                new ConnectionTask(mContext).execute();
                mHandler.postDelayed(mRunnable, 10000);
            }
        };
        // Auto-Refresh Switch
        isAutoRefreshEnabled = false;
        Switch autoRefreshSwitch = view.findViewById(R.id.auto_refresh_switch);
        autoRefreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (debug) Log.d(TAG, "onCheckedChanged: ON");
                    isAutoRefreshEnabled = true;
                    refresh.setEnabled(false);
                    refresh.setImageAlpha(60);
                    mHandler.postDelayed(mRunnable, 5000);
                } else {
                    if (debug) Log.d(TAG, "onCheckedChanged: OFF");
                    isAutoRefreshEnabled = false;
                    refresh.setEnabled(true);
                    refresh.setImageAlpha(255);
                    mHandler.removeCallbacks(mRunnable);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (debug) Log.d(TAG, "onPause is called");
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (debug) Log.d(TAG, "onResume is called");
        if (isAutoRefreshEnabled)
            mHandler.postDelayed(mRunnable, 5000);
    }

}
