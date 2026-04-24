package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.bll.BoxManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.lang.classfile.Label;

public class AddBoxController {
    private BoxManager boxManager = new BoxManager();
    private Profile selectedProfile;
    @FXML
    private TextField profileNameField;
    @FXML
    public void handleAddBox(ActionEvent actionEvent) {
        try{
            boxManager.createBox(selectedProfile.getId());
            Stage stage = (Stage) profileNameField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    // call it from adminController to pass selected profile
    public void setProfileNameField(Profile profile){
        profileNameField.setText("Adding box to: " + profile.getName());
    }
}
