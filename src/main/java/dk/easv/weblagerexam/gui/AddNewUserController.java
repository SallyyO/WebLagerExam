package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.bll.PasswordManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddNewUserController {

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtInitials;

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
            String initials = txtInitials.getText().trim();
            String password = txtPassword.getText().trim();
            String role = roleComboBox.getValue();

            if (username.isEmpty() || initials.isEmpty() || password.isEmpty() || role == null) {
                showError("Please fill out all fields");
                return;
            }

            PasswordManager pm = new PasswordManager();
            pm.AddUser(role, username, initials, password);

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