package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.Client;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.bll.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminController {

    @FXML private Label lblUsername;
    @FXML private Label lblInitials;

    @FXML private Button addUserBtn;

    @FXML private Button usersButton;
    @FXML private Button profilesButton;
    @FXML private Button clientsButton;
    @FXML private Button logsButton;

    @FXML private TableView<Object> mainTable;

    @FXML private TextField searchTextField;

    @FXML private HBox userBox;

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

    @FXML
    public void initialize() {

        User user = SessionManager.getCurrentUser();

        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblInitials.setText(user.getInitials());
        }

        mainTable.setItems(filteredList);

        loadUsers();

        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));

        userBox.setOnMouseClicked(e -> handleLogout());
        Tooltip.install(userBox, new Tooltip("Click to log out"));

        addUserBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.A) {
                        onAddUserBtnClicked(new ActionEvent());
                        event.consume();
                    }

                });
            }
        });
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
                User user =
                        (User) cell.getValue();
                return new SimpleStringProperty(
                        user.getUsername()
                );
            });

            Label statusHeader = new Label("Status");
            statusHeader.getStyleClass().add("text-h2");

            TableColumn<Object, String> statusCol =
                    new TableColumn<>();

            statusCol.setGraphic(statusHeader);

            statusCol.setCellValueFactory(cell -> {
                User user =
                        (User) cell.getValue();
                return new SimpleStringProperty(
                        /*user.isActive()
                                ? "Active"
                                : "Inactive"

                         */
                        "Active for now"
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

            mainTable.refresh();

            addUserBtn.setVisible(true);
            addUserBtn.setManaged(true);

            addUserBtn.setText("Add User");

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
                    private final Button editBtn =
                            new Button("Edit");

                    private final Button deleteBtn =
                            new Button("Delete");
                    private final HBox box =
                            new HBox(8,
                                    editBtn,
                                    deleteBtn);
                    {

                        editBtn.getStyleClass().addAll(
                                "button-primary",
                                "text-button"
                        );

                        deleteBtn.getStyleClass().addAll(
                                "button-danger",
                                "text-button"
                        );

                        editBtn.setOnAction(e -> {
                            User user =
                                    (User) getTableView()
                                            .getItems()
                                            .get(getIndex());
                            handleEditUser(user);
                        });

                        deleteBtn.setOnAction(e -> {
                            User user =
                                    (User) getTableView()
                                            .getItems()
                                            .get(getIndex());
                            handleDeleteUser(user);
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(
                                empty ? null : box
                        );
                    }
                });
        return col;
    }


    @FXML
    void onAddUserBtnClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/addNewUser.fxml")
            );
            Parent root = loader.load();


            Stage stage = new Stage();
            stage.setTitle("Add User");
            stage.setScene(new Scene(root));
            AddNewUserController controller = (AddNewUserController) loader.getController();
            controller.setAdminController(this);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteUser(User selectedUser) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setContentText("Are you sure you want to delete " + selectedUser.getUsername() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userManager.deleteUser(selectedUser.getId());
                    loadUsers();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Could not delete User", e.getMessage());
                }
            }
        });
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

                Profile profile =
                        (Profile) cell.getValue();

                Client client =
                        clientManager.getClientById(
                                profile.getClientId()
                        );

                String name =
                        client != null
                                ? client.getName()
                                : "No Client";
                return new SimpleStringProperty(name);
            });

            Label actionsHeader = new Label("Actions");
            actionsHeader.getStyleClass().add("text-h2");

            TableColumn<Object, Void> actionsCol =
                    createProfileActionsColumn();

            actionsCol.setGraphic(actionsHeader);

            mainTable.getColumns().addAll(
                    nameCol,
                    clientCol,
                    actionsCol
            );

            masterList.setAll(
                    profileManager.getAllProfiles()
            );

            addUserBtn.setVisible(false);
            addUserBtn.setManaged(false);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load profiles",
                    e.getMessage());
        }
    }

    private TableColumn<Object, Void> createProfileActionsColumn() {

        TableColumn<Object, Void> col =
                new TableColumn<>();


        col.setCellFactory(param ->
                new TableCell<>() {

                    private final Button editBtn =
                            new Button("Edit");

                    private final Button assignBtn =
                            new Button("+");

                    private final HBox box =
                            new HBox(8,
                                    editBtn,
                                    assignBtn);

                    {

                        editBtn.getStyleClass().addAll(
                                "button-primary",
                                "text-button"
                        );

                        assignBtn.getStyleClass().addAll(
                                "button-secondary",
                                "text-button"
                        );

                        editBtn.setOnAction(e -> {
                            Profile profile =
                                    (Profile) getTableView()
                                            .getItems()
                                            .get(getIndex());
                            //handleEditProfile(profile);
                        });

                        assignBtn.setOnAction(e -> {

                            Profile profile =
                                    (Profile) getTableView()
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

                Client client =
                        (Client) cell.getValue();

                return new SimpleStringProperty(
                        client.getName()
                );
            });

            Label profilesHeader = new Label("Profiles");
            profilesHeader.getStyleClass().add("text-h2");

            TableColumn<Object, String> profilesCol = new TableColumn<>();
            profilesCol.setGraphic(profilesHeader);

            profilesCol.setCellValueFactory(cell -> {

                Client client =
                        (Client) cell.getValue();

                List<Profile> profiles =
                        profileManager.getProfilesForClient(
                                client.getId()
                        );

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

            TableColumn<Object, Void> actionsCol =
                    createClientActionsColumn();

            actionsCol.setGraphic(actionsHeader);

        mainTable.getColumns().addAll(
                nameCol,
                profilesCol,
                actionsCol
        );

        masterList.setAll(
                clientManager.getAllClients()
        );

        addUserBtn.setVisible(true);
        addUserBtn.setManaged(true);

        addUserBtn.setText("Add Client");

    } catch (Exception e) {

        e.printStackTrace();

        showError(
                "Could not load clients",
                e.getMessage()
        );
    }
    }

    private TableColumn<Object, Void>
    createClientActionsColumn() {

        TableColumn<Object, Void> col =
                new TableColumn<>();



        col.setCellFactory(param ->
                new TableCell<>() {

                    private final Button editBtn =
                            new Button("Edit");

                    private final Button deleteBtn =
                            new Button("Delete");

                    private final HBox box =
                            new HBox(8,
                                    editBtn,
                                    deleteBtn);

                    {

                        editBtn.getStyleClass().addAll(
                                "button-primary",
                                "text-button"
                        );

                        deleteBtn.getStyleClass().addAll(
                                "button-secondary",
                                "text-button"
                        );

                        editBtn.setOnAction(e -> {

                            Client client =
                                    (Client) getTableView()
                                            .getItems()
                                            .get(getIndex());

                            //handleEditClient(client);
                        });

                        deleteBtn.setOnAction(e -> {

                            Client client =
                                    (Client) getTableView()
                                            .getItems()
                                            .get(getIndex());

                            //handleDeleteClient(client);
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        setGraphic(
                                empty ? null : box
                        );
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
    /* we need a button for this
    @FXML
    private void openNewProfileDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/dk/easv/weblagerexam/newProfile.fxml")));
            Parent root = loader.load();
            NewProfileController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Create Profile");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(!!!!insert button.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();

            if (controller.isConfirmed()) {
                Profile created = controller.getCreatedProfile();
                System.out.println("Created profile: " + created.getName());
                // Refresh your profile list in the admin UI here
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not open profile dialog", e);
        }
    }
    */

    @FXML
    private void onLogsClicked(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/weblagerexam/logs.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("System Logs");
            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource(
                                    "/dk/easv/weblagerexam/CSS/app.css"
                            )
                    ).toExternalForm()
            );

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open logs",
                    e.getMessage());
        }
    }

    private void handleLogout() {

        try {

            SessionManager.clearSession();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/login-view.fxml")
            );

            Parent root = loader.load();

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource("/dk/easv/weblagerexam/CSS/app.css")
                    ).toExternalForm()
            );

            Stage stage = (Stage) userBox.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}