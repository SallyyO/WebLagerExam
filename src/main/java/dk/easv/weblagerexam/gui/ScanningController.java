package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.*;
import dk.easv.weblagerexam.bll.DocumentManager;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.dal.DocumentDAO;
import dk.easv.weblagerexam.dal.LocalTiffDAO;
import dk.easv.weblagerexam.util.TiffConverter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScanningController{

    @FXML private ImageView mainPreview;
    @FXML private VBox thumbnailContainer;
    @FXML private Button btnStartScan;
    @FXML private Button btnPauseScan;
    @FXML private Button btnStopScan;
    @FXML private Label lblStatus;

    @FXML private Label lblUsername;
    @FXML private Label lblInitials;

    @FXML private ScrollPane mainScrollPane;
    @FXML private StackPane mainImageContainer;

    private Box activeBox;
    private Profile activeProfile = null;

    private double zoomFactor = 1.0;
    private static final double ZOOM_MIN = 0.3;
    private static final double ZOOM_MAX = 4.0;
    private static final double ZOOM_STEP = 0.1;

    @FXML
    public void initialize() {

        User user = SessionManager.getCurrentUser();

        if (user != null) {

            lblUsername.setText(
                    user.getUsername()
            );

            lblInitials.setText(
                    user.getInitials()
            );
        }

        //ctrl + scroll to zoom
        mainScrollPane.setOnScroll(event -> {
            if (event.isControlDown()) {
                event.consume();
                double delta = event.getDeltaY() > 0 ? ZOOM_STEP : -ZOOM_STEP;
                zoomFactor = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoomFactor + delta));
                mainPreview.setFitWidth(800 * zoomFactor);
                mainPreview.setFitHeight(700 * zoomFactor);
            }
        });
    }

    private VBox dragSource = null;
    private final DocumentManager documentManager = new DocumentManager();
    private final LocalTiffDAO localTiffDAO = new LocalTiffDAO();

    // Lets the background thread see changes immediately
    private volatile boolean paused = false;
    private volatile boolean stopped = false;


    // scanning controls

    @FXML
    public void startScan() {

       /* Removed for now to look at the scanning without all this stuff
       // Show the prescan stuff first
        Box selectedBox = showPreScanDialog();
        if (selectedBox == null) return; // user canceled
        activeProfile = selectedBox.getProfile();

        activeBox = selectedBox;
        System.out.println(
                "Starting scan for Box #" + activeBox.getId()
        );

        */

        paused  = false;
        stopped = false;

        btnStartScan.setDisable(true);
        btnPauseScan.setDisable(false);
        btnStopScan.setDisable(false);
        btnPauseScan.setText("Pause");
        lblStatus.setText("Scanning...");

        Task<Void> scanTask = new Task<>() {
            @Override
            protected Void call() throws Exception {

                while (!stopped) {  // run until user stops

                    while (paused && !stopped) {
                        Thread.sleep(200);
                    }
                    if (stopped) break;

                    File file = localTiffDAO.fetchNext();

                    DocumentManager.ScanResult result =
                            documentManager.processFileScan(file);

                    Platform.runLater(() -> {
                        lblStatus.setText(
                                "Scanned: " + documentManager.getTotalScans()
                                        + " file(s) — "
                                        + documentManager.getTotalDocuments()
                                        + " completed document(s)"
                        );

                        if (result == DocumentManager.ScanResult.BARCODE) {
                            // A barcode was just scanned — it's now page 1 of the
                            // NEW current document. Show that new (barcode-only) doc.
                            loadDocument(documentManager.getCurrentDocument());
                        } else {
                            // Normal page added — show live current document
                            loadDocument(documentManager.getCurrentDocument());
                        }
                    });

                    Thread.sleep(500);
                }

                // User clicked stop — save whatever is being built
                documentManager.finalizeLastDocument();

                Platform.runLater(() -> {
                    List<Document> completed = documentManager.getCompletedDocuments();
                    if (!completed.isEmpty())
                        loadDocument(completed.getLast());

                    lblStatus.setText(
                            "Stopped — "
                                    + documentManager.getTotalDocuments() + " document(s), "
                                    + documentManager.getTotalScans() + " total scan(s)"
                    );
                    btnStartScan.setDisable(false);
                    btnPauseScan.setDisable(true);
                    btnStopScan.setDisable(true);
                });

                return null;
            }
        };

        new Thread(scanTask).start();
    }

    /* for the api
    @FXML
    public void startScan() {
        paused  = false;
        stopped = false;

        btnStartScan.setDisable(true);
        btnPauseScan.setDisable(false);
        btnStopScan.setDisable(false);
        btnPauseScan.setText("Pause");
        lblStatus.setText("Scanning...");

        Task<Void> scanTask = new Task<>() {
            @Override
            protected Void call() throws Exception {

                while (documentManager.hasMore() && !stopped) {

                    // Pause loop — keeps checking until unpaused or stopped
                    while (paused && !stopped) {
                        Thread.sleep(200);
                    }
                    if (stopped) break;

                    DocumentManager.ScanResult result =
                            documentManager.processNextScan();

                    Platform.runLater(() -> {
                        int scans = documentManager.getTotalScans();
                        int docs  = documentManager.getTotalDocuments();
                        lblStatus.setText(
                                "Scanned: " + scans + " file(s) — "
                                        + docs + " completed document(s)"
                        );

                        if (result == DocumentManager.ScanResult.BARCODE) {
                            List<Document> completed =
                                    documentManager.getCompletedDocuments();
                            if (!completed.isEmpty())
                                loadDocument(completed.getLast());
                        } else {
                            loadDocument(documentManager.getCurrentDocument());
                        }
                    });

                    Thread.sleep(300);
                }

                // Finalize whatever is left unless user hard-stopped
                if (!stopped) {
                    documentManager.finalizeLastDocument();
                }

                Platform.runLater(() -> {
                    List<Document> completed =
                            documentManager.getCompletedDocuments();
                    if (!completed.isEmpty())
                        loadDocument(completed.getLast());

                    String reason = stopped ? "Stopped" : "Done";
                    lblStatus.setText(
                            reason + " — " + documentManager.getTotalDocuments()
                                    + " document(s), "
                                    + documentManager.getTotalScans() + " total scan(s)"
                    );

                    btnStartScan.setDisable(false);
                    btnPauseScan.setDisable(true);
                    btnStopScan.setDisable(true);
                });

                return null;
            }
        };

        new Thread(scanTask).start();
    }

     */

    @FXML
    public void pauseScan() {
        paused = !paused;
        if (paused) {
            btnPauseScan.setText("Resume");
            lblStatus.setText("Paused — " + documentManager.getTotalScans() + " scanned so far");
        } else {
            btnPauseScan.setText("Pause");
            lblStatus.setText("Resuming...");
        }
    }

    @FXML
    public void stopScan() {
        stopped = true;
        paused  = false; // unblock the pause loop so thread can exit
        lblStatus.setText("Stopping...");
        btnPauseScan.setDisable(true);
        btnStopScan.setDisable(true);
    }

    // Shows the files

    public void loadDocument(Document document) {
        thumbnailContainer.getChildren().clear();

        List<File> files = new ArrayList<>(document.getFiles());
        for (File file : files) {
            Image image = TiffConverter.toJavaFXImage(file.getImageData(), activeProfile);
            if (image == null) image = new WritableImage(120, 160);
            addThumbnail(document, file, image);
        }

        if (!document.getFiles().isEmpty()) {
            Image first = TiffConverter.toJavaFXImage(
                    document.getFiles().getFirst().getImageData(), activeProfile);
            mainPreview.setImage(first);
        }
    }

    private void addThumbnail(Document document, File file, Image image) {
        ImageView thumb = new ImageView(image);
        thumb.setFitWidth(120);
        thumb.setFitHeight(150);
        thumb.setPreserveRatio(true);

        Label pageLabel = new Label("Page " + file.getFileNumber());

        pageLabel.setStyle("""
        -fx-font-family: 'Montserrat';
        -fx-font-size: 10px;
        -fx-font-weight: 400;
        -fx-text-fill: #64748B;
    """);

        String baseStyle = file.isBarcode()
                ? """
                -fx-padding: 6;
                -fx-background-color: #FFF5F5;
                -fx-border-color: #64748B;
                -fx-border-width: 1;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                -fx-cursor: hand;
              """
                : """
                -fx-padding: 6;
                -fx-background-color: #FFFFFF;
                -fx-border-color: #E2E4E8;
                -fx-border-width: 1;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                -fx-cursor: hand;
              """;

        String hoverStyle = file.isBarcode()
                ? """
                -fx-padding: 6;
                -fx-background-color: #FFF5F5;
                -fx-border-color: #64748B;
                -fx-border-width: 1;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 2);
              """
                : """
                -fx-padding: 6;
                -fx-background-color: #FFFFFF;
                -fx-border-color: #E2E4E8;
                -fx-border-width: 1;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 2);
              """;

        // Drag-over state: primary color border #2D3D4F
        String dragOverStyle = """
                -fx-padding: 6;
                -fx-background-color: #F1F5F9;
                -fx-border-color: #2D3D4F;
                -fx-border-width: 2;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                -fx-cursor: hand;
              """;

        VBox thumbBox = new VBox(6, thumb, pageLabel);
        thumbBox.setStyle(baseStyle);

        // Hover effect
        thumbBox.setOnMouseEntered(e -> thumbBox.setStyle(hoverStyle));
        thumbBox.setOnMouseExited(e  -> thumbBox.setStyle(baseStyle));

        // Click → show in main preview
        thumbBox.setOnMouseClicked(e -> mainPreview.setImage(image));

        // drag & drop to change the order of the files

        thumbBox.setOnDragDetected(event -> {
            dragSource = thumbBox;
            Dragboard db = thumbBox.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(document.getFiles().indexOf(file)));
            db.setContent(content);
            // 60% opacity = disabled state per style guide
            thumbBox.setOpacity(0.6);
            event.consume();
        });

        thumbBox.setOnDragOver(event -> {
            if (event.getGestureSource() != thumbBox
                    && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                thumbBox.setStyle(dragOverStyle);
            }
            event.consume();
        });

        thumbBox.setOnDragExited(event -> {
            thumbBox.setStyle(baseStyle);
            event.consume();
        });

        thumbBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int oldIndex = Integer.parseInt(db.getString());
                int newIndex = thumbnailContainer.getChildren().indexOf(thumbBox);
                document.reorderFiles(oldIndex, newIndex);
                new DocumentDAO().updateFileOrder(document);
                loadDocument(document);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        thumbBox.setOnDragDone(event -> {
            thumbBox.setOpacity(1.0);
            dragSource = null;
            event.consume();
        });

        thumbnailContainer.getChildren().add(thumbBox);
    }

    private Box showPreScanDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/dk/easv/weblagerexam/prescan.fxml")));

            Parent root = loader.load();
            PreScanController dialogController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Start Scanning");
            dialog.initModality(Modality.APPLICATION_MODAL); // blocks the main window
            dialog.initOwner(btnStartScan.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait(); // block until dialog closes

            if (dialogController.isConfirmed()) {
                return dialogController.getResultBox();
            }
            return null; // cancelled

        } catch (Exception e) {
            throw new RuntimeException("Could not open pre-scan dialog", e);
        }
    }
}