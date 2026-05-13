package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.SessionManager;
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
    private Label countLabel;

    @FXML
    private ListView<?> fileListView;

    @FXML
    private Label lblInitials;

    @FXML
    private Label lblUsername;

    @FXML
    private TextField searchTextfield;

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

    }

    @FXML
    void onRefreshBtnPress(ActionEvent event) {

    }

    @FXML
    void onSearchBtnPress(ActionEvent event) {

    }

    @FXML
    void onSoftDeleteBtnPress(ActionEvent event) {

    }

}

