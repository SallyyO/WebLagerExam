package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Log;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.LogManager;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.util.LogoutUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class LogsController {

    @FXML private TableColumn<Log, String> userCol;
    @FXML private TableColumn<Log, String> actionCol;
    @FXML private TableColumn<Log, String> dateCol;
    @FXML private TableColumn<Log, String> descriptionCol;
    @FXML private TableColumn<Log, String> typeCol;
    @FXML private TableColumn<Log, String> levelCol;
    @FXML private TableView<Log> logsTable;
    @FXML private Label lblUsername;
    @FXML private Label lblInitials;
    @FXML private HBox userBox;

    private final LogManager logManager = new LogManager();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblInitials.setText(user.getInitials());
        }

        userBox.setOnMouseClicked(e ->
                LogoutUtil.logout((Stage) userBox.getScene().getWindow()));
        Tooltip.install(userBox, new Tooltip("Click to log out"));

        setupColumns();
        loadLogs();
    }

    private void setupColumns() {
        userCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername()));

        actionCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAction()));

        descriptionCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDescription()));

        dateCol.setCellValueFactory(data -> {
            LocalDateTime ts = data.getValue().getTimestamp();
            String formatted = ts != null
                    ? ts.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    : "";
            return new SimpleStringProperty(formatted);
        });
        typeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getLogType().name()));

        levelCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getLogLevel().name()));

        levelCol.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            return;
                        }

                        setText(item);

                        switch (item) {

                            case "INFO" ->
                                    setStyle("-fx-text-fill: green;");

                            case "WARN" ->
                                    setStyle("-fx-text-fill: orange;");

                            case "ERROR" ->
                                    setStyle("-fx-text-fill: red;");

                            case "FATAL" ->
                                    setStyle("-fx-text-fill: darkred;");

                            default ->
                                    setStyle("");
                        }
                    }
                });
    }

    private void loadLogs() {
        new Thread(() -> {
            List<Log> logs = logManager.getAllLogs();
            Platform.runLater(() ->
                    logsTable.setItems(FXCollections.observableArrayList(logs)));
        }).start();
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/weblagerexam/gui/admin.fxml"));

            Parent root = loader.load();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource(
                                    "/dk/easv/weblagerexam/CSS/app.css"
                            )
                    ).toExternalForm()
            );

            Stage stage = (Stage) logsTable.getScene().getWindow();

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

