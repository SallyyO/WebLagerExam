package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.bll.PasswordManager;
import dk.easv.weblagerexam.bll.UserManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddUserController
{
    @FXML
    private TextField nameTxt;
    @FXML
    private ComboBox<String> cbRole;
    @FXML
    private TextField emailTxt;
    @FXML
    private TextField passwordTxt;

    private Stage dialogStage;

    private final PasswordManager passwordManager = new PasswordManager();

    public void initialize() {
        cbRole.getItems().addAll("Admin", "User");
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void btnAddUser() {
        try {
            String username = nameTxt.getText().trim();
            String role = cbRole.getSelectionModel().getSelectedItem();
            String email = emailTxt.getText().trim();
            String password = passwordTxt.getText().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please make sure you've filled out all of the fields");
                alert.showAndWait();
                return;
            }
            //passwordManager.AddUser(role, username, email, password);
            UserManager userManager = new UserManager();
            userManager.createUser(username, role, email, password);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while adding the user: " + e.getMessage());
            alert.showAndWait();
        }
        dialogStage.close();
    }

    public void btnCancel() {
        dialogStage.close();
    }
}
