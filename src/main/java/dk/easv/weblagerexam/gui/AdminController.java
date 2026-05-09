package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.LogManager;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.bll.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AdminController {

    @FXML
    private Label lblUsername;

    @FXML
    private Label lblInitials;

    @FXML
    private Button addUserBtn;

    @FXML
    private Button deleteUserBtn;

    @FXML
    private Button editUserBtn;

    @FXML
    private Button fileInfoButton;

    @FXML
    private Button historyButton;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableView<User> mainTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> typeColumn;

    @FXML
    private Button usersButton;

    private UserManager userManager = new UserManager();
    LogManager logManager = new LogManager();


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

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadUsers();
    }


    @FXML
    void onAddUserBtnClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/addNewUser.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add User");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    void onDeleteUserBtnClicked(ActionEvent event) {
        User selectedUser = mainTable.getSelectionModel().getSelectedItem();
        if(selectedUser == null){
            showError(" No user selected", "Select a User to Delete ");
            return;
        }
        // confirm before deleting
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setContentText("Are you sure you want to delete " + selectedUser.getUsername() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try{
                    userManager.deleteUser((selectedUser.getId()));
                    loadUsers();

                }
                catch(Exception e){
                    showError("Could not delete User", e.getMessage());
                }
            }
        });

    }

    @FXML
    void onEditUserBtnClicked(ActionEvent event) {
        User selectedUser = mainTable.getSelectionModel().getSelectedItem();
        if( selectedUser == null){
            showError("No user selected", "Please select a user");
            return;
        }
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editUser.fxml"));
            Parent root = loader.load();

            EditUserControllers controller = loader.getController();
            controller.setUser(selectedUser);

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadUsers();
        }
        catch(Exception e){
            showError("Could not open Edit User Wndow", e.getMessage());
        }

    }

    @FXML
    void onFileInfoClicked(ActionEvent event) {

    }

    @FXML
    void onHistoryClicked(ActionEvent event) {
        try{
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            mainTable.setVisible(false);
            mainTable.setManaged(false);
            // TODO: Change the table for a log table
            //mainTable.setItems(
            //FXCollections.observableArrayList(logManager.getAllLogs())
            //);
        } catch (Exception e) {
            showError("Could not load Log", e.getMessage());
        }

    }

    @FXML
    void onUsersClicked(ActionEvent event) throws Exception {
        loadUsers();
    }

    private void loadUsers()  {
       try{
            mainTable.setItems(
                    FXCollections.observableArrayList(userManager.getAllUsers()));
        }
        catch (Exception e){
            showError("Could not load users", e.getMessage());
        }

    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
    /* we need a button for this
    @FXML
    private void openNewProfileDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/dk/easv/weblagerexam/newProfile.fxml")));
            Parent root = loader.load();
            NewProfileController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Create Profile");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(!!!!insert button.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();

            if (controller.isConfirmed()) {
                Profile created = controller.getCreatedProfile();
                System.out.println("Created profile: " + created.getName());
                // Refresh your profile list in the admin UI here
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not open profile dialog", e);
        }
    }
    */
}