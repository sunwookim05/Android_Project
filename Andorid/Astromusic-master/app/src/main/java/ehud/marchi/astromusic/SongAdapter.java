package ehud.marchi.astromusic;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> implements SongMoveCallback.SongTouchHelperContract {
    private ArrayList<Song> songs;
    private Song selectedSong = null;
    Context m_Context;
    int m_SelectedItemIndex = 0;
    onSongSelectedListener onSongSelectedListener;
    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(songs, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(songs, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(SongViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.DKGRAY);
        myViewHolder.rowView.setAlpha(0.8f);
        myViewHolder.rowView.startAnimation(AnimationUtils.loadAnimation(m_Context,R.anim.shake));

    }

    @Override
    public void onRowClear(SongViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.TRANSPARENT);
        myViewHolder.rowView.setAlpha(1f);
        myViewHolder.rowView.clearAnimation();
        saveData();
        notifyDataSetChanged();
    }

    @Override
    public void onRowSwipeRight(SongViewHolder myViewHolder) {
        myViewHolder.rowView.clearAnimation();
        final Dialog dialog = new Dialog(m_Context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_delete);
        Button yesBtn = (Button) dialog.findViewById(R.id.yes_btn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = myViewHolder.getAdapterPosition();
                songs.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                saveData();
                Toast.makeText(m_Context, "Song Deleted!", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRowClear(myViewHolder);
                notifyItemChanged(myViewHolder.getAdapterPosition());
                dialog.dismiss();
            }
        });
        dialog.show();

    }
    private void saveData() {
        try {
            FileOutputStream fos = m_Context.openFileOutput("songList.dat", MODE_PRIVATE);
            ObjectOutputStream oow = new ObjectOutputStream(fos);
            oow.writeObject(MusicPlayerService.songs);
            oow.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface onSongSelectedListener {
        void onSongSelected(int songIndex);

    }
    public class SongViewHolder extends RecyclerView.ViewHolder {
        public ImageView songImage;
        public TextView songName, artistName;
        View rowView;
        public SongViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            songImage = itemView.findViewById(R.id.song_image);
            songName = itemView.findViewById(R.id.songName);
            artistName = itemView.findViewById(R.id.artist);
            rowView = itemView;
        }
    }

    public SongAdapter(Context context, ArrayList<Song> songsList, Song selected, onSongSelectedListener onSongSelectedListener) {
        songs = songsList;
        this.m_Context = context;
        this.selectedSong = selected;
        this.onSongSelectedListener = onSongSelectedListener;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View gamesView = inflater.inflate(R.layout.song_item, parent, false);
        SongViewHolder viewHolder = new SongViewHolder(gamesView);
        return viewHolder;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final SongViewHolder holder, final int position) {
            Song currentSong = songs.get(position);
            Glide.with(m_Context).load(currentSong.getImageURL()).placeholder(R.drawable.galaxy).into(holder.songImage);
            holder.songName.setText(currentSong.getSongName());
            holder.artistName.setText(currentSong.getSongArtist());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_SelectedItemIndex = position;
                    selectedSong = currentSong;
                    onSongSelectedListener.onSongSelected(m_SelectedItemIndex);
                }
            });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


}
