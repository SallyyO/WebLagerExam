package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Client;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.bll.ClientManager;
import dk.easv.weblagerexam.bll.ProfileManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class AddOrEditClientController {

    @FXML private TextField txtClientName;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Label lblTitle;

    @FXML private ListView<Profile> availableProfilesListView;

    @FXML private ListView<Profile> assignedProfilesListView;

    @FXML private Button btnSave;

    @FXML private Button btnCancel;

    private final ClientManager clientManager = new ClientManager();

    private final ProfileManager profileManager = new ProfileManager();

    private AdminController adminController;

    private Client client;

    @FXML
    public void initialize() {

        cmbStatus.getItems().addAll(
                "Active",
                "Inactive"
        );

        cmbStatus.setValue("Active");

        lblTitle.setText("Add Client");

        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);
    }

    public void setClient(Client client) {

        this.client = client;

        lblTitle.setText("Edit Client");

        txtClientName.setText(client.getName());

        cmbStatus.setValue(
                client.isActive()
                        ? "Active"
                        : "Inactive"
        );

        loadProfiles();
    }

    private void loadProfiles() {

        List<Profile> allProfiles = profileManager.getAllProfiles();

        final List<Profile> assignedProfiles;

        if (client != null) {

            assignedProfiles = profileManager.getProfilesForClient(client.getId());

        } else {
            assignedProfiles = new ArrayList<>();
        }

        int currentClientId =
                client != null
                        ? client.getId()
                        : -1;

        allProfiles.removeIf(profile ->
                profile.getClientId() > 0
                        && profile.getClientId() != currentClientId
        );

        allProfiles.removeIf(profile ->
                assignedProfiles.stream()
                        .anyMatch(ap ->
                                ap.getId() == profile.getId())
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

        assignedProfilesListView
                .getItems()
                .add(selected);

        availableProfilesListView
                .getItems()
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

        availableProfilesListView
                .getItems()
                .add(selected);

        assignedProfilesListView
                .getItems()
                .remove(selected);
    }

    @FXML
    private void onSaveClicked() {

        try {

            String clientName =
                    txtClientName.getText().trim();

            if (clientName.isBlank()) {

                showError(
                        "Client name cannot be empty."
                );

                return;
            }

            boolean active =
                    "Active".equals(
                            cmbStatus.getValue()
                    );

            // CREATE
            if (client == null) {

                clientManager.createClient(
                        clientName
                );

                Client newClient =
                        clientManager.getAllClients()
                                .stream()
                                .filter(c ->
                                        c.getName()
                                                .equals(clientName))
                                .findFirst()
                                .orElse(null);

                if (newClient != null) {

                    for (Profile profile :
                            assignedProfilesListView.getItems()) {

                        profileManager.assignProfileToClient(
                                profile.getId(),
                                newClient.getId()
                        );
                    }

                    clientManager.setClientActive(
                            newClient.getId(),
                            active
                    );
                }

            }

            // EDIT
            else {

                client.setName(
                        clientName
                );

                clientManager.updateClient(
                        client
                );

                clientManager.setClientActive(
                        client.getId(),
                        active
                );

                saveProfiles();
            }

            if (adminController != null) {
                adminController.refreshCurrentInfo();
            }

            closeWindow();

        } catch (Exception ex) {

            ex.printStackTrace();

            showError(
                    "Could not save client."
            );
        }
    }

    private void saveProfiles() {

        // Remove all currently assigned profiles
        List<Profile> existingProfiles =
                profileManager.getProfilesForClient(
                        client.getId()
                );

        for (Profile profile : existingProfiles) {

            profileManager.removeProfileFromClient(
                    profile.getId()
            );
        }

        // Reassign selected profiles
        for (Profile profile :
                assignedProfilesListView.getItems()) {

            profileManager.assignProfileToClient(
                    profile.getId(),
                    client.getId()
            );
        }
    }

    @FXML
    private void onCancelClicked() {
        closeWindow();
    }

    private void closeWindow() {

        Stage stage =
                (Stage) btnCancel
                        .getScene()
                        .getWindow();

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


    public void gotANewClient() {

        this.client = null;

        lblTitle.setText("Add Client");

        txtClientName.clear();

        cmbStatus.setValue("Active");

        loadProfiles();
    }

    public void setAdminController(
            AdminController adminController) {

        this.adminController =
                adminController;
    }
}
