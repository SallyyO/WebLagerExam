
package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.PasswordManager;
import dk.easv.weblagerexam.bll.SessionManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.util.Objects;

public class LoginController {
    @FXML
    public PasswordField txtPasswordField;
    @FXML
    public TextField txtUsernameField;
    @FXML
    public Label lblErr;
    @FXML
    public Button btnLogIn;

    PasswordManager passwordManager = new PasswordManager();

    //Shortcuts
    private final EventHandler<KeyEvent> keyHandler = event -> {
        switch (event.getCode()) {
            case ENTER -> { btnSignIn(); event.consume(); }
        }
    };

    @FXML
    public void initialize() {
        txtUsernameField.textProperty().addListener((_, _, _) -> updateButton());
        txtPasswordField.textProperty().addListener((_, _, _) -> updateButton());
        //txtUsernameField.insertText(0, "example@weblager.dk");
        //txtPasswordField.insertText(0, "admin");
    }

    private void updateButton() {
        btnLogIn.setDisable(txtUsernameField.getText().isEmpty() || txtPasswordField.getText().isEmpty());
    }

    public void btnSignIn() {
        String initials = txtUsernameField.getText().trim().toUpperCase();
        String password = txtPasswordField.getText();

        if (passwordManager.checkLogin(initials, password)) {
            User user = passwordManager.getUser();
            SessionManager.setCurrentUser(user);
            try {
                Stage stage = (Stage) txtUsernameField.getScene().getWindow();
                if (user.getRole().equals("Admin")) {

                    stage.getScene().setRoot(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../admin.fxml"))));
                    stage.setTitle("Admin");
                } else {
                    stage.getScene().setRoot(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../user.fxml"))));
                    stage.setTitle("User");
                }
            } catch (Exception e) {
                lblErr.setVisible(true);
                lblErr.setText("An error occurred while loading the homepage");
                e.printStackTrace();
            }
        } else {
            lblErr.setVisible(true);
            lblErr.setText("Wrong login or password!");
        }
    }
}
