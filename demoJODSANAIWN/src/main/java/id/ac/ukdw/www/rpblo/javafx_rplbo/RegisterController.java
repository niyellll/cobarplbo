package id.ac.ukdw.www.rpblo.javafx_rplbo;

import id.ac.ukdw.www.rpblo.javafx_rplbo.Apps;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // Validasi username
        if (user.length() < 8) {
            showAlert(Alert.AlertType.WARNING, "Username Tidak Valid", "Username harus memiliki minimal 8 karakter.");
            return;
        }

        // Validasi password
        if (!isValidPassword(pass)) {
            showAlert(Alert.AlertType.WARNING, "Password Tidak Valid",
                    "Password harus memiliki minimal 8 karakter dan mengandung huruf kapital, huruf kecil, angka, dan spesial karakter.");
            return;
        }

        if (!pass.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING, "Password Tidak Cocok", "Password dan Konfirmasi Password harus sama.");
            return;
        }

        // Proses penyimpanan ke database
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:D:\\RPLBO_JAWIR\\Project\\JavaFX_rplbo\\user.db");
            String sql = "INSERT INTO user (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user);
            stmt.setString(2, pass); // Disarankan hashing

            stmt.executeUpdate();
            stmt.close();
            conn.close();

            showAlert(Alert.AlertType.INFORMATION, "Registrasi Berhasil", "Akun berhasil dibuat untuk: " + user);
            Apps.showLogin();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Registrasi Gagal", "Gagal registrasi: " + e.getMessage());
        }
    }

    // Fungsi untuk validasi password
    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#$%^&*()_+-=[]{}|;:'\",.<>?/`~".contains(String.valueOf(c))) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // Fungsi bantu untuk menampilkan alert
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToLogin() {
        Apps.showLogin();
    }
}
