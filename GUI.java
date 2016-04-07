import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;


/**
 *
 * @author allenmuy
 */

public class GUI extends JFrame {
    private int windowType;
    private static int MAIN = 0;
    private JFrame frame;
    private JScrollPane scroll;
    private MusicTable musicTable;
    private JPopupMenu popup;
    private MusicTablePopupListener musicTablePopupListener = new MusicTablePopupListener();
    private MusicPlayer player;


    public GUI() {
        this.windowType = GUI.MAIN;
        this.musicTable = new MusicTable();
        player = new MusicPlayer();
        buildWindowLayout("HoodTunes");
    }

    public void display() {
        frame.setVisible(true);
    }
    
    private void playSong(int row) {
        int songId = Integer.parseInt(musicTable.getTable().getValueAt(row, MusicTable.COL_ID).toString());
        player.setSongRow(row);
        musicTable.getTable().setRowSelectionInterval(row, row);
        player.play(Tester.db.getSongFilePath(songId));
    }
    
    private JPanel getControlPanel() {
        JPanel controlPanel = new JPanel();
        JButton playButton = new JButton("Play");
        JButton pauseButton = new JButton("Pause");
        //JButton resumeButton = new JButton("Resume");
        JButton stopButton = new JButton("Stop");
        JButton previousButton = new JButton("<<");
        JButton nextButton = new JButton(">>");
    
        playButton.addActionListener(new PlayListener());
        pauseButton.addActionListener(new PauseListener());
        //resumeButton.addActionListener(new ResumeListener());
        stopButton.addActionListener(new StopListener());
        previousButton.addActionListener(new PreviousListener());
        nextButton.addActionListener(new NextListener());
        
        controlPanel.add(previousButton);
        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        //controlPanel.add(resumeButton);
        controlPanel.add(stopButton);
        controlPanel.add(nextButton);
        controlPanel.setMaximumSize(new Dimension(1080, 40));
        return controlPanel;
    }
    
    private class PlayListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = musicTable.getTable().getSelectedRow();
            if (selectedRow == -1) {
                selectedRow = 0;
            }
             
            playSong(selectedRow); 
        }
    }
    
    private class PauseListener implements ActionListener {  
        public void actionPerformed(ActionEvent e) {
            player.pause();
        }
    }
    
    /*private class ResumeListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            player.resume();
        }
    }*/


    
    private class StopListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            player.stop();
        }
    }
    
    private class PreviousListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int previousSongRow = player.getSongRow() - 1;
            player.stop();
            playSong(previousSongRow);
        }
    }

    private class NextListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int nextSongIndex = player.getSongRow() + 1;
            int lastItemInTable = musicTable.getTable().getRowCount() - 1;

            if (nextSongIndex <= lastItemInTable) {   
                playSong(nextSongIndex);
            }
        }
    }
    
    
    private void createMusicTablePopupMenu() {
        popup = new JPopupMenu();
        JMenuItem addMenuItem = new JMenuItem("Add Song");
        JMenuItem deleteMenuItem = new JMenuItem("Delete Song");

        addMenuItem.addActionListener(new AddSongListener());
        deleteMenuItem.addActionListener(new DeleteSongListener());

        popup.add(addMenuItem);
        popup.add(deleteMenuItem);
    }
    
    private class MusicTablePopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void buildMusicTable() {
        musicTable.getTable().setPreferredScrollableViewportSize(new Dimension(800, 200));
        musicTable.getTable().setFillsViewportHeight(true);
        //Color ivory = new Color(255, 255, 208);
        //musicTable.getTable().setBackground(Color.orange);
        
        createMusicTablePopupMenu();
        musicTable.getTable().addMouseListener(musicTablePopupListener);

        musicTable.getTable().addMouseListener(new DoubleClickListener());

        musicTable.getTable().setDropTarget(new AddToTableDropTarget());
    }

    private void buildWindowLayout(String title) {
        frame = new JFrame();
        frame.setTitle(title);
        frame.setMinimumSize(new Dimension(800, 500));
        frame.setLocationRelativeTo(null);

        if(windowType == GUI.MAIN) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } 
  
        // Create the main panel that resides within the windowFrame
        // Layout: BoxLayout, X_AXIS
        JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPanel.setDividerLocation(100);

        // Instantiate scroll pane for table
        scroll = new JScrollPane(musicTable.getTable());

        // Create the controlTablePanel that will reside within the mainPanel
        // Layout: BoxLayout, Y_AXIS
        JPanel controlTablePanel = new JPanel();
        controlTablePanel.setLayout(new BoxLayout(controlTablePanel, BoxLayout.Y_AXIS));
        controlTablePanel.add(getControlPanel());
        controlTablePanel.add(scroll);
        controlTablePanel.setMinimumSize(new Dimension(500, 200)); 
        
        // Create menuBar and add File/Control menus
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getFileMenu());

        // Build the music table
        buildMusicTable();


        // Build main panel
  
        mainPanel.add(controlTablePanel);

        // Add all GUI components
        frame.setJMenuBar(menuBar);
        frame.setContentPane(mainPanel);
        frame.getContentPane().setBackground(Color.DARK_GRAY);
        frame.pack();
        frame.setLocationByPlatform(true);
    }
    
    private JMenu getFileMenu() {
        JMenu menu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem addItem = new JMenuItem("Add Song");
        JMenuItem deleteItem = new JMenuItem("Delete Song");
        JMenuItem exitItem = new JMenuItem("Exit");

        addItem.addActionListener(new AddSongListener());
        deleteItem.addActionListener(new DeleteSongListener());
        openItem.addActionListener(new OpenItemListener());
        exitItem.addActionListener(new ExitItemListener());

        menu.add(openItem);
        menu.add(addItem);
        menu.add(deleteItem);
        menu.add(exitItem);
        return menu;
    }
    
    private class OpenItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
                chooser.setFileFilter(filter); 

                if (chooser.showDialog(frame, "Open Song") == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    Song selectedSong = new Song(selectedFile.getPath());

                    player.play(selectedSong.getFilePath());
                }
            }
    }
    
    private class AddSongListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(filter);  
       
            if (chooser.showDialog(frame, "Add Song") == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Song selectedSong = new Song(selectedFile.getPath());
                int id = Tester.db.insertSong(selectedSong);  // -1 if failure

                if(musicTable.getType() == MusicTable.LIBRARY) {
                    if (id != -1) {
                        musicTable.addSongToTable(id, selectedSong);
                    }
                } 
            }
        }
    }
    
    private class DeleteSongListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            int[] selectedRows = musicTable.getTable().getSelectedRows();

            DefaultTableModel model = (DefaultTableModel) musicTable.getTable().getModel();

            for(int i = 0; i < selectedRows.length; i++) {
                int selectedSongRow = selectedRows[i];
                int selectedSongId = Integer.parseInt(musicTable.getTable().getValueAt(selectedSongRow, MusicTable.COL_ID).toString());
                if (selectedSongRow == player.getSongRow()) {
                    player.stop();
                }
                model.removeRow(selectedSongRow);
                if (musicTable.getType() == MusicTable.LIBRARY) {
                    Tester.db.deleteSong(selectedSongId);
                }
            }
        }
    }
    
    private class ExitItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if(windowType == GUI.MAIN) {
                Tester.db.close();
                System.exit(0);
            }
        }
    }

    private class DoubleClickListener extends MouseAdapter{
        public void mousePressed(MouseEvent me) {
            if (me.getClickCount() == 2) {
                int row = musicTable.getTable().getSelectedRow();
                playSong(row);
            }
        }
    }
    
    private class AddToTableDropTarget extends DropTarget {
        @Override
        public synchronized void drop(DropTargetDropEvent dtde) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable t = dtde.getTransferable();
            java.util.List fileList;
            try {
                fileList = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
                for (Object file : fileList) {
                    Song song = new Song(file.toString());

                    if (musicTable.getType() == MusicTable.LIBRARY) {
                        int id = Tester.db.insertSong(song);
                        if (id != -1) {
                            musicTable.addSongToTable(id, song);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }    
}
