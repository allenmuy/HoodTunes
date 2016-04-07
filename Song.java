import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import java.io.IOException;

public class Song {

    private String filePath;
    private String artist;
    private String title;
    private String album;
    private String year;
    private String genre;
    private String comment;

    public Song(String filePath) {
        this.filePath = filePath;
        try {
            Mp3File mp3file = new Mp3File(filePath);
            if (mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                this.artist = id3v1Tag.getArtist();
                this.title = id3v1Tag.getTitle();
                this.album = id3v1Tag.getAlbum();
                this.year = id3v1Tag.getYear();
                this.genre = id3v1Tag.getGenreDescription();
                this.comment = id3v1Tag.getComment();
            } else if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                this.artist = id3v2Tag.getArtist();
                this.title = id3v2Tag.getTitle();
                this.album = id3v2Tag.getAlbum();
                this.year = id3v2Tag.getYear();
                this.genre = id3v2Tag.getGenreDescription();
                this.comment = id3v2Tag.getComment();
            }
        } catch (IOException ioe) {
            System.out.println("IOException occurred..");
        } catch (Exception e) {
            System.out.println("An exception occurred...");
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public String getComment() { 
        return comment; 
    }
}