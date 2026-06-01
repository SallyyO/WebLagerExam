package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Client;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.*;
import dk.easv.weblagerexam.util.LogoutUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminController {

    @FXML
    private Label lblUsername;
    @FXML
    private Label lblInitials;
    @FXML
    private Button addNewBtn;
    @FXML
    private Button usersButton;
    @FXML
    private Button profilesButton;
    @FXML
    private Button clientsButton;
    @FXML
    private Button logsButton;
    @FXML
    private TableView<Object> mainTable;
    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<String> statusFilterCombo;
    @FXML
    private HBox userBox;

    @FXML
    private Button deleteUserBtn;
    @FXML
    private Button editUserBtn;
    @FXML
    private Button fileInfoButton;
    @FXML
    private Button historyButton;

    private UserManager userManager = new UserManager();
    private ProfileManager profileManager = new ProfileManager();
    private ClientManager clientManager = new ClientManager();
    LogManager logManager = new LogManager();

    private final ObservableList<Object> masterList = FXCollections.observableArrayList();
    private final FilteredList<Object> filteredList = new FilteredList<>(masterList, p -> true);

    private enum AdminInfo {
        USERS,
        PROFILES,
        CLIENTS
    }

    private static final String PI_EDIT = "\ue942";
    private static final String PI_DELETE = "\ue93d";
    private static final String PI_ADD = "\ue93f";

    private AdminInfo currentInfo = AdminInfo.USERS;

    //Shortcuts
    private final EventHandler<KeyEvent> keyHandler = event -> {
        switch (event.getCode()) {
            case A -> {
                onAddUserBtnClicked(new ActionEvent());
                event.consume();
            }
            case P -> {
                onProfilesClicked(new ActionEvent());
                event.consume();
            }
            case C -> {
                onClientsClicked(new ActionEvent());
                event.consume();
            }
            case L -> {
                onLogsClicked(new ActionEvent());
                event.consume();
            }
            case U -> {
                try {
                    onUsersClicked(new ActionEvent());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                event.consume();
            }
        }
    };

    @FXML
    public void initialize() {

        User user = SessionManager.getCurrentUser();

        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblInitials.setText(user.getInitials());
        }

        mainTable.setItems(filteredList);

        loadUsers();


        userBox.setOnMouseClicked(e -> handleLogout());
        Tooltip.install(userBox, new Tooltip("Click to log out"));

        addNewBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.A) {
                        onAddUserBtnClicked(new ActionEvent());
                        event.consume();
                    }

                });
            }
        });

        Platform.runLater(() -> {
            mainTable.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        });

        statusFilterCombo.getItems().addAll(
                "Active",
                "Inactive",
                "All"
        );

        statusFilterCombo.setValue("Active");

        statusFilterCombo.valueProperty().addListener(
                (obs, oldVal, newVal) -> applyFilters()
        );

        searchTextField.textProperty().addListener(
                (obs, oldVal, newVal) -> applyFilters()
        );
    }

    private void loadUsers() {
        try {

            currentInfo = AdminInfo.USERS;
            mainTable.getColumns().clear();

            Label nameHeader = new Label("Name");
            nameHeader.getStyleClass().add("text-h2");
            TableColumn<Object, String> nameCol = new TableColumn<>();
            nameCol.setGraphic(nameHeader);

            nameCol.setCellValueFactory(cell -> {
                User user = (User) cell.getValue();
                return new SimpleStringProperty(user.getUsername());
            });

            Label statusHeader = new Label("Status");
            statusHeader.getStyleClass().add("text-h2");

            TableColumn<Object, String> statusCol =
                    new TableColumn<>();

            statusCol.setGraphic(statusHeader);

            statusCol.setCellValueFactory(cell -> {
                User user = (User) cell.getValue();
                return new SimpleStringProperty(user.isActive()
                        ? "Active"
                        : "Inactive"
                );
            });

            Label actionsHeader = new Label("Actions");
            actionsHeader.getStyleClass().add("text-h2");

            TableColumn<Object, Void> actionsCol =
                    createUserActionsColumn();

            actionsCol.setGraphic(actionsHeader);

            mainTable.getColumns().addAll(
                    nameCol,
                    statusCol,
                    actionsCol
            );

            masterList.setAll(
                    userManager.getAllUsers()
            );
            statusFilterCombo.setDisable(false);
            statusFilterCombo.setValue("Active");

            applyFilters();

            mainTable.refresh();

            addNewBtn.setVisible(true);
            addNewBtn.setManaged(true);
            addNewBtn.setText("Add User [ A ]");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load users",
                    e.getMessage());
        }
    }

    private TableColumn<Object, Void> createUserActionsColumn() {
        TableColumn<Object, Void> col = new TableColumn<>();

        col.setCellFactory(param ->
                new TableCell<>() {
                    private final Button editBtn = new Button("Edit");

                    private final Button statusBtn = new Button();
                    private final HBox box =
                            new HBox(8,
                                    editBtn,
                                    statusBtn);

                    {

                        editBtn.getStyleClass().addAll(
                                "button-primary",
                                "text-button"
                        );

                        statusBtn.getStyleClass().addAll(
                                "button-danger",
                                "text-button"
                        );

                        editBtn.setOnAction(e -> {
                            User user = (User) getTableView().getItems().get(getIndex());
                            handleEditUser(user);
                        });

                        statusBtn.setOnAction(e -> {
                            User user = (User) getTableView().getItems().get(getIndex());
                            userManager.setUserActive(user.getId(), !user.isActive()
                            );

                            loadUsers();
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            return;
                        }
                        User user = (User) getTableView().getItems().get(getIndex());

                        statusBtn.getStyleClass().removeAll(
                                "button-danger",
                                "button-reactivate"
                        );

                        if (user.isActive()) {

                            statusBtn.setText("Deactivate");
                            statusBtn.getStyleClass().add("button-danger");

                        } else {

                            statusBtn.setText("Activate");
                            statusBtn.getStyleClass().add("button-reactivate");
                        }

                        setGraphic(box);
                    }
                });
        return col;
    }


    @FXML
    void onAddUserBtnClicked(ActionEvent event) {

        switch (currentInfo) {

            case USERS -> openAddUser();

            case PROFILES -> openAddProfile();

            case CLIENTS -> openAddClient();
        }
    }


    private void handleEditUser(User selectedUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/weblagerexam/addNewUser.fxml"));
            Parent root = loader.load();

            AddNewUserController controller = loader.getController();
            controller.setUser(selectedUser);

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open Edit User Window", e.getMessage());
        }
    }


    private void loadProfiles() {

        try {
            currentInfo = AdminInfo.PROFILES;
            mainTable.getColumns().clear();

            Label nameHeader = new Label("Name");
            nameHeader.getStyleClass().add("text-h2");

            TableColumn<Object, String> nameCol = new TableColumn<>();
            nameCol.setGraphic(nameHeader);


            nameCol.setCellValueFactory(cell -> {
                Profile profile =
                        (Profile) cell.getValue();
                return new SimpleStringProperty(
                        profile.getName()
                );
            });

            Label clientHeader = new Label("Client");
            clientHeader.getStyleClass().add("text-h2");

            TableColumn<Object, String> clientCol = new TableColumn<>();
            clientCol.setGraphic(clientHeader);


            clientCol.setCellValueFactory(cell -> {
                Profile profile = (Profile) cell.getValue();
                Client client = clientManager.getClientById(profile.getClientId());

                String name = client != null ? client.getName()
                        : "No Client";
                return new SimpleStringProperty(name);
            });

            mainTable.getColumns().addAll(
                    nameCol,
                    clientCol
            );

            masterList.setAll(profileManager.getAllProfiles());
            statusFilterCombo.setDisable(true);
            statusFilterCombo.setValue("All");

            applyFilters();

            addNewBtn.setVisible(true);
            addNewBtn.setManaged(true);
            addNewBtn.setText("Add Profile");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load profiles",
                    e.getMessage());
        }
    }

   /* Not sure which actions a profile would need
   private TableColumn<Object, Void> createProfileActionsColumn() {
        TableColumn<Object, Void> col =
                new TableColumn<>();

        col.setCellFactory(param ->
                new TableCell<>() {

                    private final Button editBtn = new Button("Edit");

                    private final Button assignBtn = new Button("+");

                    private final HBox box = new HBox(8,
                                    editBtn,
                                    assignBtn);

                    {

                        editBtn.getStyleClass().addAll(
                                "button-primary",
                                "text-button"
                        );

                        assignBtn.getStyleClass().addAll("button-secondary", "text-button");

                        editBtn.setOnAction(e -> {
                            Profile profile = (Profile) getTableView()
                                            .getItems()
                                            .get(getIndex());
                            //handleEditProfile(profile);
                        });

                        assignBtn.setOnAction(e -> {

                            Profile profile = (Profile) getTableView()
                                            .getItems()
                                            .get(getIndex());
                            // handleAssignProfile(profile);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(
                                empty ? null : box
                        );
                    }
                });

        return col;
    }
    */

    @FXML
    private void onClientsClicked(ActionEvent event) {
        loadClients();
    }

    private void loadClients() {
        try {
            currentInfo = AdminInfo.CLIENTS;
            mainTable.getColumns().clear();

            Label nameHeader = new Label("Name");
            nameHeader.getStyleClass().add("text-h2");

            TableColumn<Object, String> nameCol = new TableColumn<>();
            nameCol.setGraphic(nameHeader);

            nameCol.setCellValueFactory(cell -> {
                Client client = (Client) cell.getValue();
                return new SimpleStringProperty(client.getName()
                );
            });

            Label statusHeader = new Label("Status");
            statusHeader.getStyleClass().add("text-h2");
            TableColumn<Object, String> statusCol = new TableColumn<>();
            statusCol.setGraphic(statusHeader);

            statusCol.setCellValueFactory(cell -> {
                Client client = (Client) cell.getValue();

                return new SimpleStringProperty(
                        client.isActive()
                                ? "Active"
                                : "Inactive"
                );
            });

            Label profilesHeader = new Label("Profiles");
            profilesHeader.getStyleClass().add("text-h2");

            TableColumn<Object, String> profilesCol = new TableColumn<>();
            profilesCol.setGraphic(profilesHeader);

            profilesCol.setCellValueFactory(cell -> {

                Client client = (Client) cell.getValue();

                List<Profile> profiles = profileManager.getProfilesForClient(client.getId());

                if (profiles.isEmpty()) {
                    return new SimpleStringProperty("No Profiles");
                }

                String names = profiles.stream()
                        .map(Profile::getName)
                        .limit(3)
                        .collect(Collectors.joining(", "));

                if (profiles.size() > 3) {
                    names += " +" + (profiles.size() - 3) + " more";
                }

                return new SimpleStringProperty(names);
            });

            Label actionsHeader = new Label("Actions");
            actionsHeader.getStyleClass().add("text-h2");

            TableColumn<Object, Void> actionsCol = createClientActionsColumn();
            actionsCol.setGraphic(actionsHeader);

            mainTable.getColumns().addAll(
                    nameCol,
                    statusCol,
                    profilesCol,
                    actionsCol
            );

            masterList.setAll(clientManager.getAllClients());
            statusFilterCombo.setDisable(false);
            statusFilterCombo.setValue("Active");

            applyFilters();

            addNewBtn.setVisible(true);
            addNewBtn.setManaged(true);
            addNewBtn.setText("Add Client");

        } catch (Exception e) {
            e.printStackTrace();
            showError(
                    "Could not load clients",
                    e.getMessage()
            );
        }
    }

    private TableColumn<Object, Void> createClientActionsColumn() {
        TableColumn<Object, Void> col = new TableColumn<>();

        col.setCellFactory(param ->
                new TableCell<>() {
                    private final Button editBtn =
                            new Button("Edit");

                    private final Button statusBtn = new Button();

                    private final HBox box =
                            new HBox(8, editBtn, statusBtn);

                    {

                        editBtn.getStyleClass().addAll(
                                "button-primary",
                                "text-button"
                        );

                        statusBtn.getStyleClass().addAll(
                                "button-danger",
                                "text-button"
                        );

                        editBtn.setOnAction(e -> {

                            Client client =
                                    (Client) getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleEditClient(client);
                        });

                        statusBtn.setOnAction(e -> {

                            Client client =
                                    (Client) getTableView()
                                            .getItems()
                                            .get(getIndex());

                            clientManager.setClientActive(
                                    client.getId(),
                                    !client.isActive()
                            );

                            loadClients();
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {

                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                            return;
                        }

                        Client client =
                                (Client) getTableView()
                                        .getItems()
                                        .get(getIndex());

                        statusBtn.getStyleClass().removeAll(
                                "button-danger",
                                "button-reactivate"
                        );

                        if (client.isActive()) {

                            statusBtn.setText("Deactivate");
                            statusBtn.getStyleClass().add("button-danger");

                        } else {

                            statusBtn.setText("Activate");
                            statusBtn.getStyleClass().add("button-reactivate");
                        }

                        setGraphic(box);
                    }
                });

        return col;
    }

    private void filterTable(String searchText) {

        if (searchText == null || searchText.isBlank()) {
            filteredList.setPredicate(p -> true);
            return;
        }
        String lowercase = searchText.toLowerCase();

        filteredList.setPredicate(item -> {
            if (item instanceof User user) {
                return user.getUsername() != null && user.getUsername()
                        .toLowerCase()
                        .contains(lowercase);
            }

            if (item instanceof Profile profile) {
                return profile.getName() != null && profile.getName()
                        .toLowerCase()
                        .contains(lowercase);
            }

            if (item instanceof Client client) {
                return client.getName() != null && client.getName()
                        .toLowerCase()
                        .contains(lowercase);
            }
            return false;
        });
    }

    @FXML
    private void onUsersClicked(ActionEvent event) {
        loadUsers();
    }

    @FXML
    private void onProfilesClicked(ActionEvent event) {
        loadProfiles();
    }

    public void refreshCurrentInfo() {
        switch (currentInfo) {
            case USERS -> loadUsers();
            case PROFILES -> loadProfiles();
            case CLIENTS -> loadClients();
        }
    }

    private Label createIcon(String glyph) {
        Label icon = new Label(glyph);
        icon.getStyleClass().add("pi-icon");
        return icon;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    private void onLogsClicked(ActionEvent event) {

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/weblagerexam/logs.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) profilesButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("System Logs");
                stage.centerOnScreen();


            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // To switch what the add button opens:
    private void openAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/addNewUser.fxml")
            );
            Parent root = loader.load();


            Stage stage = new Stage();
            stage.setTitle("Add User");
            stage.setScene(new Scene(root));
            AddNewUserController controller = loader.getController();
            controller.setAdminController(this);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAddProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/newProfile.fxml")
            );
            Parent root = loader.load();


            Stage stage = new Stage();
            stage.setTitle("Add Profile");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProfiles();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAddClient() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/dk/easv/weblagerexam/AddOrEditClient.fxml"
                    )
            );

            Parent root = loader.load();

            AddOrEditClientController controller = loader.getController();
            controller.gotANewClient();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadClients();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEditClient(Client client) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/dk/easv/weblagerexam/AddOrEditClient.fxml"
                    )
            );

            Parent root = loader.load();

            AddOrEditClientController controller = loader.getController();
            controller.setClient(client);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadClients();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onInfoBtnClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/admin_shortcuts.fxml")
            );
            VBox content = loader.load();

            Stage modal = new Stage();
            modal.initOwner(profilesButton.getScene().getWindow());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setScene(new Scene(content));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {

        String search = searchTextField.getText() == null
                ? ""
                : searchTextField.getText().toLowerCase();

        String status = statusFilterCombo.getValue();

        filteredList.setPredicate(item -> {

            // SEARCH

            boolean matchesSearch = true;

            if (!search.isBlank()) {

                if (item instanceof User user) {

                    matchesSearch =
                            user.getUsername() != null
                                    && user.getUsername()
                                    .toLowerCase()
                                    .contains(search);

                } else if (item instanceof Profile profile) {

                    matchesSearch =
                            profile.getName() != null
                                    && profile.getName()
                                    .toLowerCase()
                                    .contains(search);

                } else if (item instanceof Client client) {

                    matchesSearch =
                            client.getName() != null
                                    && client.getName()
                                    .toLowerCase()
                                    .contains(search);
                }
            }

            // STATUS(active/inactive) FILTER

            boolean matchesStatus = true;

            if (item instanceof User user) {
                matchesStatus =
                        status.equals("All")
                                || (status.equals("Active") && user.isActive())
                                || (status.equals("Inactive") && !user.isActive());

            } else if (item instanceof Client client) {

                matchesStatus =
                        status.equals("All")
                                || (status.equals("Active") && client.isActive())
                                || (status.equals("Inactive") && !client.isActive());

            }
            return matchesSearch && matchesStatus;
        });
    }

    private void handleLogout() {
        Stage stage =
                (Stage) userBox.getScene().getWindow();

        LogoutUtil.logout(stage);
    }
}