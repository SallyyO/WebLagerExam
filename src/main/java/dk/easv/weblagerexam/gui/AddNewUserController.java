package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.*;
import dk.easv.weblagerexam.dal.DAOManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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
    private ComboBox<String> cmbStatus;

    @FXML
    private VBox profileSection;

    @FXML
    private ListView<Profile> availableProfilesListView;

    @FXML
    private ListView<Profile> assignedProfilesListView;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private final UserManager userManager = new UserManager();
    private final ProfileManager profileManager = new ProfileManager();
    private final LogManager logManager = new LogManager();
    private AdminController adminController;
    private final DAOManager dao = new DAOManager();
    private User user;


    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "User");
        cmbStatus.getItems().addAll(
                "Active",
                "Inactive"
        );

        cmbStatus.setValue("Active");
        profileSection.setVisible(false);
        profileSection.setManaged(false);

        roleComboBox.valueProperty().addListener(
                (obs, oldVal, newVal) -> {
                    boolean showProfiles =
                            "User".equals(newVal);
                    profileSection.setVisible(showProfiles);
                    profileSection.setManaged(showProfiles);
                });

        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);
    }


    @FXML
    void onSaveClicked() throws Exception {

        //Create/edit
        PasswordManager pm = new PasswordManager();

        String username =
                txtUsername.getText().trim();

        String initials =
                txtInitials.getText().trim();

        String role =
                roleComboBox.getValue();

        boolean active =
                "Active".equals(cmbStatus.getValue());

        if (username.isBlank()
                || initials.isBlank()
                || role == null) {

            showError("Please fill out all required fields.");
            return;
        }

        //Editing profile
        if (user != null) {

            String password =
                    txtPassword.getText().trim();

            pm.editUser(
                    user.getId(),
                    username,
                    initials,
                    role,
                    password.isBlank()
                            ? null
                            : password
            );

            userManager.setUserActive(
                    user.getId(),
                    active
            );

            saveProfilesToUser();
        }
        // create user
        else {

            String password =
                    txtPassword.getText().trim();

            if (password.isBlank()) {
                showError("Password is required.");
                return;
            }

            int newUserId =
                    pm.AddUser(
                            role,
                            username,
                            initials,
                            password
                    );

            userManager.setUserActive(
                    newUserId,
                    active
            );

            if ("User".equals(role)) {

                for (Profile profile :
                        assignedProfilesListView.getItems()) {

                    userManager.assignProfileToUser(
                            newUserId,
                            profile.getId()
                    );
                }
            }

            logManager.logUserCreated(username);
        }

        if (adminController != null) {
            adminController.refreshCurrentInfo();
        }

        closeWindow();
    }

    @FXML
    private void handleAssignProfile() {

        Profile selected =
                availableProfilesListView
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {
            return;
        }

        assignedProfilesListView.getItems()
                .add(selected);

        availableProfilesListView.getItems()
                .remove(selected);
    }

    @FXML
    private void handleRemoveProfile() {

        Profile selected =
                assignedProfilesListView
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {
            return;
        }

        availableProfilesListView.getItems()
                .add(selected);

        assignedProfilesListView.getItems()
                .remove(selected);
    }

    @FXML
    void onInfoBtnClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/general_shortcuts.fxml")
            );
            VBox content = loader.load();

            Stage modal = new Stage();
            modal.initOwner(btnSave.getScene().getWindow());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setScene(new Scene(content));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveProfilesToUser() throws Exception {

        if (user == null) {
            return;
        }

        dao.getUserDAO().removeAllProfilesFromUser(
                user.getId()
        );

        for (Profile profile :
                assignedProfilesListView.getItems()) {

            userManager.assignProfileToUser(
                    user.getId(),
                    profile.getId()
            );
        }
    }

    public void setUser(User user) {

        try {

            this.user = user;

            txtUsername.setText(
                    user.getUsername()
            );

            txtInitials.setText(
                    user.getInitials()
            );

            roleComboBox.setValue(
                    user.getRole()
            );

            cmbStatus.setValue(
                    user.isActive()
                            ? "Active"
                            : "Inactive"
            );

            loadProfiles();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfiles() throws Exception {

        List<Profile> allProfiles =
                profileManager.getAllProfiles();

        List<Profile> assignedProfiles =
                userManager.getProfilesForUser(
                        user.getId()
                );

        allProfiles.removeIf(profile ->
                assignedProfiles.stream()
                        .anyMatch(assigned ->
                                assigned.getId()
                                        == profile.getId())
        );

        availableProfilesListView.setItems(
                FXCollections.observableArrayList(
                        allProfiles
                )
        );

        assignedProfilesListView.setItems(
                FXCollections.observableArrayList(
                        assignedProfiles
                )
        );

        boolean showProfiles =
                "User".equals(user.getRole());

        profileSection.setVisible(showProfiles);
        profileSection.setManaged(showProfiles);
    }

    @FXML
    private void onCancelClicked() {
        closeWindow();
    }

    private void closeWindow() {

        Stage stage =
                (Stage) btnCancel.getScene().getWindow();

        stage.close();
    }

    private void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public void setAdminController(
            AdminController adminController) {

        this.adminController = adminController;
    }
}

