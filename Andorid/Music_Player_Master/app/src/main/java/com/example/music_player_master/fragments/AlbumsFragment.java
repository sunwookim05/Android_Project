package com.example.music_player_master.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_player_master.MainActivity;
import com.example.music_player_master.R;
import com.example.music_player_master.interfaces.OnClickListen;

public class AlbumsFragment extends Fragment implements OnClickListen {
    protected View v;
    protected RecyclerView recyclerView;

    protected RecyclerView.LayoutManager mmanager;
    protected static AlbumAdapter albumAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.albums_fragment,container,false);
        recyclerView = v.findViewById(R.id.albums_recycleview);
        recyclerView.setHasFixedSize(true);
        mmanager=new GridLayoutManager(getContext(),2);
        albumAdapter = new AlbumAdapter(this);
        recyclerView.setLayoutManager(mmanager);
        recyclerView.setAdapter(albumAdapter);
        return v;

    }

    @Override
    public void onClick(int position) {
        Intent intent=new Intent(MainActivity.getInstance(), AlbumActivity.class).putExtra("index",position);
        startActivity(intent);
    }

}
