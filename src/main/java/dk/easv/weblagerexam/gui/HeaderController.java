package dk.easv.weblagerexam.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class HeaderController {

    @FXML private StackPane avatarCircle;
    @FXML private Label avatarLabel;
    @FXML private Label usernameLabel;
    @FXML private HBox userSection;

    private Runnable logoutAction;

    private final ContextMenu userMenu = new ContextMenu();

    @FXML
    public void initialize() {
        setupDropdownMenu();
        hideUser();
    }

    private void setupDropdownMenu() {

        MenuItem logoutItem = new MenuItem("Log out");

        logoutItem.setOnAction(e -> {
            if (logoutAction != null) {
                logoutAction.run();
            }
        });

        userMenu.getItems().add(logoutItem);
    }

    public void setUser(String fullName) {
        usernameLabel.setText(fullName);

        avatarLabel.setText(getInitials(fullName));

        userSection.setVisible(true);
        userSection.setManaged(true);
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) +
                parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    public void setUser(String fullName, String initials) {
        usernameLabel.setText(fullName);

        // Use DB initials directly rather than computing from name
        avatarLabel.setText(initials != null && !initials.isBlank()
                ? initials.toUpperCase()
                : getInitials(fullName));  // fallback if initials column is empty

        userSection.setVisible(true);
        userSection.setManaged(true);
    }

    public void clearUser() {
        hideUser();
    }

    private void hideUser() {
        userSection.setVisible(false);
        userSection.setManaged(false);
    }

    public void setLogoutAction(Runnable logoutAction) {
        this.logoutAction = logoutAction;
    }

    @FXML
    private void handleUserClick() {
        userMenu.show(userSection,
                javafx.geometry.Side.BOTTOM,
                0, 0);
    }
}
