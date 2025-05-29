package id.ac.ukdw.www.rpblo.javafx_rplbo.Manager;

import java.io.*;

public class Sessionmanager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SESSION_FILE = "session.dat";

    private static String currentUsername;
    private static int currentUserId;

    // Panggil saat login berhasil
    public static void setCurrentUser(String username, int userId) {
        currentUsername = username;
        currentUserId = userId;
        saveSession();
    }

    public static String getCurrentUser() {
        return currentUsername;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void clearSession() {
        currentUsername = null;
        currentUserId = 0;
        File f = new File(SESSION_FILE);
        if (f.exists()) f.delete();
    }

    public static void saveSession() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SESSION_FILE))) {
            oos.writeObject(new SessionmanagerData(currentUsername, currentUserId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadSession() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            SessionmanagerData data = (SessionmanagerData) ois.readObject();
            currentUsername = data.getUsername();
            currentUserId = data.getUserId();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Kelas untuk menyimpan data session
    private static class SessionmanagerData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String username;
        private final int userId;

        public SessionmanagerData(String username, int userId) {
            this.username = username;
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public int getUserId() {
            return userId;
        }
    }
}
