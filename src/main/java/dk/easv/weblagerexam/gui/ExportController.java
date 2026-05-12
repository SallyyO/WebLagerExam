package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.bll.ExportManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class ExportController {
    @FXML
    public Label profileNameLabel;
    @FXML
    public Label boxIdLabel;
    @FXML
    public Label pageCountLabel;
    @FXML
    public Button btnSelectFolder;
    @FXML
    public Label selectedFolderLabel;
    @FXML
    private Button btnExport;
    @FXML private Button btnCancel;


    private ExportManager exportManager = new ExportManager();

    private String selectedFolderPath = null;
    private List<byte[]> tiffPages;
    private Profile profile;
    private Box box;

    public void initialize() {
        btnExport.setDisable(true); // disable until folder is selected
    }

    public void setExportData(List<byte[]> tiffPages, Profile profiles, Box box) {
        this.tiffPages = tiffPages;
        this.profile = profiles;
        this.box = box;

        profileNameLabel.setText("Profile: " + profiles.getName());
        boxIdLabel.setText("Box ID: " + box.getBoxId());
        pageCountLabel.setText("Total pages: " + tiffPages.size());
    }


    @FXML
    public void onSelectFolderClicked(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder");
        Stage stage = (Stage) btnSelectFolder.getScene().getWindow();
        File selectedFolder = chooser.showDialog(stage);
        if (selectedFolder != null) {
            selectedFolderPath = selectedFolder.getAbsolutePath();
            selectedFolderLabel.setText("Folder: " + selectedFolderPath);
            btnExport.setDisable(false);
        }
    }

    @FXML
    public void onExportClicked(ActionEvent actionEvent) {

        try {
            exportManager.exportMultiPageTiff(
                    tiffPages,
                    selectedFolderPath,
                    profile.getName(),
                    String.valueOf(box.getBoxId()));

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setContentText("File exported successfully to:\n" + selectedFolderPath);
            alert.showAndWait();

            // Close popup
            Stage stage = (Stage) btnExport.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setContentText("Could not export file: " + e.getMessage());
            alert.show();
        }
    }

        @FXML
        public void onCancelClicked (ActionEvent actionEvent){
            Stage stage = (Stage) btnExport.getScene().getWindow();
            stage.close();
        }
    }
