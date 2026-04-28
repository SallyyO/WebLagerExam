package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.ProfileManager;
import dk.easv.weblagerexam.bll.UserManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class EditUserControllers {

    @FXML
    public Label userNameLabel;
    @FXML
    public ListView<Profile> availableProfilesListView;
    @FXML
    public ListView<Profile> assignedProfilesListView;

    private UserManager userManager = new UserManager();
    private ProfileManager profileManager = new ProfileManager();
    private User selectedUser;

    public void initialize() {}


    public void setUser(User user){
        this.selectedUser = user;
        userNameLabel.setText("Editing:" + user.getUsername());
        loadProfiles();
    }

    private void loadProfiles() throws Exception {
        List<Profile> allProfiles = profileManager.getAllProfiles();

        List<Profile> assignedprofiles = userManager.getProfilesForUser(selectedUser.getId());

        // Available profiles = all profiles minus already assigned
        allProfiles.removeIf(p-> assignedprofiles.stream().anyMatch(a -> a.getId() == p.getId()));

        availableProfilesListView.setItems(FXCollections.observableArrayList(allProfiles));
        assignedProfilesListView.setItems(FXCollections.observableArrayList(assignedprofiles));
    }


    @FXML
    public void handleAssignProfile(ActionEvent actionEvent) throws Exception {
        Profile selected = availableProfilesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a profile to assign");
            return;
        }
        userManager.assignProfileToUser(selectedUser.getId(), selected.getId());
        loadProfiles();
    }

    @FXML
    public void handleRemoveProfile(ActionEvent actionEvent) throws Exception {
        Profile selected = availableProfilesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a profile to remove");
            return;
        }
        userManager.removeProfileFromUser(selectedUser.getId(), selected.getId());
        loadProfiles();
    }

    @FXML
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }
}
