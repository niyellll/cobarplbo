package id.ac.ukdw.www.rpblo.javafx_rplbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import id.ac.ukdw.www.rpblo.javafx_rplbo.Manager.SqliteDB;
import id.ac.ukdw.www.rpblo.javafx_rplbo.Manager.Sessionmanager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;


public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void onLogin() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        try (Connection connection = SqliteDB.getInstance().getConnection()) {
            String query = "SELECT * FROM user WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Login berhasil
                int userId = resultSet.getInt("id");
                String usernameFromDB = resultSet.getString("username");

                // Set session user
                Sessionmanager.setCurrentUser(usernameFromDB, userId);

                Apps.setLoggedInUsername(usernameFromDB);

                // Notifikasi login berhasil
                javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                successAlert.setTitle("Login Berhasil");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Selamat datang, " + username + "!");
                successAlert.showAndWait();

                // Ganti scene ke halaman utama
                Apps.showMain();
            } else {
                // Login gagal - tampilkan alert
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Login Gagal");
                alert.setHeaderText(null);
                alert.setContentText("Username atau password salah.");
                alert.showAndWait();
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void goToRegister() {
        Apps.showRegister();
    }


}
