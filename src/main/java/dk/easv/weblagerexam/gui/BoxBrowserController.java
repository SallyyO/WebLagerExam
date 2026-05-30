package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.*;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.util.LogoutUtil;
import dk.easv.weblagerexam.util.TiffConverter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoxBrowserController {

    @FXML
    private Label lblUsername;
    @FXML
    private Label lblInitials;
    @FXML
    private HBox userBox;

    @FXML
    private FlowPane folderGrid;
    @FXML
    private VBox fileListContainer;
    @FXML
    private HBox breadcrumbBar;
    @FXML
    private Label lblLevelTitle;
    @FXML
    private Label lblItemCount;

    @FXML
    private VBox previewPanel;
    @FXML
    private ImageView previewImage;
    @FXML
    private Label lblPreviewTitle;
    @FXML
    private Label lblPreviewFileNumber;
    @FXML
    private Label lblPreviewDocId;
    @FXML
    private Label lblPreviewType;

    private final DAOManager dao = new DAOManager();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    // Navigation state
    private enum Level {BOXES, DOCUMENTS, FILES}

    private Level currentLevel = Level.BOXES;
    private Box selectedBox;
    private Document selectedDocument;

    // Keyboard navigation state
    private int focusedIndex = -1;

    // Cached item lists for keyboard nav
    private final List<Box> currentBoxes = new ArrayList<>();
    private final List<Document> currentDocuments = new ArrayList<>();
    private final List<File> currentFiles = new ArrayList<>();

    private static final String PI_BOX = "\ue9d9";
    private static final String PI_FOLDER = "\ue963";
    private static final String PI_FILE = "\ue9a8";

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblInitials.setText(user.getInitials());
        }

        Font.loadFont(
                Objects.requireNonNull(
                        getClass().getResource("/dk/easv/weblagerexam/fonts/primeicons.ttf")
                ).toExternalForm(),
                12
        );

        showBoxes();

        userBox.setOnMouseClicked(e ->
                LogoutUtil.logout((Stage) userBox.getScene().getWindow())
        );
        Tooltip.install(userBox, new Tooltip("Click to log out"));

        // Attach keyboard handler once the scene is available
        folderGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                attachKeyboardNavigation(newScene);
            }
        });
    }

    @FXML
    void onInfoBtnClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/boxes_shortcuts.fxml")
            );
            VBox content = loader.load();

            Stage modal = new Stage();
            modal.initOwner(userBox.getScene().getWindow());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setScene(new Scene(content));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Attaches a key-pressed filter to the scene so arrow keys and Enter
     * work regardless of which node currently holds focus.
     */
    private void attachKeyboardNavigation(Scene scene) {
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            if (code == KeyCode.LEFT || code == KeyCode.RIGHT ||
                    code == KeyCode.UP   || code == KeyCode.DOWN) {

                event.consume();

                int size = currentItemCount();
                if (size == 0) return;

                if (currentLevel == Level.BOXES || currentLevel == Level.DOCUMENTS) {
                    // Grid navigation — compute columns from FlowPane width / card width
                    int cols = Math.max(1, computeColumns());
                    focusedIndex = moveInGrid(focusedIndex, code, size, cols);
                } else {
                    // List navigation — up/down only
                    if (code == KeyCode.UP)   focusedIndex = Math.max(0, focusedIndex - 1);
                    if (code == KeyCode.DOWN) focusedIndex = Math.min(size - 1, focusedIndex + 1);
                }

                applyFocusStyle(focusedIndex);

            } else if (code == KeyCode.ENTER) {
                event.consume();
                activateFocusedItem();

            } else if (code == KeyCode.ESCAPE) {
                event.consume();
                if (previewPanel.isVisible()) {
                    closePreview();
                } else {
                    navigateBack();
                }

            } else if (code == KeyCode.BACK_SPACE) {
                event.consume();
                navigateBack();
            }
        });
    }

    /** Returns the total number of items visible at the current level. */
    private int currentItemCount() {
        return switch (currentLevel) {
            case BOXES     -> currentBoxes.size();
            case DOCUMENTS -> currentDocuments.size();
            case FILES     -> currentFiles.size();
        };
    }

    /** Computes how many columns the FlowPane currently shows. */
    private int computeColumns() {
        double paneWidth = folderGrid.getWidth();
        double cardWidth = 160 + folderGrid.getHgap(); // card prefWidth + gap
        return (int) Math.max(1, Math.floor(paneWidth / cardWidth));
    }

    /** Moves focus index in a grid based on arrow key direction. */
    private int moveInGrid(int current, KeyCode code, int size, int cols) {
        if (current < 0) return 0; // select first item if none focused yet
        return switch (code) {
            case LEFT  -> Math.max(0, current - 1);
            case RIGHT -> Math.min(size - 1, current + 1);
            case UP    -> Math.max(0, current - cols);
            case DOWN  -> Math.min(size - 1, current + cols);
            default    -> current;
        };
    }

    /** Activates (clicks) whatever item is currently focused. */
    private void activateFocusedItem() {
        if (focusedIndex < 0) return;
        switch (currentLevel) {
            case BOXES -> {
                if (focusedIndex < currentBoxes.size())
                    showDocuments(currentBoxes.get(focusedIndex));
            }
            case DOCUMENTS -> {
                if (focusedIndex < currentDocuments.size())
                    showFiles(currentDocuments.get(focusedIndex));
            }
            case FILES -> {
                if (focusedIndex < currentFiles.size())
                    showFilePreview(currentFiles.get(focusedIndex), selectedDocument);
            }
        }
    }

    /** Navigates back one level (mirrors the back button). */
    private void navigateBack() {
        switch (currentLevel) {
            case DOCUMENTS -> showBoxes();
            case FILES     -> showDocuments(selectedBox);
            case BOXES     -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/dk/easv/weblagerexam/user.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) folderGrid.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Homepage");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final String FOCUS_STYLE =
            "-fx-border-color: #2D3D4F; -fx-border-width: 2; -fx-border-radius: 6;";

    /**
     * Updates visual focus ring on children of the active container.
     * Works for both FlowPane (grid) and VBox (list).
     */
    private void applyFocusStyle(int newIndex) {
        List<Node> children = currentLevel == Level.FILES
                ? fileListContainer.getChildren()
                : folderGrid.getChildren();

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            // Remove old focus ring from all items
            String base = (String) child.getProperties().getOrDefault("baseStyle", "");
            if (i == newIndex) {
                child.setStyle(base + FOCUS_STYLE);
                child.requestFocus();
                // Ensure the item is scrolled into view if inside a ScrollPane
                child.getParent().layout();
            } else {
                child.setStyle(base);
            }
        }
    }

    /**
     * Resets the focused index and clears any focus ring styles.
     * Called whenever the level changes.
     */
    private void resetFocus() {
        focusedIndex = -1;
    }

    // Boxes

    private void showBoxes() {
        currentLevel = Level.BOXES;
        selectedBox = null;
        selectedDocument = null;
        currentBoxes.clear();
        resetFocus();

        folderGrid.setVisible(true);
        folderGrid.setManaged(true);
        fileListContainer.setVisible(false);
        fileListContainer.setManaged(false);

        lblLevelTitle.setText("Boxes");
        updateBreadcrumb();
        folderGrid.getChildren().clear();

        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        executor.submit(() -> {
            List<Box> boxes = dao.getBoxDAO().getBoxesByUser(user.getId());
            Platform.runLater(() -> {
                currentBoxes.clear();
                currentBoxes.addAll(boxes);
                lblItemCount.setText(boxes.size() + " box" + (boxes.size() == 1 ? "" : "es"));
                if (boxes.isEmpty()) {
                    folderGrid.getChildren().add(emptyState("No boxes yet"));
                    return;
                }
                for (Box box : boxes) {
                    folderGrid.getChildren().add(buildBoxFolder(box));
                }
            });
        });
    }

    // Documents

    private void showDocuments(Box box) {
        currentLevel = Level.DOCUMENTS;
        selectedBox = box;
        selectedDocument = null;
        currentDocuments.clear();
        resetFocus();

        folderGrid.setVisible(true);
        folderGrid.setManaged(true);
        fileListContainer.setVisible(false);
        fileListContainer.setManaged(false);

        String displayId = box.getBoxId() > 0
                ? String.valueOf(box.getBoxId())
                : String.valueOf(box.getId());

        lblLevelTitle.setText("Box #" + displayId);
        updateBreadcrumb();
        folderGrid.getChildren().clear();

        executor.submit(() -> {
            List<Document> docs = dao.getDocumentDAO()
                    .getDocumentsForBox(box.getId());
            Platform.runLater(() -> {
                currentDocuments.clear();
                currentDocuments.addAll(docs);
                lblItemCount.setText(docs.size() + " document"
                        + (docs.size() == 1 ? "" : "s"));
                if (docs.isEmpty()) {
                    folderGrid.getChildren().add(emptyState("No documents in this box"));
                    return;
                }
                for (Document doc : docs) {
                    folderGrid.getChildren().add(buildDocumentFolder(doc));
                }
            });
        });
    }

    // Files

    private void showFiles(Document doc) {
        currentLevel = Level.FILES;
        selectedDocument = doc;
        currentFiles.clear();
        resetFocus();

        lblLevelTitle.setText("Document #" + doc.getDocumentNumber());
        updateBreadcrumb();

        // Switch layouts so files can be shown in a list
        folderGrid.setVisible(false);
        folderGrid.setManaged(false);
        fileListContainer.setVisible(true);
        fileListContainer.setManaged(true);

        fileListContainer.getChildren().clear();

        executor.submit(() -> {
            List<File> files =
                    dao.getDocumentDAO().getFilesForDocument(doc.getId());

            List<HBox> rows = new ArrayList<>();

            for (File file : files) {
                rows.add(buildFileRow(file, doc));
            }

            Platform.runLater(() -> {
                currentFiles.clear();
                currentFiles.addAll(files);
                lblItemCount.setText(
                        files.size() + " file"
                                + (files.size() == 1 ? "" : "s")
                );

                if (rows.isEmpty()) {

                    fileListContainer.getChildren().add(
                            emptyState("No files in this document")
                    );

                    return;
                }

                fileListContainer.getChildren().addAll(rows);
            });
        });
    }

    private HBox buildFileRow(File file, Document doc) {

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        String baseStyle = """
                    -fx-padding: 10 12 10 12;
                    -fx-background-color: white;
                    -fx-border-color: #E2E4E8;
                    -fx-border-width: 0 0 1 0;
                    -fx-cursor: hand;
                """;

        row.setStyle(baseStyle);
        row.getProperties().put("baseStyle", baseStyle);

        Label icon = createIcon(file.isBarcode() ? PI_FILE : PI_FILE);

        Label name = new Label(
                file.isBarcode()
                        ? "Barcode"
                        : "File #" + file.getFileNumber()
        );

        name.getStyleClass().add("label-regular");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label type = new Label(
                file.isBarcode() ? "Barcode" : "File"
        );

        type.getStyleClass().add("text-helper");

        row.getChildren().addAll(icon, name, spacer, type);

        //String baseStyle = row.getStyle();

        row.setOnMouseEntered(e -> {
        if (!row.getStyle().contains(FOCUS_STYLE))
            row.setStyle(baseStyle + "-fx-background-color: #F1F5F9;");
        });

        row.setOnMouseExited(e -> {
            if (!row.getStyle().contains(FOCUS_STYLE))
                row.setStyle(baseStyle);
        });

        // Preview ONLY on click
        row.setOnMouseClicked(e -> {
            // Sync keyboard focus index to clicked item
            focusedIndex = fileListContainer.getChildren().indexOf(row);
            applyFocusStyle(focusedIndex);
            showFilePreview(file, doc);
        });

        return row;
    }


    private Label createIcon(String glyph) {
        Label icon = new Label(glyph);
        icon.getStyleClass().add("pi-icon");
        return icon;
    }


    private VBox buildBoxFolder(Box box) {
        String displayId = box.getBoxId() > 0
                ? String.valueOf(box.getBoxId())
                : String.valueOf(box.getCreatedAt());

        VBox folder = folderCard(
                createIcon(PI_BOX),
                "Box #" + displayId,
                "Created " + box.getCreatedAt()
        );
        folder.setOnMouseClicked(e -> {
            focusedIndex = folderGrid.getChildren().indexOf(folder);
            applyFocusStyle(focusedIndex);
            showDocuments(box);
        });
        return folder;
    }

    private VBox buildDocumentFolder(Document doc) {
        String subtitle = "Created " + doc.getCreatedAt(); //Change this to number of files mby
        VBox folder = folderCard(
                createIcon(PI_FOLDER),
                "Document #" + doc.getDocumentNumber(),
                subtitle
        );
        folder.setOnMouseClicked(e -> {
            focusedIndex = folderGrid.getChildren().indexOf(folder);
            applyFocusStyle(focusedIndex);
            showFiles(doc);
        });
        return folder;
    }

    @FXML
    private void closePreview() {
        previewPanel.setVisible(false);
        previewPanel.setManaged(false);
        previewImage.setImage(null);
    }

    // folder card/template used for boxes and documents
    private VBox folderCard(Label iconLabel, String title, String subtitle) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(160);
        String baseStyle = """
                -fx-background-color: white;
                -fx-border-color: #E2E4E8;
                -fx-border-width: 1;
                -fx-background-radius: 6;
                -fx-border-radius: 6;
                -fx-padding: 14;
                -fx-cursor: hand;
                """;
        card.setStyle(baseStyle);
        card.getProperties().put("baseStyle", baseStyle);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("label-medium");
        titleLabel.setWrapText(true);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("text-helper");

        card.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        card.setOnMouseEntered(e -> {
            if (!card.getStyle().contains(FOCUS_STYLE))
                card.setStyle(baseStyle
                        + "-fx-background-color: #F1F5F9;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        });
        card.setOnMouseExited(e -> {
            if (!card.getStyle().contains(FOCUS_STYLE))
                card.setStyle(baseStyle);
        });

        return card;
    }

    private Label emptyState(String message) {
        Label lbl = new Label(message);
        lbl.getStyleClass().add("text-helper");
        lbl.setStyle("-fx-padding: 24;");
        return lbl;
    }

    // breadcrumbs, try to follow them if you dare

    private void updateBreadcrumb() {
        breadcrumbBar.getChildren().clear();

        // "All Boxes" always present
        Label allBoxes = crumbLabel("All Boxes", currentLevel != Level.BOXES);
        if (currentLevel != Level.BOXES) {
            allBoxes.setOnMouseClicked(e -> showBoxes());
        }
        breadcrumbBar.getChildren().add(allBoxes);

        if (currentLevel == Level.DOCUMENTS || currentLevel == Level.FILES) {
            breadcrumbBar.getChildren().add(crumbSeparator());
            String displayId = selectedBox.getBoxId() > 0
                    ? String.valueOf(selectedBox.getBoxId())
                    : String.valueOf(selectedBox.getId());

            Label boxCrumb = crumbLabel("Box #" + displayId,
                    currentLevel != Level.DOCUMENTS);
            if (currentLevel == Level.FILES) {
                boxCrumb.setOnMouseClicked(e -> showDocuments(selectedBox));
            }
            breadcrumbBar.getChildren().add(boxCrumb);
        }

        if (currentLevel == Level.FILES) {
            breadcrumbBar.getChildren().add(crumbSeparator());
            breadcrumbBar.getChildren().add(
                    crumbLabel("Document #" + selectedDocument.getId(), false));
        }
    }

    private Label crumbLabel(String text, boolean clickable) {
        Label lbl = new Label(text);
        if (clickable) {
            lbl.getStyleClass().add("label-medium");
            lbl.setStyle("-fx-cursor: hand; -fx-text-fill: #2D3D4F;");
            lbl.setOnMouseEntered(e ->
                    lbl.setStyle("-fx-cursor: hand; -fx-text-fill: #415060;"));
            lbl.setOnMouseExited(e ->
                    lbl.setStyle("-fx-cursor: hand; -fx-text-fill: #2D3D4F;"));
        } else {
            lbl.getStyleClass().add("text-helper");
        }
        return lbl;
    }

    private Label crumbSeparator() {
        Label sep = new Label("›");
        sep.getStyleClass().add("text-helper");
        sep.setStyle("-fx-padding: 0 4 0 4;");
        return sep;
    }


    private void showFilePreview(File file, Document doc) {
        // Show the panel immediately with a loading state
        previewPanel.setVisible(true);
        previewPanel.setManaged(true);
        previewImage.setImage(null);
        lblPreviewTitle.setText("Preview");
        lblPreviewFileNumber.setText("Loading...");
        lblPreviewDocId.setText("Document #" + doc.getDocumentNumber());
        lblPreviewType.setText(file.isBarcode() ? "Separator sheet" : "Scanned page");

        executor.submit(() -> {
            // Fetch full image data if not already loaded
            File fullFile = (file.getImageData() != null)
                    ? file
                    : dao.getDocumentDAO().getFileById(file.getId());

            if (fullFile == null || fullFile.getImageData() == null) {
                Platform.runLater(() ->
                        lblPreviewFileNumber.setText("No image data available"));
                return;
            }

            Profile profile = selectedBox.getProfile();

            Image image = TiffConverter.toJavaFXImage(
                    fullFile.getImageData(),
                    profile);

            Platform.runLater(() -> {
                previewImage.setImage(image);
                lblPreviewTitle.setText(file.isBarcode()
                        ? "Barcode / Separator"
                        : "File #" + fullFile.getFileNumber());
                lblPreviewFileNumber.setText("File ID: " + fullFile.getId());
                lblPreviewDocId.setText("Document #" + doc.getDocumentNumber());
                lblPreviewType.setText(file.isBarcode()
                        ? "Separator sheet"
                        : "Scanned page · Page " + fullFile.getFileNumber());
            });
        });
    }

    @FXML
    private void onBackClicked(ActionEvent event) {
        switch (currentLevel) {
            case DOCUMENTS -> showBoxes();
            case FILES -> showDocuments(selectedBox);
            case BOXES -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/dk/easv/weblagerexam/user.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) ((Node) event.getSource())
                            .getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Homepage");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}