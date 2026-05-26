package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.util.LogoutUtil;
import dk.easv.weblagerexam.util.TiffConverter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

import javafx.scene.input.MouseEvent;

public class UserController {

    @FXML
    private Label lblUsername;

    @FXML
    private Label lblInitials;

    @FXML
    private Button boxesBtn;

    @FXML
    private Button exportBtn;

    @FXML
    private Button newScanBtn;

    @FXML
    private Button profilesBtn;

    @FXML
    private Button scanningButton;

    @FXML
    private Button splitDocumentsBtn;

    @FXML
    private Button deleteScansBtn;

    @FXML private ImageView latestScanPreview;
    @FXML private Label lblContinueScan;
    @FXML private VBox continueScanSection;

    @FXML private HBox userBox;

    private final DAOManager dao = new DAOManager();
    private Box latestBox = null;

    @FXML
    public void initialize() {

        User user = SessionManager.getCurrentUser();

        if (user != null) {

            lblUsername.setText(user.getUsername());
            lblInitials.setText(user.getInitials());
            loadLatestScan(user.getId());
        }

        userBox.setOnMouseClicked(e ->
                LogoutUtil.logout((Stage) userBox.getScene().getWindow())
        );
        Tooltip.install(userBox, new Tooltip("Click to log out"));
    }

    private void loadLatestScan(int userId) {
        try {
            File latestFile = dao.getDocumentDAO().getLatestFileForUser(userId);

            if (latestFile == null || latestFile.getImageData() == null) {
                // No previous scan — hide the whole section
                continueScanSection.setVisible(false);
                continueScanSection.setManaged(false);
                return;
            }

            // Convert and display the image
            Image preview = TiffConverter.toJavaFXImage(latestFile.getImageData());
            if (preview != null) {
                latestScanPreview.setImage(preview);
            }

            // Find which box this belongs to so we can resume
            latestBox = dao.getDocumentDAO().getBoxForFile(latestFile.getId());

            lblContinueScan.setText(latestBox != null
                    ? "Continue scan — Box #" + latestBox.getBoxId()
                    : "Continue scan");

        } catch (Exception e) {
            System.err.println("Could not load latest scan: " + e.getMessage());
            continueScanSection.setVisible(false);
            continueScanSection.setManaged(false);
        }
    }


    @FXML
    void onBoxesBtnClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/boxes-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Boxes");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onExportBtnClicked(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Export.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Export");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    void onNewScanBtnClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/scanning_page.fxml")
            );

            Parent root = loader.load();

            // Get current window (to replace it)
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Scanning");

            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onProfilesBtnClicked(ActionEvent event) {

    }

    @FXML
    void onScanningClicked(ActionEvent event) {

    }

    @FXML
    void onDeleteScansBtnClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/delete_scan.fxml")
            );

            Parent root = loader.load();

            // Get current window (to replace it)
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Deleting");

            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onContinueScanClicked(MouseEvent mouseEvent) {
        if (latestBox == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/scanning_page.fxml")
            );
            Parent root = loader.load();
            ScanningController scanController = loader.getController();
            scanController.resumeWithBox(latestBox);

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Scanning");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
