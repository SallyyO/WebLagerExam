package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.PasswordManager;
import dk.easv.weblagerexam.bll.UserManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddNewUserController {

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "User");
    }

    @FXML
    void onSaveClicked() {
        try {
            String username = txtUsername.getText().trim();
            String email = txtEmail.getText().trim();
            String password = txtPassword.getText().trim();
            String role = roleComboBox.getValue();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
                showError("Please fill out all fields");
                return;
            }

            PasswordManager pm = new PasswordManager();
            pm.AddUser(role, username, email, password);

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    void onCancelClicked() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.show();
    }
}