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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
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
    }

    // Boxes

    private void showBoxes() {
        currentLevel = Level.BOXES;
        selectedBox = null;
        selectedDocument = null;

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

        row.setStyle("""
                    -fx-padding: 10 12 10 12;
                    -fx-background-color: white;
                    -fx-border-color: #E2E4E8;
                    -fx-border-width: 0 0 1 0;
                    -fx-cursor: hand;
                """);

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

        String baseStyle = row.getStyle();

        row.setOnMouseEntered(e ->
                row.setStyle(baseStyle + """
                                -fx-background-color: #F1F5F9;
                        """));

        row.setOnMouseExited(e ->
                row.setStyle(baseStyle));

        // Preview ONLY on click
        row.setOnMouseClicked(e ->
                showFilePreview(file, doc));

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
        folder.setOnMouseClicked(e -> showDocuments(box));
        return folder;
    }

    private VBox buildDocumentFolder(Document doc) {
        String subtitle = "Created " + doc.getCreatedAt(); //Change this to number of files mby
        VBox folder = folderCard(
                createIcon(PI_FOLDER),
                "Document #" + doc.getDocumentNumber(),
                subtitle
        );
        folder.setOnMouseClicked(e -> showFiles(doc));
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
        card.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #E2E4E8;
                -fx-border-width: 1;
                -fx-background-radius: 6;
                -fx-border-radius: 6;
                -fx-padding: 14;
                -fx-cursor: hand;
                """);


        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("label-medium");
        titleLabel.setWrapText(true);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("text-helper");

        card.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        String base = card.getStyle();
        card.setOnMouseEntered(e -> card.setStyle(base
                + "-fx-background-color: #F1F5F9;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle(base));

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