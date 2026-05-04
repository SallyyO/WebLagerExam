package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.User;
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
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showError("Please fill out all fields");
            return;
        }

        // !! This does NOT add stuff to the database! It's just mock data for now !!
        User newUser = new User(0, username, role);

        System.out.println("Created user: " + username + " (" + role + ")");

        // Close window
        closeWindow();
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
        alert.setContentText(msg);
        alert.show();
    }
}