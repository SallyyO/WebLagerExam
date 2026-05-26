package dk.easv.weblagerexam;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(
                getClass().getResource("/dk/easv/weblagerexam/CSS/app.css").toExternalForm()
        );

        stage.setTitle("Login");


        //minimum size so user cant make the window too small
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.setScene(scene);
        stage.show();
    }
}
