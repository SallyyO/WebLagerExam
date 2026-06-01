package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Document;
import dk.easv.weblagerexam.bll.DocumentManager;
import dk.easv.weblagerexam.bll.ExportManager;
import dk.easv.weblagerexam.util.ExportMode;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExportController {

    @FXML private Label lblBox;
    @FXML private Label lblProfile;
    @FXML private Label lblDocuments;

    @FXML private RadioButton radioMulti;
    @FXML private RadioButton radioSingle;

    @FXML private TextField txtFolder;

    private final DocumentManager documentManager =
            new DocumentManager();

    private Box box;

    //Shortcuts
    private final EventHandler<KeyEvent> keyHandler = event -> {
        switch (event.getCode()) {
            case B -> { browseFolder(); event.consume(); }
            case ESCAPE -> { cancel(); event.consume(); }
            case ENTER -> {export(); event.consume(); }
        }
    };

    public void setup(Box box, int documentCount) {

        this.box = box;
        lblBox.setText(String.valueOf(box.getBoxId()));
        lblProfile.setText(
                box.getProfile() != null
                        ? box.getProfile().getName()
                        : "No Profile"
        );

        lblDocuments.setText(String.valueOf(documentCount));
        radioMulti.setSelected(true);

        Platform.runLater(() -> {
            Scene scene = txtFolder.getScene();
            if (scene != null) {scene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);}
        });
    }

    @FXML
    private void browseFolder() {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Export Folder");
        java.io.File folder =
                chooser.showDialog(
                        txtFolder.getScene().getWindow());
        if (folder != null) {
            txtFolder.setText(folder.getAbsolutePath());
        }
    }

    @FXML
    private void export() {

        try {
            if (!validate()) {
                return;
            }
            ExportMode mode =
                    radioMulti.isSelected()
                            ? ExportMode.MULTI_PAGE
                            : ExportMode.SINGLE_PAGE;

            List<Document> documents =
                    documentManager.getDocsAndFilesFromBox(
                            box.getId());

            ExportManager manager = new ExportManager();
            Path exportedPath = manager.exportBox(
                    box, documents, Path.of(txtFolder.getText()), mode
            );

            
            showInfo(
                    "Export Completed",
                    "Files exported successfully:\n\n"
                            + exportedPath
            );

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Export Failed",
                    e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    @FXML
    void onInfoBtnClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/export_shortcuts.fxml")
            );
            VBox content = loader.load();

            Stage modal = new Stage();
            modal.initOwner(txtFolder.getScene().getWindow());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setScene(new Scene(content));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean validate() {
        String folder = txtFolder.getText();
        if (folder == null || folder.isBlank()) {
            showError("Missing Folder",
                    "Please select an export folder.");
            return false;
        }

        Path path = Path.of(folder);
        if (!Files.exists(path)) {
            showError("Invalid Folder",
                    "Selected folder does not exist.");
            return false;
        }
        return true;
    }


    private void closeWindow() {
        txtFolder.getScene()
                .removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        txtFolder.getScene()
                .getWindow()
                .hide();
    }

    private void showInfo(
            String title,
            String message) {
        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showError(
            String title,
            String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}