package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.LogManager;
import dk.easv.weblagerexam.bll.PasswordManager;
import dk.easv.weblagerexam.bll.SessionManager;
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

    private AdminController adminController;
    private User user;
    LogManager logManager = new LogManager();

    @FXML
    void onSaveClicked() throws Exception {
        PasswordManager pm = new PasswordManager();
        if (user != null) {
            if (!txtPassword.getText().isEmpty()) {
                String newPassword = txtPassword.getText();
                pm.editUser(user.getId(), txtUsername.getText(), txtInitials.getText(), roleComboBox.getSelectionModel().getSelectedItem(), txtPassword.getText());
            }
            else {
                pm.editUser(user.getId(),txtUsername.getText(), txtInitials.getText(), roleComboBox.getSelectionModel().getSelectedItem(), null);
            }

            }
         else {
            try {
                String username = txtUsername.getText().trim();
                String initials = txtInitials.getText().trim();
                String password = txtPassword.getText().trim();
                String role = roleComboBox.getValue();

                if (username.isEmpty() || initials.isEmpty() || password.isEmpty() || role == null) {
                    showError("Please fill out all fields");
                    return;
                }


                pm.AddUser(role, username, initials, password);
                logManager.logUserCreated(
                        username
                );

            } catch (Exception e) {
                e.printStackTrace();
                showError("Error: " + e.getMessage());
            }
        }
        if (adminController != null) {
            adminController.refreshCurrentInfo();
        }
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
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.show();
    }

    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }

    public void setUser(User user) {
        this.user = user;
        this.txtUsername.setText(user.getUsername());
        this.txtInitials.setText(user.getInitials());
        this.roleComboBox.setValue(user.getRole());
    }
}