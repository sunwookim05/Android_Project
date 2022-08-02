package ehud.marchi.astromusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class UploadMusicFragment extends Fragment {
    final int CAMERA_REQUEST = 1;
    final int WRITE_PERMISSION_REQUEST = 1;
    private final static int IMAGE_PICK_CODE=1000;
    private final static int PERMISSION_CODE=1001;
    Button openGalleryButton, openCameraButton, clearBtn, saveBtn;
    EditText songNameEditText, songArtistEditText, songLinkEditText;
    RelativeLayout saveLayout;
    File file;
    Uri imageUri;
    ImageView songImageView, astronaut ,spaceship;

    public UploadMusicFragment() {
        // Required empty public constructor
    }

    public static UploadMusicFragment newInstance(String param1, String param2) {
        UploadMusicFragment fragment = new UploadMusicFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        songNameEditText = getView().findViewById(R.id.song_name_input);
        songArtistEditText = getView().findViewById(R.id.song_artist_input);
        songLinkEditText = getView().findViewById(R.id.song_link_input);
        songImageView = getView().findViewById(R.id.song_image_view);
        openGalleryButton = getView().findViewById(R.id.gallery);
        openCameraButton = getView().findViewById(R.id.camera);
        astronaut = getView().findViewById(R.id.astronaut);
        spaceship = getView().findViewById(R.id.spaceship);
        spaceship.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.shake));
        saveLayout = getView().findViewById(R.id.save_layout);
        clearBtn = getView().findViewById(R.id.clear);
        saveBtn = getView().findViewById(R.id.save);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "song_pic"+new Random().nextInt(100000)+".jpg");
                imageUri = FileProvider.getUriForFile(getContext(), "ehud.marchi.astromusic.provider", file);
                //Toast.makeText(getContext(), imageUri.toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });
        openGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        pickImageFromGallery();
                    }
                } else {
                    pickImageFromGallery();
                }
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songNameEditText.setText("");
                songArtistEditText.setText("");
                songLinkEditText.setText("");
                imageUri = Uri.parse("");
                Glide.with(getContext()).load(imageUri).centerCrop().into(songImageView);
                saveLayout.setVisibility(View.GONE);
                astronaut.setVisibility(View.VISIBLE);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidSong(songNameEditText.getText().toString(), songArtistEditText.getText().toString(), songLinkEditText.getText().toString())) {
                    try {
                        MusicPlayerService.songs.add(new Song(songNameEditText.getText().toString(), songArtistEditText.getText().toString(), imageUri.toString(), songLinkEditText.getText().toString()));
                        saveData();
                        Toast.makeText(getContext(), "Song Uploaded Successfully", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    saveLayout.setVisibility(View.GONE);
                    astronaut.setVisibility(View.VISIBLE);
                    songNameEditText.setText("");
                    songArtistEditText.setText("");
                    songLinkEditText.setText("");
                    imageUri = Uri.parse("");
                    Glide.with(getContext()).load(imageUri).centerCrop().into(songImageView);
                } else {
                    Toast.makeText(getContext(), "Must fill song name, artist and link!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            } else openCameraButton.setVisibility(View.VISIBLE);
        }
        else openCameraButton.setVisibility(View.VISIBLE);
    }

    private boolean isValidSong(String songName, String songArtist, String songLink) {
        boolean isValid = false;
        if ((!songName.equals("")) && (!songArtist.equals("")) && (!songLink.equals("")))
        {
            isValid = true;
        }

        return isValid;
    }

    private void saveData() {
        try {
            FileOutputStream fos = getActivity().openFileOutput("songList.dat", MODE_PRIVATE);
            ObjectOutputStream oow = new ObjectOutputStream(fos);
            oow.writeObject(MusicPlayerService.songs);
            oow.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload_music, container, false);
    }
    private void pickImageFromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Glide
                    .with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.galaxy)
                    .centerCrop()
                    .into(songImageView);
            saveLayout.setVisibility(View.VISIBLE);
            astronaut.setVisibility(View.GONE);
        }
        else if ( requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK){
            imageUri = data.getData();
            Glide
                    .with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(songImageView);
            saveLayout.setVisibility(View.VISIBLE);
            astronaut.setVisibility(View.GONE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_PERMISSION_REQUEST){
            if(grantResults[0]!= PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getContext(),"can't take pic", Toast.LENGTH_SHORT).show();
            }
            else {
                openCameraButton.setVisibility(View.VISIBLE);
            }
        }
        else {
            switch (requestCode){
                case PERMISSION_CODE:{
                    if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        pickImageFromGallery();
                    }
                    else {
                        Toast.makeText(getContext(), "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}