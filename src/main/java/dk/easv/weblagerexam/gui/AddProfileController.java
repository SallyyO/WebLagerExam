package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.bll.ProfileManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.security.PrivateKey;

public class AddProfileController {
    @FXML
    private TextField profileNameField;

    private final ProfileManager profileManager = new ProfileManager();

    /* @FXML
    private void handleAddProfile(ActionEvent event) {
        String name = profileNameField.getText();
            profileManager.createProfile(name);
            Stage stage = (Stage) profileNameField.getScene().getWindow();
            stage.close();
    }

     */

    //Code for adminController
    // there should be loadProfiles() method with observable list to update the profile

    /*
    @FXML
    private void openAddProfile(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AddProfile.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Add Profile");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL); // user must deal with this popup window( other windows will be blocked)
        stage.showAndWait();

        loadProfiles(); // Refresh our profile list after closing

    }

     */


}
