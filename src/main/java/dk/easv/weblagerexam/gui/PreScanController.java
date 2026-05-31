package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PreScanController {

    @FXML private TextField txtBoxId;
    @FXML private ComboBox<Profile> cmbProfile;
    @FXML private Label lblBoxError;
    @FXML private Label lblProfileError;
    @FXML private Button btnStart;
    @FXML private Button btnCancel;

    private final ProfileManager profileManager = new ProfileManager();
    private final BoxManager boxManager = new BoxManager();


    private Box resultBox = null;
    private boolean confirmed = false;


    @FXML
    public void initialize() {
        User currentUser = SessionManager.getCurrentUser();

        try {
            List<Profile> profiles = profileManager.getProfilesForUser(currentUser.getId());
            cmbProfile.getItems().addAll(profiles);
        } catch (Exception e) {
            System.err.println("Could not load profiles: " + e.getMessage());
        }

        // Only allow numbers in Box ID field
        txtBoxId.textProperty().addListener((_, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) txtBoxId.setText(oldVal);
            lblBoxError.setVisible(false);
            lblBoxError.setManaged(false);
        });
        cmbProfile.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblProfileError.setVisible(false);
            lblProfileError.setManaged(false);
        });

        btnStart.setDefaultButton(true);
        btnCancel.setCancelButton(true);
    }

    @FXML
    private void confirm() {
        String boxIdText = txtBoxId.getText().trim();

        if (boxIdText.isEmpty()) {
            lblBoxError.setText("Please enter a Box ID");
            lblBoxError.setVisible(true);
            lblBoxError.setManaged(true);
            return;
        }

        int boxId = Integer.parseInt(boxIdText);

        User currentUser = SessionManager.getCurrentUser();
        Profile selectedProfile = cmbProfile.getValue();

        if (selectedProfile == null) {
            lblProfileError.setText("Please select a profile");
            lblProfileError.setVisible(true);
            lblProfileError.setManaged(true);
            return;
        }

        lblProfileError.setVisible(false);
        lblProfileError.setManaged(false);

        int profileId =  selectedProfile.getId();

        Box box = new Box(currentUser.getId());
        box.setBoxId(boxId);
        box.setProfileId(profileId);
        box.setProfile(selectedProfile); // stores the full object
        boxManager.saveBox(box);

        new LogManager().logBoxCreated(currentUser.getId(), box.getBoxId(), currentUser.getUsername());

        resultBox = box;
        confirmed = true;
        txtBoxId.getScene().getWindow().hide();
    }

    @FXML
    private void cancel() {
        confirmed = false;
        txtBoxId.getScene().getWindow().hide();
    }

    @FXML
    void onInfoBtnClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/general_shortcuts.fxml")
            );
            VBox content = loader.load();

            Stage modal = new Stage();
            modal.initOwner(btnStart.getScene().getWindow());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setScene(new Scene(content));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Box getResultBox() { return resultBox; }
    public boolean isConfirmed() { return confirmed; }
}
