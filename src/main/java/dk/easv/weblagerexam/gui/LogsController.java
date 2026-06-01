package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Log;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.LogManager;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.util.LogoutUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogsController {

    @FXML private TableColumn<Log, String> userCol;
    @FXML private TableColumn<Log, String> actionCol;
    @FXML private TableColumn<Log, String> dateCol;
    @FXML private TableColumn<Log, String> descriptionCol;
    @FXML private TableColumn<Log, String> typeCol;
    @FXML private TableColumn<Log, String> levelCol;
    @FXML private TableView<Log> logsTable;
    @FXML private TextField txtSearch;

    @FXML private ComboBox<String> cmbLevel;
    @FXML private ComboBox<String> cmbType;

    private FilteredList<Log> filteredLogs;
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

        userBox.setOnMouseClicked(e -> handleLogout());
        Tooltip.install(userBox, new Tooltip("Click to log out"));

        setupColumns();
        setupFilters();
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

            Platform.runLater(() -> {

                ObservableList<Log> masterData =
                        FXCollections.observableArrayList(logs);

                filteredLogs =
                        new FilteredList<>(masterData, log -> true);

                SortedList<Log> sorted =
                        new SortedList<>(filteredLogs);

                sorted.comparatorProperty().bind(
                        logsTable.comparatorProperty()
                );

                logsTable.setItems(sorted);
            });

        }).start();
    }

    @FXML
    private void onBackClicked(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/admin.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Homepage");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupFilters() {

        cmbLevel.getItems().addAll(
                "All",
                "INFO",
                "WARN",
                "ERROR",
                "FATAL"
        );

        cmbType.getItems().addAll(
                "All",
                "AUDIT",
                "SYSTEM",
                "SECURITY"
        );

        cmbLevel.setValue("All");
        cmbType.setValue("All");

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        cmbLevel.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        cmbType.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {

        if (filteredLogs == null) {
            return;
        }

        String search =
                txtSearch.getText() == null
                        ? ""
                        : txtSearch.getText().toLowerCase();

        String level = cmbLevel.getValue();

        String type = cmbType.getValue();

        filteredLogs.setPredicate(log -> {

            boolean matchesSearch = search.isBlank()

                    || log.getUsername().toLowerCase().contains(search)

                    || log.getAction().toLowerCase().contains(search)

                    || log.getDescription().toLowerCase().contains(search);

            boolean matchesLevel =
                    level == null
                            || level.equals("All")
                            || log.getLogLevel().name().equals(level);

            boolean matchesType =
                    type == null
                            || type.equals("All")
                            || log.getLogType().name().equals(type);

            return matchesSearch
                    && matchesLevel
                    && matchesType;
        });
    }

    private void handleLogout() {
        Stage stage =
                (Stage) userBox.getScene().getWindow();

        LogoutUtil.logout(stage);
    }
}

