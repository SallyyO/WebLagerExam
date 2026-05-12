package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.dal.DAOManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class PreScanController {

    @FXML private TextField txtBoxId;
    @FXML private ComboBox<Profile> cmbProfile;
    @FXML private Label lblBoxError;
    @FXML private Button btnStart;

    private final DAOManager dao = new DAOManager();

    private Box resultBox = null;
    private boolean confirmed = false;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getCurrentUser();

        try {
            List<Profile> profiles = dao.getProfileDAO().getProfilesForUser(currentUser.getId());
            cmbProfile.getItems().addAll(profiles);
        } catch (Exception e) {
            System.err.println("Could not load profiles: " + e.getMessage());
            e.printStackTrace(); // print full stack
        }

        // Only allow numbers in Box ID field
        txtBoxId.textProperty().addListener((_, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) txtBoxId.setText(oldVal);
            lblBoxError.setVisible(false);
            lblBoxError.setManaged(false);
        });
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
        int profileId = selectedProfile != null ? selectedProfile.getId() : 0;

        Box box = new Box(currentUser.getId());
        box.setBoxId(String.valueOf(boxId));
        box.setProfileId(profileId);
        box.setProfile(selectedProfile); // stores the full object
        dao.getBoxDAO().saveBox(box);

        resultBox = box;
        confirmed = true;
        txtBoxId.getScene().getWindow().hide();
    }

    @FXML
    private void cancel() {
        confirmed = false;
        txtBoxId.getScene().getWindow().hide();
    }

    public Box getResultBox() { return resultBox; }
    public boolean isConfirmed() { return confirmed; }
}
