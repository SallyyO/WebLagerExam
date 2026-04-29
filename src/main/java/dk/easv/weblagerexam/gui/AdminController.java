package dk.easv.weblagerexam.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AdminController {

    @FXML
    private Button addUserBtn;

    @FXML
    private Button deleteUserBtn;

    @FXML
    private Button editUserBtn;

    @FXML
    private Button fileInfoButton;

    @FXML
    private Button historyButton;

    @FXML
    private TableColumn<?, ?> idColumn;

    @FXML
    private TableView<?> mainTable;

    @FXML
    private TableColumn<?, ?> nameColumn;

    @FXML
    private TableColumn<?, ?> typeColumn;

    @FXML
    private Button usersButton;

    @FXML
    void onAddUserBtnClicked(ActionEvent event) {

    }

    @FXML
    void onDeleteUserBtnClicked(ActionEvent event) {

    }

    @FXML
    void onEditUserBtnClicked(ActionEvent event) {

    }

    @FXML
    void onFileInfoClicked(ActionEvent event) {

    }

    @FXML
    void onHistoryClicked(ActionEvent event) {

    }

    @FXML
    void onUsersClicked(ActionEvent event) {

    }

}
