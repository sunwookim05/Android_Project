package ehud.marchi.astromusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabAccessorAdapter tabAccessorAdapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAccessorAdapter);
        sharedPreferences = getSharedPreferences("song_first", MODE_PRIVATE);
        Boolean firstTime = sharedPreferences.getBoolean("firstTime", true);

        if (firstTime) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            initSongs();
            saveData();
            editor.putBoolean("firstTime", false);
            editor.apply();
        } else
            loadData();
    }

    private void loadData() {
        try {
            FileInputStream fis = openFileInput("songList.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            MusicPlayerService.songs = (ArrayList<Song>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            FileOutputStream fos = openFileOutput("songList.dat", MODE_PRIVATE);
            ObjectOutputStream oow = new ObjectOutputStream(fos);
            oow.writeObject(MusicPlayerService.songs);
            oow.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initSongs()
    {
        MusicPlayerService.songs.add(new Song("So Close", "NOTD, Felix Jaehn (ft. Georgia Ku & Captain Cuts)", "https://upload.wikimedia.org/wikipedia/en/4/47/NOTD_and_Felix_Jaehn_-_So_Close.png", "https://www.mboxdrive.com/NOTD-Felix-Jaehn-Captain-Cuts-Georgia-Ku-So-Close.mp3"));
        MusicPlayerService.songs.add(new Song("Mood", "24kGoldn (ft. iann dior)", "https://upload.wikimedia.org/wikipedia/he/thumb/e/eb/MoodSingle.jpg/1200px-MoodSingle.jpg", "https://www.mboxdrive.com/24kGoldn-Mood-Official-Audio-ft.-Iann-Dior.mp3"));
        MusicPlayerService.songs.add(new Song("No Problem", "Chance the Rapper (ft. 2 Chainz & Lil Wayne)", "https://m.media-amazon.com/images/I/81iXE2XNYDL._SS500_.jpg", "https://www.mboxdrive.com/Chance%20the%20Rapper%20ft.%202%20Chainz%20&%20Lil%20Wayne%20-%20No%20Problem%20(Official%20Video).mp3"));
        MusicPlayerService.songs.add(new Song("Higher", "Clean Bandit (feat. iann dior)", "https://static.billboard.com/files/2021/02/Clean-Bandit-x-iann-dior-Press-Photo-Credit-Nikii-Kane-billboard-1548-1614193032-compressed.jpg", "https://www.mboxdrive.com/Clean%20Bandit%20-%20Higher%20(feat.%20iann%20dior)%20[Official%20Video].mp3"));
        MusicPlayerService.songs.add(new Song("Rewind", "Krewella & Yellow Claw", "https://www.edmtunes.com/wp-content/uploads/2020/07/bab65cfcb4815114b516fcff4f5b5012.jpg", "https://www.mboxdrive.com/Krewella%20&%20Yellow%20Claw%20-%20Rewind%20(Official%20Music%20Video).mp3"));
        MusicPlayerService.songs.add(new Song("Sky Walker", "Miguel (ft. Travis Scott)", "https://images.genius.com/67e5b4e0572b354606cd0dc5529030ee.1000x1000x1.jpg", "https://www.mboxdrive.com/Miguel%20-%20Sky%20Walker%20(Official%20Video)%20ft.%20Travis%20Scott.mp3"));
        MusicPlayerService.songs.add(new Song("No Money", "Galantis", "https://img.discogs.com/0khtsQQWLxqvQ4NO_abx7ESAkmc=/fit-in/600x600/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-8333279-1459548671-8947.jpeg.jpg", "https://www.mboxdrive.com/Galantis%20-%20No%20Money%20(Radio%20Edit).mp3"));
        MusicPlayerService.songs.add(new Song("Bellyache (Marian Hill Remix)", "Billie Eilish", "https://i1.sndcdn.com/artworks-000469749213-aul99b-t500x500.jpg", "https://www.mboxdrive.com/Billie%20Eilish%20-%20Bellyache%20(Marian%20Hill%20Remix_Audio)-320.mp3"));
        MusicPlayerService.songs.add(new Song("The Hills", "The Weeknd", "https://img.discogs.com/aGRtBuJ9gsBj8TZVj3d25kzBz4I=/fit-in/600x524/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-7892739-1588905621-4284.jpeg.jpg", "https://www.mboxdrive.com/The%20Weeknd%20-%20The%20Hills%20(Official%20Video).mp3"));
        MusicPlayerService.songs.add(new Song("Holy", "Justin Bieber (ft. Chance the Rapper)", "https://upload.wikimedia.org/wikipedia/en/8/8d/Holy_-_Justin_Bieber.png", "https://www.mboxdrive.com/Justin%20Bieber%20-%20Holy%20ft.%20Chance%20the%20Rapper%20(Lyric%20Video).mp3"));
        MusicPlayerService.songs.add(new Song("Sunflower", "Post Malone, Swae Lee", "https://i.redd.it/kz2xb045j6v11.jpg", "https://www.mboxdrive.com/Post%20Malone,%20Swae%20Lee%20-%20Sunflower%20(Spider-Man%20Into%20the%20Spider-Verse).mp3"));
        MusicPlayerService.songs.add(new Song("Dive", "Salvatore Ganacci (feat. Enya and Alex Aris)", "https://i1.sndcdn.com/artworks-tcGVQMqMda9l-0-t500x500.png", "https://www.mboxdrive.com/Salvatore%20Ganacci%20-%20Dive%20(feat.%20Enya%20&%20Alex%20Aris)%20[Lyric%20Video].mp3"));
        MusicPlayerService.songs.add(new Song("Is This Love", "Bob Marley & The Wailers", "https://img.discogs.com/1TfPJLCKsLPnkgq3bdraqUjlsjc=/fit-in/600x589/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-10404971-1496773750-6855.jpeg.jpg", "https://www.mboxdrive.com/Bob%20Marley%20-%20Is%20This%20Love.mp3"));
        MusicPlayerService.songs.add(new Song("Swing", "SOFI TUKKER", "https://i1.sndcdn.com/artworks-000557006556-taniby-t500x500.jpg", "https://www.mboxdrive.com/SOFI%20TUKKER%20-%20Swing%20(Official%20Video)%20[Ultra%20Music].mp3"));
        MusicPlayerService.songs.add(new Song("Believer", "Imagine Dragons", "https://upload.wikimedia.org/wikipedia/he/5/5c/Imagine-Dragons-Believer-art.jpg", "https://www.mboxdrive.com/Imagine%20Dragons%20-%20Believer%20(Lyrics).mp3"));
        MusicPlayerService.songs.add(new Song("Pump It", "The Black Eyed Peas", "https://upload.wikimedia.org/wikipedia/en/c/cf/Pump_It.png", "https://www.mboxdrive.com/The%20Black%20Eyed%20Peas%20-%20Pump%20It%20(Radio%20Edit).mp3"));
        MusicPlayerService.songs.add(new Song("JUNGLE", "TASH SULTANA", "https://upload.wikimedia.org/wikipedia/en/8/80/Jungle_by_Tash_Sultana.jpg", "https://www.mboxdrive.com/TASH%20SULTANA%20-%20JUNGLE%20(OFFICIAL%20AUDIO).mp3"));
        MusicPlayerService.songs.add(new Song("Anxious", "Dennis Lloyd", "https://i1.sndcdn.com/artworks-Uu0TEgsTEOkym8OL-6M3vNA-t500x500.jpg", "https://www.mboxdrive.com/Dennis%20Lloyd%20-%20Anxious%20(Official%20Video).mp3"));
        MusicPlayerService.songs.add(new Song("Do You?", "TroyBoi", "https://static.billboard.com/files/media/troyboi-do-you-MV-2019-billboard-1548-compressed.jpg", "https://www.mboxdrive.com/TroyBoi%20-Do%20You%20[Official%20Video].mp3"));
    }

    @Override
    protected void onDestroy() {
        //saveData();
        super.onDestroy();
    }
}

