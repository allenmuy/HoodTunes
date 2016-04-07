import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class MusicTable {

    public static int LIBRARY = 0;

    private JTable table;
    private String name; 
    private int type;
    public static final String[] SONG_COLUMN_NAMES =  {"ID", "File Path", "Title", "Artist", "Album", "Year", "Genre", "Comment"};
    public static final int COL_ID = 0;
    public static final int COL_FILE_PATH = 1;

    public MusicTable(){
        table = new JTable();
        buildTable(Tester.db.getAllSongs());
        name = "Library";
        type = LIBRARY;
    }
    
    public JTable getTable() {
        return table;
    }
    
    public int getType() {
        return type;
    }

    private void buildTable(Object[][] songs) {
        DefaultTableModel tableModel = new DefaultTableModel(songs, SONG_COLUMN_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setModel(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        setColumnVisibility();
    }

    public void setColumnVisibility() {
        for (int i = 0; i < SONG_COLUMN_NAMES.length; i++) {
            String columnName = SONG_COLUMN_NAMES[i];
            if (Tester.db.getColumnVisible(columnName)) {show(columnName);}
            else {hide(columnName);}
        }
    }

    public void hide(String columnName) {
        int index = table.getColumnModel().getColumnIndex(columnName);

        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setWidth(0);

        Tester.db.setColumnVisible(columnName, false);
    }

    public void show(String columnName) {
        int index = table.getColumnModel().getColumnIndex(columnName);

        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(10);
        column.setMaxWidth(500);
        column.setWidth(10);
        column.setPreferredWidth(80);

        Tester.db.setColumnVisible(columnName, true);
    }

    public void updateTableModel(String name) {
        this.name = name;
        if (name.equals("Library")) {
            buildTable(Tester.db.getAllSongs());
            type = LIBRARY;
        } 
    }


    public void addSongToTable(int id, Song song) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[]{String.valueOf(id), 
            song.getFilePath(), song.getTitle(), song.getArtist(), song.getAlbum(), 
            song.getYear(), song.getGenre(), song.getComment()});
        updateTableModel(name);
    }
}