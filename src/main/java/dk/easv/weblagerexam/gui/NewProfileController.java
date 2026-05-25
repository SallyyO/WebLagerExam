package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.ProfileSettings;
import dk.easv.weblagerexam.bll.ProfileManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class NewProfileController {

    @FXML private TextField  txtName;
    @FXML private ComboBox<ProfileSettings> cmbSettings;
    @FXML private VBox       valueSection;
    @FXML private Label      lblValueDescription;
    @FXML private TextField  txtValue;
    @FXML private Label      lblPreview;
    @FXML private Label      lblNameError;
    @FXML private Label      lblSettingsError;
    @FXML private Label      lblValueError;

    private final ProfileManager profileManager = new ProfileManager();

    private Profile createdProfile = null;
    private boolean confirmed = false;

    @FXML
    public void initialize() {
        cmbSettings.getItems().addAll(ProfileSettings.values());

        // Update the value section and preview whenever selection changes
        cmbSettings.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProfileSettings s) {
                if (s == null) return "";
                return switch (s) {
                    case GRAYSCALE   -> "Grayscale (Black & White)";
                    case ROTATE      -> "Rotate by degrees";
                    case ROTATE_AUTO -> "Rotate to Horizontal";
                    case BRIGHTEN    -> "Brighten";

                    case RAVENCLAW   -> "Ravenclaw (Blue)";
                    case GRYFFINDOR  -> "Gryffindor (Red)";
                    case SLYTHERIN   -> "Slytherin (Green)";
                    case HUFFLEPUFF  -> "Hufflepuff (Yellow)";
                };
            }
            @Override
            public ProfileSettings fromString(String s) { return null; }
        });
    }

    @FXML
    private void onSettingsChanged() {
        ProfileSettings selected = cmbSettings.getValue();
        if (selected == null) return;

        hideError(lblSettingsError);

        switch (selected) {
            case GRAYSCALE -> {
                hideValueSection();
                lblPreview.setText(
                        "All pages in the box will be converted to grayscale.");
            }
            case ROTATE -> {
                showValueSection(
                        "Degrees (positive = clockwise, e.g. 5)",
                        "0.0");
                lblPreview.setText(
                        "All pages will be rotated by the specified number of degrees.");
            }
            case ROTATE_AUTO -> {
                hideValueSection();
                lblPreview.setText(
                        "Portrait pages will automatically be rotated 90° to landscape.");
            }
            case BRIGHTEN -> {
                showValueSection(
                        "Brightness increase (0–255, e.g. 30)",
                        "30");
                lblPreview.setText(
                        "All pages will have their brightness increased by the given amount.");
            }
            case RAVENCLAW -> {
                hideValueSection();
                lblPreview.setText(
                        "All pages will be converted to a blue monochrome style.");
            }

            case GRYFFINDOR -> {
                hideValueSection();
                lblPreview.setText(
                        "All pages will be converted to a red monochrome style.");
            }

            case SLYTHERIN -> {
                hideValueSection();
                lblPreview.setText(
                        "All pages will be converted to a green monochrome style.");
            }

            case HUFFLEPUFF -> {
                hideValueSection();
                lblPreview.setText(
                        "All pages will be converted to a yellow monochrome style.");
            }
        }
    }

    @FXML
    private void createProfile() {
        if (!validate()) return;

        String name = txtName.getText().trim();
        ProfileSettings settings = cmbSettings.getValue();

        Double value = null;
        if (settings == ProfileSettings.ROTATE || settings == ProfileSettings.BRIGHTEN) {
            value = Double.parseDouble(txtValue.getText().trim());
        }

        try {
            Profile profile = profileManager.createProfile(name, settings, value);
            createdProfile = profile;
            confirmed = true;
            txtName.getScene().getWindow().hide();
        } catch (Exception e) {
            showError(lblNameError, "Could not save profile: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        confirmed = false;
        txtName.getScene().getWindow().hide();
    }

    // Validation

    private boolean validate() {
        boolean valid = true;

        if (txtName.getText().trim().isEmpty()) {
            showError(lblNameError, "Please enter a profile name");
            valid = false;
        } else {
            hideError(lblNameError);
        }

        if (cmbSettings.getValue() == null) {
            showError(lblSettingsError, "Please select a profile");
            valid = false;
        } else {
            hideError(lblSettingsError);
        }

        ProfileSettings selected = cmbSettings.getValue();
        if (selected == ProfileSettings.ROTATE || selected == ProfileSettings.BRIGHTEN) {
            String val = txtValue.getText().trim();
            if (val.isEmpty()) {
                showError(lblValueError, "Please enter a value");
                valid = false;
            } else {
                try {
                    double parsed = Double.parseDouble(val);
                    if (selected == ProfileSettings.BRIGHTEN
                            && (parsed < 0 || parsed > 255)) {
                        showError(lblValueError, "Brightness must be between 0 and 255");
                        valid = false;
                    } else {
                        hideError(lblValueError);
                    }
                } catch (NumberFormatException e) {
                    showError(lblValueError, "Must be a number");
                    valid = false;
                }
            }
        }

        return valid;
    }

    // helpers

    private void showValueSection(String description, String defaultValue) {
        lblValueDescription.setText(description);
        txtValue.setPromptText(defaultValue);
        txtValue.clear();
        valueSection.setVisible(true);
        valueSection.setManaged(true);
    }

    private void hideValueSection() {
        valueSection.setVisible(false);
        valueSection.setManaged(false);
        hideError(lblValueError);
    }

    private void showError(Label lbl, String message) {
        lbl.setText(message);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void hideError(Label lbl) {
        lbl.setVisible(false);
        lbl.setManaged(false);
    }

    public Profile getCreatedProfile() { return createdProfile; }
    public boolean isConfirmed()       { return confirmed; }
}