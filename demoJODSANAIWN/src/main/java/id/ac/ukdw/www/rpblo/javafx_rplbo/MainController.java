package id.ac.ukdw.www.rpblo.javafx_rplbo;

import id.ac.ukdw.www.rpblo.javafx_rplbo.Manager.SqliteDB;
import id.ac.ukdw.www.rpblo.javafx_rplbo.Manager.Sessionmanager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableRow;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainController {

    @FXML public TextField searchKataKunci;
    @FXML private TableColumn<ToDo, String> aksi;
    @FXML private Button btnKeluar;
    @FXML private TableColumn<ToDo, String> deadline;
    @FXML private TableColumn<ToDo, String> kategori;
    @FXML private TableColumn<ToDo, String> todolist;
    @FXML private TableColumn<ToDo, Void> noColumn;
    @FXML private TableColumn<ToDo, Boolean> checkboxColumn;
    @FXML private TableView<ToDo> TableView;
    @FXML private ComboBox<String> comboKategori;
    @FXML private TextField searchKategori;
    private boolean sudahTampilkanNotifikasi = false;


    private static final List<ToDo> toDoList = new ArrayList<>();

    public static List<ToDo> getToDoList() {
        return toDoList;
    }

    @FXML
    public void initialize() {
        todolist.setCellValueFactory(new PropertyValueFactory<>("judul"));
        deadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        kategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        TableView.setEditable(true);
        checkboxColumn.setEditable(true);

        loadToDoFromDatabase();
        cekDeadlineBesok();

        aksi.setCellFactory(col -> new TableCell<ToDo, String>() {
            private final Button editBtn = new Button("Edit");
            private final Button hapusBtn = new Button("Hapus");
            private final javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5, editBtn, hapusBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                hapusBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                hapusBtn.setOnAction(event -> {
                    ToDo todo = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Konfirmasi Hapus");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Apakah kamu yakin ingin menghapus to-do ini?");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try (Connection conn = SqliteDB.getInstance().getConnection()) {
                                String sql = "DELETE FROM todo WHERE user_id = ? AND id = ?";
                                PreparedStatement stmt = conn.prepareStatement(sql);
                                stmt.setInt(1, Sessionmanager.getCurrentUserId());
                                stmt.setInt(2, todo.getId());
                                stmt.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            toDoList.remove(todo);
                            applyFilter();
                        }
                    });
                });

                editBtn.setOnAction(event -> {
                    ToDo todo = getTableView().getItems().get(getIndex());
                    Apps.showTambahToDo(todo);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        noColumn.setCellFactory(col -> new TableCell<ToDo, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getTableRow().getIndex() + 1));
            }
        });

        checkboxColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        checkboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkboxColumn));

        TableView.setRowFactory(tv -> new TableRow<ToDo>() {
            @Override
            protected void updateItem(ToDo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    updateRowStyle(item);
                    item.selectedProperty().addListener((obs, oldVal, newVal) -> updateRowStyle(item));
                }
            }

            private void updateRowStyle(ToDo item) {
                boolean isChecked = item.isSelected();
                boolean isPrioritas = item.isPrioritas();

                if (isChecked) {
                    setStyle("-fx-background-color: #d4fcd4;");
                } else if (isPrioritas) {
                    setStyle("-fx-background-color: #fff3b0;");
                } else {
                    setStyle("");
                }
            }
        });

        List<String> semuaKategori = new ArrayList<>();
        semuaKategori.add("Semua");
        semuaKategori.addAll(KategoriController.getKategoriComboBoxItems());
        comboKategori.getItems().setAll(semuaKategori);
        comboKategori.setValue("Semua");

        searchKategori.textProperty().addListener((obs, oldVal, newVal) -> filterByKategoriTextField(newVal));
        searchKataKunci.textProperty().addListener((obs, oldVal, newVal) -> filterToDoByAllFields(newVal));
    }

    void loadToDoFromDatabase() {
        try (Connection conn = SqliteDB.getInstance().getConnection()) {
            int userId = Sessionmanager.getCurrentUserId();
            List<ToDo> semuaToDo = new ArrayList<>();
            String sql = "SELECT * FROM todo WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String judul = rs.getString("judul");
                String deskripsi = rs.getString("deskripsi");
                String deadline = rs.getString("deadline");
                String kategori = rs.getString("kategori");
                boolean prioritas = rs.getInt("prioritas") == 1;

                semuaToDo.add(new ToDo(id, judul, deskripsi, deadline, kategori, prioritas));
            }

            rs.close();
            stmt.close();

            toDoList.clear();
            toDoList.addAll(semuaToDo);
            sortToDoList();
            TableView.getItems().setAll(toDoList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sortToDoList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        toDoList.sort((a, b) -> {
            // Prioritas dulu
            if (a.isPrioritas() != b.isPrioritas()) {
                return Boolean.compare(!a.isPrioritas(), !b.isPrioritas());
            }

            // Lalu berdasarkan deadline
            try {
                LocalDate dateA = LocalDate.parse(a.getDeadline(), formatter);
                LocalDate dateB = LocalDate.parse(b.getDeadline(), formatter);
                return dateA.compareTo(dateB);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void applyFilter() {
        String kategoriDipilih = comboKategori.getValue();
        if (kategoriDipilih == null || kategoriDipilih.equals("Semua")) {
            sortToDoList();
            TableView.getItems().setAll(toDoList);
        } else {
            List<ToDo> hasilFilter = new ArrayList<>();
            for (ToDo todo : toDoList) {
                if (todo.getKategori().equalsIgnoreCase(kategoriDipilih)) {
                    hasilFilter.add(todo);
                }
            }
            hasilFilter.sort(Comparator.comparing(ToDo::isPrioritas).reversed()
                    .thenComparing(todo -> {
                        try {
                            return LocalDate.parse(todo.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        } catch (Exception e) {
                            return LocalDate.MAX;
                        }
                    }));
            TableView.getItems().setAll(hasilFilter);
        }
    }

    private void filterByKategoriTextField(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            TableView.getItems().setAll(toDoList);
        } else {
            List<ToDo> hasilFilter = new ArrayList<>();
            for (ToDo todo : toDoList) {
                String kategori = todo.getKategori();
                if (kategori != null && kategori.toLowerCase().contains(keyword.toLowerCase())) {
                    hasilFilter.add(todo);
                }
            }
            TableView.getItems().setAll(hasilFilter);
        }
    }

    private void filterToDoByAllFields(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            TableView.getItems().setAll(toDoList);
            return;
        }

        String search = keyword.toLowerCase().trim();
        List<ToDo> hasilFilter = new ArrayList<>();

        for (ToDo todo : toDoList) {
            String judul = todo.getJudul() != null ? todo.getJudul().toLowerCase() : "";
            String deadline = todo.getDeadline() != null ? todo.getDeadline().toLowerCase() : "";
            String deskripsi = todo.getDeskripsi() != null ? todo.getDeskripsi().toLowerCase() : "";

            if (judul.contains(search) || deadline.contains(search) || deskripsi.contains(search)) {
                hasilFilter.add(todo);
            }
        }

        TableView.getItems().setAll(hasilFilter);
    }

    private void cekDeadlineBesok() {
        LocalDate besok = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (ToDo todo : toDoList) {
            try {
                LocalDate deadline = LocalDate.parse(todo.getDeadline(), formatter);
                if (deadline.equals(besok)) {
                    tampilkanNotifikasiModal(todo.getJudul());
                }
            } catch (Exception e) {
                System.out.println("❌ Gagal parsing tanggal: " + todo.getDeadline());
            }
        }
    }

    private void tampilkanNotifikasiModal(String judulTugas) {
        Platform.runLater(() -> {
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Modal tapi tidak blocking Main Stage
            popupStage.setTitle("📌 Pengingat Deadline");

            Label pesan = new Label(judulTugas + ", tenggatnya besok.");
            pesan.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

            Button btnTutup = new Button("Tutup");
            btnTutup.setOnAction(e -> popupStage.close());
            btnTutup.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");

            VBox layout = new VBox(15, pesan, btnTutup);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));
            layout.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8;");

            Scene scene = new Scene(layout, 300, 150);
            popupStage.setScene(scene);
            popupStage.setResizable(false);
            popupStage.initOwner(TableView.getScene().getWindow()); // Supaya modal ada di atas main window
            popupStage.show();
        });
    }





    @FXML
    void SearchByCategory(ActionEvent event) {
        applyFilter();
    }

    @FXML
    void addCategory(ActionEvent event) {
        Apps.showKategori();
        // setelah dialog ditutup, update isi combo box
        List<String> semuaKategori = new ArrayList<>();
        semuaKategori.add("Semua");
        semuaKategori.addAll(KategoriController.getKategoriComboBoxItems());
        comboKategori.getItems().setAll(semuaKategori);
        comboKategori.setValue("Semua");
    }



    @FXML
    void modul_tambah_todolist(ActionEvent event) {
        Apps.showTambahToDo(null);
    }

    @FXML
    void Keluar() {
        Sessionmanager.clearSession();
        Apps.showLogin();
    }
}
