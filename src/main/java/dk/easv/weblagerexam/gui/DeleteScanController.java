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

            lblUsername.setText(
                    user.getUsername()
            );

            lblInitials.setText(
                    user.getInitials()
            );
        }

        loadFiles();

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
    }
    private void loadFiles() {

        files.setAll(fileManager.getAllFiles());

        fileListView.setItems(files);
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

        if (selectedFile == null) {
            return;
        }

        fileManager.hardDeleteFile(selectedFile.getId());

        loadFiles();
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

        files.setAll(fileManager.searchFiles(searchText));
    }
}

