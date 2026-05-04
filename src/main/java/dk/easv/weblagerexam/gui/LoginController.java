
package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.MockAuthService;
import dk.easv.weblagerexam.bll.PasswordManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private PasswordField txtPasswordField;
    @FXML
    public TextField txtUsernameField;
    @FXML
    public Label lblErr;
    @FXML
    public Button btnLogIn;

    PasswordManager passwordManager = new PasswordManager();
    
    private void updateButton() {
        btnLogIn.setDisable(txtUsernameField.getText().isEmpty() || txtPasswordField.getText().isEmpty());
    }

    @FXML private void btnSignIn() {
        String login = txtUsernameField.getText();
        String password = txtPasswordField.getText();
        if (passwordManager.checkLogin(login, password)) {
            User user = passwordManager.getUser();
            try {
                Stage stage = (Stage) txtUsernameField.getScene().getWindow();
                if (user.getRole().equals("Admin")) {
                    // TODO: set the active user in the header
                    stage.getScene().setRoot(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../admin.fxml"))));
                } else {
                    stage.getScene().setRoot(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../user.fxml"))));
                }
            } catch (Exception e) {
                lblErr.setVisible(true);
                lblErr.setText("An error occurred while loading the homepage");
            }
        } else {
            lblErr.setVisible(true);
            lblErr.setText("Wrong login or password!");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtUsernameField.textProperty().addListener((_, _, _) -> updateButton());
        txtPasswordField.textProperty().addListener((_, _, _) -> updateButton());
        txtUsernameField.insertText(0, "admin");
        txtPasswordField.insertText(0, "admin");
    }
}
