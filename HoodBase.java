import java.sql.*;
import java.util.*;
/**
 *
 * @author allenmuy
 */
public class HoodBase {
    private static final String DB_NAME = "HoodBase";
    private static final String SONG_TABLE = "SONG";
    private static final String[] SONG_COLUMNS = {"songId", "filePath", "title", "artist", "album", "yearReleased", "comment", "genre"};
    private static final String COLUMN_TABLE = "COLUMN_CONFIG";
    private static final String CREATE = ";create=true";
    private static final String PROTOCOL = "jdbc:derby:";
    private Connection conn;
    private PreparedStatement stmt;
    private boolean connected;

    public HoodBase() {
        connect();     
        createTables(); 
    }

    private void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = DriverManager.getConnection(PROTOCOL + DB_NAME);
            connected = true;
            System.out.println("HoodBase Connected...");
        } catch (Exception except) {
            createDatabase();
        }
    }
    
    public void close() {
        try {
            DriverManager.getConnection("jdbc:derby:" + DB_NAME + ";shutdown=true");
        } catch (Exception except){
            except.printStackTrace();
        }
    }

    private boolean createDatabase() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = DriverManager.getConnection(PROTOCOL + DB_NAME + CREATE);
            connected = true;
            System.out.println("HoodBase Created...");
            return true;
        } catch (Exception except) {
            except.printStackTrace();
            return false;
        }
    } 
    
    private boolean createSongTable() {
        try {
            String query = "CREATE TABLE " + SONG_TABLE +
                    " (songId INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "filePath VARCHAR(200) UNIQUE NOT NULL, " +
                    "title VARCHAR(150), " +
                    "artist VARCHAR(100), " +
                    "album VARCHAR(150), " +
                    "yearReleased VARCHAR(4), " +
                    "genre VARCHAR(20), " +
                    "comment VARCHAR(200), " +
                    "PRIMARY KEY (songId))";
            stmt = conn.prepareStatement(query);
            stmt.execute();
            stmt.close();
            System.out.println("Song Table Created...");
            return true;
        }
        catch (SQLException sqlExcept) {

        }
        return false;
    }
    
    private boolean createColumns() {
        String query;
        try {
            query = "CREATE TABLE " + COLUMN_TABLE +
                    " (columnName VARCHAR(50)," +
                    "columnVisible BOOLEAN NOT NULL)";
            stmt = conn.prepareStatement(query);
            stmt.execute();
            stmt.close();

            for (int i = 0; i < MusicTable.SONG_COLUMN_NAMES.length; i++) {
                query = "INSERT INTO " + COLUMN_TABLE +
                        " (columnName, columnVisible)" +
                        " VALUES (?, ?)";
                stmt = conn.prepareStatement(query);
                String columnName = MusicTable.SONG_COLUMN_NAMES[i];
                stmt.setString(1, columnName);
                if (columnName.equals("ID") || columnName.equals("File Path")) {
                    stmt.setBoolean(2, false);  
                } else {
                    stmt.setBoolean(2, true); 
                }
                stmt.execute();
                stmt.close();
            }
            return true;
        } catch (SQLException sqlExcept) {

        }
        return false;
    }

    public boolean getColumnVisible(String columnName) {
        boolean columnVisible = false;
        try {
            String query = "SELECT columnVisible FROM " + COLUMN_TABLE +
                    " WHERE columnName=?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, columnName);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            columnVisible = resultSet.getBoolean("columnVisible");
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return columnVisible;
    }

    public void setColumnVisible(String columnName, boolean visible) {
        try {
            String query = "UPDATE " + COLUMN_TABLE +
                    " SET columnVisible=? " +
                    " WHERE columnName=?";
            stmt = conn.prepareStatement(query);
            stmt.setBoolean(1, visible);
            stmt.setString(2, columnName);
            stmt.execute();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    private void createTables() {
        createSongTable();
        createColumns();
    }
    
    public boolean songExists(String filePath) {
        int rowCount = 0;
        try {
            String query = "SELECT count(*) AS rowcount FROM " + SONG_TABLE +
                    " WHERE filePath=?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, filePath);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            rowCount = resultSet.getInt("rowcount");
            stmt.close();
            if (rowCount != 0) {
                return true;
            }
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            return false;
        }
        return false;
    }
    
    public int insertSong(Song song) {
        int id = -1;
        ResultSet keys = null;
        if (!songExists(song.getFilePath())) {
            try {
                String query = "INSERT INTO " + SONG_TABLE +
                        " (filePath, title, artist, album, yearReleased, genre, comment)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, song.getFilePath());
                stmt.setString(2, song.getTitle());
                stmt.setString(3, song.getArtist());
                stmt.setString(4, song.getAlbum());
                stmt.setString(5, song.getYear());
                stmt.setString(6, song.getGenre());
                stmt.setString(7, song.getComment());
                stmt.execute();
                keys = stmt.getGeneratedKeys();
                while (keys.next()) {
                    id = keys.getInt(1);
                }
                keys.close();
                stmt.close();
                System.out.println(song.getTitle() + " inserted...");
            } catch (SQLException sqlExcept) {
                sqlExcept.printStackTrace();
            }
        }
        return id;
    }
    
    public boolean deleteSong(int songId) {
        try {
            String query = "DELETE FROM " + SONG_TABLE + " WHERE songId=?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, songId);
            stmt.execute();
            stmt.close();
            return true;
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return false;
    }

    public Object[][] getAllSongs() {
        Object[][] allSongs;
        int rowCount = 0;
        int index = 0;
        try {
            String rowCountQuery = "SELECT count(*) AS rowcount FROM " + SONG_TABLE;
            stmt = conn.prepareStatement(rowCountQuery);
            ResultSet rowCountRS = stmt.executeQuery();
            rowCountRS.next();
            rowCount = rowCountRS.getInt("rowcount");
            allSongs = new Object[rowCount][SONG_COLUMNS.length];

            String allSongsQuery = "SELECT * FROM " + SONG_TABLE + " ORDER BY title";
            stmt = conn.prepareStatement(allSongsQuery);
            ResultSet allSongsRS = stmt.executeQuery();

            while (allSongsRS.next()) {
                allSongs[index] = getSongRow(allSongsRS);
                index++;
            }
            stmt.close();
            return allSongs;
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return new Object[0][0];
    }
    
    private String[] getSongRow(ResultSet rs) {
        String[] song = new String[SONG_COLUMNS.length];
        try {
            for (int i = 0; i < SONG_COLUMNS.length; i++) {
                song[i] = rs.getString(SONG_COLUMNS[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return song;
    }

    public int getSongId(String filePath) {
        int songId = -1;
        try {
            String query = "SELECT * FROM " + SONG_TABLE + " WHERE filePath=?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, filePath);
            ResultSet songIdRS = stmt.executeQuery();
            if (songIdRS.next()) {
                songId = songIdRS.getInt("songId");
            }
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return songId;
    }

    public String getSongFilePath(int songId) {
        String songFilePath = null;
        try {
            String query = "SELECT * FROM " + SONG_TABLE + " WHERE songId=?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, songId);
            ResultSet songIdRS = stmt.executeQuery();
            if (songIdRS.next()) {
                songFilePath = songIdRS.getString("filePath");
            }
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return songFilePath;
    }

    public String getSongTitle(int songId) {
        String title = null;
        try {
            String query = "SELECT title FROM " + SONG_TABLE + " WHERE songId=?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, songId);
            ResultSet songIdRS = stmt.executeQuery();
            if (songIdRS.next()) {
                title = songIdRS.getString("title");
            }
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return title;
    }
}