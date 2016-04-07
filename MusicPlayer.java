import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import java.io.File;

public class MusicPlayer {

    private int songRow;
    private BasicPlayer player;
    private BasicController controller;
    private boolean paused;

    public MusicPlayer() {
        player = new BasicPlayer();
        controller = (BasicController) player;
    }
    
    public BasicPlayer getPlayer() {
        return player;
    }
    
    public boolean play(String filePath) {
        try {
            controller.open(new File(filePath));
            controller.play();
            paused = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean pause() {
        try {
            if (paused) {
                controller.resume();
                paused = false;
            }
            else{
                 controller.pause();
                paused = true;
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    
    
    public boolean stop() {
        try {
            controller.stop();
            paused = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void setSongRow(int row) {
        this.songRow = row;
    }

    public int getSongRow() {
        return songRow;
    }
}
