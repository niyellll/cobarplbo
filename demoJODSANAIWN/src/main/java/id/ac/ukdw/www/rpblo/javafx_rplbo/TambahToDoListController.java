package id.ac.ukdw.www.rpblo.javafx_rplbo;

import id.ac.ukdw.www.rpblo.javafx_rplbo.Manager.SqliteDB;
import id.ac.ukdw.www.rpblo.javafx_rplbo.Manager.Sessionmanager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TambahToDoListController implements Initializable {

    @FXML
    private DatePicker deadline;

    @FXML
    private TextArea deskripsi;

    @FXML
    private TextArea judul;

    @FXML
    private ComboBox<String> kategoriComboBox;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private CheckBox prioritas;
    private ToDo currentToDo;  // Mendeklarasikan currentToDo

    // Setter untuk menginisialisasi currentToDo
    public void setCurrentToDo(ToDo toDo) {
        this.currentToDo = toDo;
        if (judul != null) {
            prefillForm();
        }
    }

    // Getter untuk mengambil currentToDo (jika diperlukan)
    public ToDo getCurrentToDo() {
        return currentToDo;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        kategoriComboBox.setItems(KategoriController.getKategoriComboBoxItems());

        //  Batasi agar hanya bisa pilih hari ini atau ke depan
        deadline.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;"); // Warna penanda
                }
            }
        });
    }

    @FXML
    void submit(ActionEvent event) {
        String taskJudul = judul.getText();
        String taskDeskripsi = deskripsi.getText();
        String taskDeadline = deadline.getValue() != null ? deadline.getValue().toString() : "";
        String taskKategori = kategoriComboBox.getValue();
        boolean isPrioritas = prioritas.isSelected();

        try (Connection conn = SqliteDB.getInstance().getConnection()) {
            if (currentToDo != null) {
                // Update database
                String sqlUpdate = "UPDATE todo SET judul = ?, deskripsi = ?, deadline = ?, kategori = ?, prioritas = ? WHERE id = ? AND user_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sqlUpdate);
                stmt.setString(1, taskJudul);
                stmt.setString(2, taskDeskripsi);
                stmt.setString(3, taskDeadline);
                stmt.setString(4, taskKategori);
                stmt.setInt(5, isPrioritas ? 1 : 0);
                stmt.setInt(6, currentToDo.getId());
                stmt.setInt(7, Sessionmanager.getCurrentUserId());

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("✔ ToDo berhasil diupdate di database.");
                } else {
                    System.out.println("⚠ Tidak ada data yang diupdate.");
                }
            } else {
                // Insert baru ke DB
                String sqlInsert = "INSERT INTO todo (judul, deskripsi, deadline, kategori, prioritas, user_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sqlInsert);
                stmt.setString(1, taskJudul);
                stmt.setString(2, taskDeskripsi);
                stmt.setString(3, taskDeadline);
                stmt.setString(4, taskKategori);
                stmt.setInt(5, isPrioritas ? 1 : 0);
                stmt.setInt(6, Sessionmanager.getCurrentUserId());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Gagal menyimpan atau mengupdate to-do ke database.");
        }

        // Tutup form setelah submit
        stage.close();
    }



    @FXML
    void goback(ActionEvent event) {
        Stage currentStage = (Stage) judul.getScene().getWindow();
        currentStage.close();
        Apps.showMain();
    }

    @FXML
    void list_kategori(ActionEvent event) {
        String kategoriDipilih = kategoriComboBox.getValue();
        if (kategoriDipilih != null) {
            System.out.println("Kategori dipilih: " + kategoriDipilih);
        }
    }

    public void prefillForm() {
        if (currentToDo != null) {
            judul.setText(currentToDo.getJudul());
            deskripsi.setText(currentToDo.getDeskripsi());
            if (currentToDo.getDeadline() != null && !currentToDo.getDeadline().isEmpty()) {
                deadline.setValue(LocalDate.parse(currentToDo.getDeadline())); // pastikan format benar
            }
            kategoriComboBox.setValue(currentToDo.getKategori());
            prioritas.setSelected(currentToDo.isPrioritas());
        }
    }
}
