package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.UserManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class
AdminController {

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
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> typeColumn;

    @FXML
    private Button usersButton;

    private UserManager userManager = new UserManager();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadUsers();

    }



    // I will create addUser FXML and controller at night
    @FXML
    void onAddUserBtnClicked(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addUser.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add User");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            AddUserController controller = loader.getController();
            controller.setDialogStage(stage);

            stage.showAndWait();
            loadUsers();

    }
        catch(Exception e){
        showError("Could not open Add User Wndow", e.getMessage());
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

}
