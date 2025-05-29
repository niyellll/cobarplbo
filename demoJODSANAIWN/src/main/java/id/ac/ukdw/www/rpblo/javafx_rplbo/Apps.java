package id.ac.ukdw.www.rpblo.javafx_rplbo;

import id.ac.ukdw.www.rpblo.javafx_rplbo.Manager.Sessionmanager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;


public class Apps extends Application {
    private static Stage primaryStage;
    public static MainController mainControllerInstance;


    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        Sessionmanager.loadSession();

        if (Sessionmanager.getCurrentUser() != null) {
            showMain();
        } else {
            showLogin();
        }
    }

    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(Apps.class.getResource("/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1100);

            // Pastikan keluar dari fullscreen dan maximize
            primaryStage.setFullScreen(false);
            primaryStage.setMaximized(false);

            // Set ukuran tetap dan tidak bisa diubah
            primaryStage.setScene(scene);
            primaryStage.setTitle("Login");
            primaryStage.setResizable(false);
            primaryStage.setWidth(1920);
            primaryStage.setHeight(1100);
            primaryStage.centerOnScreen();

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void showMain() {
        try {
            FXMLLoader loader = new FXMLLoader(Apps.class.getResource("/views/main.fxml"));
            Parent root = loader.load();

            mainControllerInstance = loader.getController();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("ToDo List - Wolf Edition");

            // Hilangkan hint ESC fullscreen
            primaryStage.setFullScreenExitHint("");

            // Set fullscreen aktif
            primaryStage.setFullScreen(true);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(false);

            // Listener fullscreen untuk handle ESC keluar fullscreen
            primaryStage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
                if (!isFullScreen) {
                    // Saat keluar fullscreen (misal tekan ESC)
                    primaryStage.setMaximized(false);
                    primaryStage.setResizable(true);

                    // Atur ukuran jendela normal
                    primaryStage.setWidth(1280);
                    primaryStage.setHeight(800);
                    primaryStage.centerOnScreen();
                } else {
                    // Saat masuk fullscreen kembali
                    primaryStage.setMaximized(true);
                    primaryStage.setResizable(false);
                }
            });

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(Apps.class.getResource("/views/register.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1100);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Register");
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void showTambahToDo(ToDo todo) {
        try {
            FXMLLoader loader = new FXMLLoader(Apps.class.getResource("/views/TambahToDoList.fxml"));
            Parent root = loader.load();

            TambahToDoListController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.setCurrentToDo(todo);

            stage.setOnHiding(e -> {
                if (mainControllerInstance != null) {
                    mainControllerInstance.loadToDoFromDatabase();
                }
            });

            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Modal agar fokus ke pop-up
            stage.initOwner(primaryStage);
            stage.setTitle("Tambah ToDo");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            stage.setOnHiding(e -> {
                if (mainControllerInstance != null) {
                    mainControllerInstance.loadToDoFromDatabase(); // Atau bisa load kategori combo box
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showKategori() {
        try {
            FXMLLoader loader = new FXMLLoader(Apps.class.getResource("/views/kategori.fxml"));
            Parent root = loader.load();

            Stage kategoriStage = new Stage();
            kategoriStage.setTitle("Manajemen Kategori");
            kategoriStage.setScene(new Scene(root));

            kategoriStage.initModality(Modality.APPLICATION_MODAL); // Pop-up mengunci parent window
            kategoriStage.initOwner(primaryStage); // Set agar benar-benar terkait dengan primary stage
            kategoriStage.setResizable(false);
            kategoriStage.centerOnScreen();

            kategoriStage.setOnHiding(e -> {
                if (mainControllerInstance != null) {
                    mainControllerInstance.loadToDoFromDatabase(); // Atau bisa load kategori combo box
                }
            });

            kategoriStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void setRoot(String fxml, String title, boolean isResizeable){
        primaryStage.getScene().setRoot(loadFXML(fxml));
        primaryStage.setTitle(title);
        primaryStage.sizeToScene();
        primaryStage.setResizable(isResizeable);
    }

    private static Parent loadFXML(String fxml) {
        FXMLLoader fxmlLoader = new FXMLLoader(Apps.class.getResource(fxml + ".fxml"));
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String loggedInUsername;

    public static void setLoggedInUsername(String username) {
        loggedInUsername = username;
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
