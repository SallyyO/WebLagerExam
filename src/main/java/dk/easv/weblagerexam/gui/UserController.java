package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.BoxManager;
import dk.easv.weblagerexam.bll.DocumentManager;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.util.LogoutUtil;
import dk.easv.weblagerexam.util.TiffConverter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class UserController {

    @FXML
    private Label lblUsername;
    @FXML
    private Label lblInitials;
    @FXML
    private HBox userBox;

    @FXML
    private Button boxesBtn;
    @FXML
    private Button newScanBtn;
    @FXML
    private Button profilesBtn;
    @FXML
    private Button scanningButton;

    @FXML
    private ImageView latestScanPreview;
    @FXML
    private Label lblContinueScan;
    @FXML
    private VBox continueScanSection;

    private final DocumentManager documentManager = new DocumentManager();
    private Box latestBox = null;

    //Shortcuts
    private final EventHandler<KeyEvent> keyHandler = event -> {
        switch (event.getCode()) {
            case B -> {
                onBoxesBtnClicked(new ActionEvent());
                event.consume();
            }
            case N -> {
                onNewScanBtnClicked(new ActionEvent());
                event.consume();
            }
            case S -> {
                onScanningClicked(new ActionEvent());
                event.consume();
            }
        }
    };

    @FXML
    public void initialize() {

        User user = SessionManager.getCurrentUser();

        if (user != null) {

            lblUsername.setText(user.getUsername());
            lblInitials.setText(user.getInitials());
            loadLatestScan(user.getId());
        }

        userBox.setOnMouseClicked(e -> handleLogout());
        Tooltip.install(userBox, new Tooltip("Click to log out"));

        boxesBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            }
        });
    }

    private void loadLatestScan(int userId) {
        try {
            File latestFile = documentManager.getLatestFileForUser(userId);

            if (latestFile == null || latestFile.getImageData() == null) {
                // No scans yet — show placeholder text, keep section visible
                lblContinueScan.setText("No scans yet — start a new scan below");
                latestScanPreview.setImage(new Image(
                        getClass().getResourceAsStream("/Images/WeblagerWhiteIcon.png")));
                // Disable click since there's nothing to resume
                continueScanSection.setOnMouseClicked(null);
                continueScanSection.setStyle(""); // remove hand cursor
                return;
            }

            new Thread(() -> {
                Image preview = TiffConverter.toJavaFXImage(latestFile.getImageData());
                Box box = documentManager.getBoxForFile(latestFile.getId());
                latestBox = box;

                Platform.runLater(() -> {
                    if (preview != null) latestScanPreview.setImage(preview);
                    lblContinueScan.setText(box != null
                            ? "Continue scan — Box #" + box.getBoxId()
                            : "Continue scan");
                });
            }).start();

        } catch (Exception e) {
            System.err.println("Could not load latest scan: " + e.getMessage());
            lblContinueScan.setText("Could not load latest scan");
            continueScanSection.setOnMouseClicked(null);
        }
    }


    @FXML
    void onBoxesBtnClicked(ActionEvent event) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/weblagerexam/boxes-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) boxesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Boxes");
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void onNewScanBtnClicked(ActionEvent event) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/dk/easv/weblagerexam/scanning_page.fxml")
                );

                Parent root = loader.load();

                // Get current window (to replace it)
                Stage stage = (Stage) boxesBtn.getScene().getWindow();

                stage.setScene(new Scene(root));
                stage.setTitle("Scanning");

                stage.centerOnScreen();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void onScanningClicked(ActionEvent event) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/dk/easv/weblagerexam/scanning_page.fxml")
                );

                Parent root = loader.load();

                Stage stage = (Stage) boxesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Scanning");

                stage.centerOnScreen();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void onContinueScanClicked(MouseEvent mouseEvent) {

        if (latestBox == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/scanning_page.fxml")
            );

            Parent root = loader.load();

            ScanningController scanController = loader.getController();

            Box fullBox = new BoxManager().getBoxById(latestBox.getId());

            scanController.resumeWithBox(fullBox);

            Stage stage =
                    (Stage) ((Node) mouseEvent.getSource())
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
    void onInfoBtnClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/user_shortcuts.fxml")
            );
            VBox content = loader.load();

            Stage modal = new Stage();
            modal.initOwner(boxesBtn.getScene().getWindow());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setScene(new Scene(content));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        Stage stage =
                (Stage) userBox.getScene().getWindow();

        LogoutUtil.logout(stage);
    }

}
