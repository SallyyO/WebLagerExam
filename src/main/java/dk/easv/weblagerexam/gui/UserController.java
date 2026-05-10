package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

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
    private Button viewScansBtn;

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
    }

    @FXML
    void onBoxesBtnClicked(ActionEvent event) {

    }

    @FXML
    void onExportBtnClicked(ActionEvent event) {

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
    void onViewScansBtnClicked(ActionEvent event) {

    }

    @FXML
    void onsplitDocumentsBtnClicked(ActionEvent event) {

    }

}
