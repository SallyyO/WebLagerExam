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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogsController {

    @FXML private TableColumn<Log, String> userCol;
    @FXML private TableColumn<Log, String> actionCol;
    @FXML private TableColumn<Log, String> dateCol;
    @FXML private TableColumn<Log, String> descriptionCol;
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
    }

    private void loadLogs() {
        new Thread(() -> {
            List<Log> logs = logManager.getAllLogs();
            Platform.runLater(() ->
                    logsTable.setItems(FXCollections.observableArrayList(logs)));
        }).start();
    }
}

