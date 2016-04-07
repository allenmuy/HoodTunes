import java.util.ArrayList;

/**
 *
 * @author allenmuy
 */
public class Tester {
    static GUI gui;
    static HoodBase db;
 
    public static void main(String[] args) {
        db = new HoodBase();
       
        //Song song = new Song("/Users/allenmuy/NetBeansProjects/343Project/src/songs/song.mp3");
        //db.insertSong(song);

        gui = new GUI();

        // Display main window
        gui.display();  
    }
}
