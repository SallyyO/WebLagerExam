package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.FileManager;
import dk.easv.weblagerexam.bll.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import java.util.List;
import java.io.IOException;

public class DeleteScanController {

    @FXML
    private ListView<File> fileListView;

    @FXML
    private Label lblInitials;

    @FXML
    private Label lblUsername;

    @FXML
    private TextField searchTextfield;

    private final FileManager fileManager = new FileManager();

    private final ObservableList<File> files = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();

        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblInitials.setText(user.getInitials());
        }

        // Bind listview to files list once, here
        fileListView.setItems(files);

        fileListView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("ID: " + item.getId() + " | File Number: " + item.getFileNumber() + " | Barcode: " + item.isBarcode());
                }
            }
        });

        loadFiles();
    }
    private void loadFiles() {
        Task<List<File>> task = new Task<>() {
            @Override
            protected List<File> call() {
                List<File> result = fileManager.getAllFiles();
                System.out.println("Files fetched from DB: " + (result != null ? result.size() : "null"));
                return result;
            }
        };

        task.setOnSucceeded(e -> {
            System.out.println("Task succeeded, updating list with: " + task.getValue().size() + " files");
            files.setAll(task.getValue());
            System.out.println("Observable list size after setAll: " + files.size());
            System.out.println("ListView items size: " + fileListView.getItems().size());
        });

        task.setOnFailed(e -> System.err.println("Failed to load files: " + task.getException().getMessage()));

        new Thread(task).start();
    }

    @FXML
    void handleListClick(MouseEvent event) {

    }

    @FXML
    void onCloseBtnPress(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/user.fxml")
            );

            Parent root = loader.load();

            // Get current window (to replace it)
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("User");

            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onHardDeleteBtnPress(ActionEvent event) {
        File selectedFile = fileListView.getSelectionModel().getSelectedItem();

        if (selectedFile == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                fileManager.hardDeleteFile(selectedFile.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> loadFiles());
        task.setOnFailed(e -> System.err.println("Delete failed: " + task.getException().getMessage()));

        new Thread(task).start();
    }

    @FXML
    void onRefreshBtnPress(ActionEvent event) {
        loadFiles();
    }

    @FXML
    void onSearchBtnPress(ActionEvent event) {
        String searchText = searchTextfield.getText();

        if (searchText == null || searchText.isBlank()) {
            loadFiles();
            return;
        }

        Task<List<File>> task = new Task<>() {
            @Override
            protected List<File> call() {
                return fileManager.searchFiles(searchText);
            }
        };

        task.setOnSucceeded(e -> files.setAll(task.getValue()));
        task.setOnFailed(e -> System.err.println("Search failed: " + task.getException().getMessage()));

        new Thread(task).start();
    }
}

