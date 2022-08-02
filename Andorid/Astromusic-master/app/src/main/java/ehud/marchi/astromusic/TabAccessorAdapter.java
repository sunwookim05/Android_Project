package ehud.marchi.astromusic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabAccessorAdapter extends FragmentPagerAdapter {

    public TabAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case  0:
                MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
                return  musicPlayerFragment;
            case  1:
                UploadMusicFragment uploadMusicFragment = new UploadMusicFragment();
                return  uploadMusicFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case  0:
                return  "Music Player";
            case  1:
                return  "Upload Music";
            default:
                return null;
        }
    }
}
