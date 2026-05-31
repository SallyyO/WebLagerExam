package dk.easv.weblagerexam.util;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.LogManager;
import dk.easv.weblagerexam.bll.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LogoutUtil {

    public static void logout(Stage stage) {
        try {
            User currentUser = SessionManager.getCurrentUser();

            if (currentUser != null) {
                LogManager logManager = new LogManager();
                logManager.logLogout(currentUser.getUsername());
            }


            SessionManager.clearSession();

            // Load login page
            FXMLLoader loader = new FXMLLoader(
                    LogoutUtil.class.getResource("/dk/easv/weblagerexam/login-view.fxml")
            );

            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Add CSS again so styling is re-added (without it, we get the login-page without any styling)
            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            LogoutUtil.class.getResource("/dk/easv/weblagerexam/CSS/app.css")
                    ).toExternalForm()
            );

            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}