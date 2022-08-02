package ehud.marchi.astromusic;

import java.io.Serializable;

public class Song implements Serializable {
    private String songLink;
    private String imageURL;
    private String songName;
    private String songArtist;

    public Song(String songName, String songArtist,  String imageURL, String songLink) {
        this.songLink = songLink;
        this.imageURL = imageURL;
        this.songName = songName;
        this.songArtist = songArtist;
    }

    public String getSongLink() {
        return songLink;
    }

    public void setSongLink(String songLink) {
        this.songLink = songLink;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }
}
