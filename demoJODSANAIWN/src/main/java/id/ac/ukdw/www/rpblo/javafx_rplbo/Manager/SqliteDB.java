package id.ac.ukdw.www.rpblo.javafx_rplbo.Manager;

import java.sql.*;

public class SqliteDB implements DatabaseDriver{
    private static final String URL = "jdbc:Sqlite:D:\\RPLBO_JAWIR\\Project\\JavaFX_rplbo\\user.db";


    private static volatile SqliteDB instance;
    private Connection connection;

    private SqliteDB() {
        try {
            connection = DriverManager.getConnection(URL);
            prepareSchema(); // buat tabel jika belum ada
        } catch (SQLException e) {
            throw new RuntimeException("Gagal konek ke database MariaDB", e);
        }
    }

    public static SqliteDB getInstance() {
        if (instance == null) {
            synchronized (SqliteDB.class) {
                if (instance == null) {
                    instance = new SqliteDB();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mendapatkan koneksi MariaDB", e);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menutup koneksi MariaDB", e);
        }
    }

    public void prepareSchema() {
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS user ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL ,"
                + "password TEXT NOT NULL"
                + ")";
        String createKategoriTableSQL = "CREATE TABLE IF NOT EXISTS kategori (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nama TEXT NOT NULL," +
                "user_id INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES user(id)" +
                ")";

        String createTodoTableSQL = "CREATE TABLE IF NOT EXISTS todo ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "judul TEXT NOT NULL,"
                + "deskripsi TEXT,"
                + "deadline TEXT,"
                + "kategori TEXT,"
                + "prioritas INTEGER,"
                + "user_id INTEGER NOT NULL,"
                + "FOREIGN KEY (user_id) REFERENCES user(id)"
                + ");";


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUserTableSQL);
            stmt.execute(createKategoriTableSQL);
            stmt.execute(createTodoTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
