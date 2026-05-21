package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.*;
import dk.easv.weblagerexam.bll.DocumentManager;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.dal.DocumentDAO;
import dk.easv.weblagerexam.util.LogoutUtil;
import dk.easv.weblagerexam.util.TiffConverter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class ScanningController{

    @FXML private ImageView mainPreview;
    @FXML private VBox thumbnailContainer;
    @FXML private Button btnStartScan;
    @FXML private Button btnPauseScan;
    @FXML private Button btnStopScan;
    @FXML private Label lblStatus;
    @FXML private ProgressBar progressBar;

    @FXML private Label lblUsername;
    @FXML private Label lblInitials;

    @FXML private ScrollPane mainScrollPane;
    @FXML private StackPane mainImageContainer;

    @FXML private HBox userBox;


    private Box activeBox;
    private Profile activeProfile = null;

    private List<File> currentFiles = new ArrayList<>(); // flat list of all files in view
    private int currentFileIndex = 0; // which file is shown in mainPreview
    private double currentRotation = 0; // current rotation of displayed image

    private VBox dragSource = null;
    private final DocumentManager documentManager = new DocumentManager();
    private final DAOManager dao = new DAOManager();

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

        userBox.setOnMouseClicked(e ->
                LogoutUtil.logout((Stage) userBox.getScene().getWindow())
        );
        Tooltip.install(userBox, new Tooltip("Click to log out"));

        // handle keyboard focus n some shortcuts
        mainScrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case S -> nextFile();
                        case W  -> previousFile();
                        case R     -> rotateCurrentFile(90);
                        case L     -> rotateCurrentFile(-90);
                    }
                });
            }
        });
    }

    // Lets the background thread see changes immediately
    private volatile boolean paused = false;
    private volatile boolean stopped = false;



    @FXML
    public void startScan() {

        Box selectedBox = showPreScanDialog();
        if (selectedBox == null) return; // user canceled
        activeProfile = selectedBox.getProfile();

        activeBox = selectedBox;
        documentManager.setActiveBoxId(activeBox.getId());



        paused  = false;
        stopped = false;

        btnStartScan.setDisable(true);
        btnPauseScan.setDisable(false);
        btnStopScan.setDisable(false);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
        progressBar.setProgress(-1);
        btnPauseScan.setText("Pause");
        lblStatus.setText("Scanning...");

        Task<Void> scanTask = new Task<>() {
            @Override
            protected Void call() throws Exception {

                try {
                    while (!stopped) {

                        while (paused && !stopped) {
                            Thread.sleep(200);
                        }
                        if (stopped) break;

                        File file = dao.getLocalTiffDAO().fetchNext();

                        if(!checkBarcodeAndAlert(file)){
                            stopped = true; // stop the scan loop
                            break;
                        }
                        DocumentManager.ScanResult result = documentManager.processFileScan(file);

                        // Convert image on background thread, we dont want it to do all the heavy lifting here
                        Image image = TiffConverter.toJavaFXImage(
                                file.getImageData(), activeProfile);

                        Platform.runLater(() -> {
                            // Full bar
                            progressBar.setProgress(1.0);

                            // RETURN TO ANIMATED STATE
                            new Thread(() -> {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException ignored) {}

                                Platform.runLater(() ->
                                        progressBar.setProgress(-1));
                            }).start();

                            lblStatus.setText(
                                    "Scanned: "
                                            + documentManager.getTotalScans()
                                            + " file(s) — "
                                            + documentManager.getTotalDocuments()
                                            + " completed document(s)"
                            );
                            // Pass the already-converted image so JavaFX thread aint doing any heavy work
                            loadDocumentWithImage(documentManager.getCurrentDocument(),
                                    file, image);
                        });

                        Thread.sleep(500);
                    }

                } catch (Exception e) {

                    System.err.println("Scan loop stopped due to exception: " + e.getMessage());
                    e.printStackTrace();
                }

                documentManager.finalizeLastDocument();

                Platform.runLater(() -> {
                    List<Document> completed = documentManager.getCompletedDocuments();
                    if (!completed.isEmpty())
                        loadDocument(completed.getLast());

                    lblStatus.setText(
                            (stopped ? "Stopped" : "Done") + " — "
                                    + documentManager.getTotalDocuments() + " document(s), "
                                    + documentManager.getTotalScans() + " total scan(s)"
                    );
                    btnStartScan.setDisable(false);
                    btnPauseScan.setDisable(true);
                    btnStopScan.setDisable(true);

                    // HIDE BAR WHEN FINISHED
                    progressBar.setVisible(false);
                    progressBar.setManaged(false);
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
            // Frozen bar = paused state
            progressBar.setProgress(0);

            lblStatus.setText("Paused — " + documentManager.getTotalScans() + " scanned so far");
        } else {
            btnPauseScan.setText("Pause");
            // Back to animated scanning state
            progressBar.setProgress(-1);
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
        currentFiles = new ArrayList<>(document.getFiles());
        currentFileIndex = 0;
        currentRotation = 0;

        for (int i = 0; i < currentFiles.size(); i++) {
            File file = currentFiles.get(i);
            Image image = TiffConverter.toJavaFXImage(file.getImageData(), activeProfile);
            if (image == null) image = new WritableImage(120, 160);
            addThumbnail(document, file, image, i); // pass index
        }

        if (!currentFiles.isEmpty()) {
            Image first = TiffConverter.toJavaFXImage(
                    currentFiles.getFirst().getImageData(), activeProfile);
            mainPreview.setImage(first);
            mainPreview.setRotate(0);
        }

        // Request focus so keyboard events work immediately
        mainScrollPane.requestFocus();
    }

    private void addThumbnail(Document document, File file, Image image, int index) {
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

        // Update click handler to also track the current index
        thumbBox.setOnMouseClicked(e -> {
            currentFileIndex = index;
            currentRotation = 0;
            mainPreview.setImage(image);
            mainPreview.setRotate(0);
            highlightThumbnail(index);
        });

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

    private void loadDocumentWithImage(Document document, File latestFile, Image latestImage) {
        thumbnailContainer.getChildren().clear();

        List<File> files = new ArrayList<>(document.getFiles());
        currentFiles = files; // keep currentFiles in sync

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            Image image = file == latestFile
                    ? latestImage
                    : TiffConverter.toJavaFXImage(file.getImageData(), activeProfile);
            if (image == null) image = new WritableImage(120, 160);
            addThumbnail(document, file, image, i); // pass index
        }

        if (latestImage != null) {
            mainPreview.setImage(latestImage);
            currentFileIndex = files.indexOf(latestFile);
        }
    }

    public void resumeWithBox(Box box) {
        activeBox = box;
        activeProfile = box.getProfile(); // may be null if profile not loaded
        documentManager.setActiveBoxId(box.getId());
        lblStatus.setText("Resuming scan for Box #" + box.getBoxId() + "...");
    }

    private void nextFile() {
        if (currentFiles.isEmpty()) return;
        currentFileIndex = (currentFileIndex + 1) % currentFiles.size();
        showFileAtIndex(currentFileIndex);
    }

    private void previousFile() {
        if (currentFiles.isEmpty()) return;
        currentFileIndex = (currentFileIndex - 1 + currentFiles.size()) % currentFiles.size();
        showFileAtIndex(currentFileIndex);
    }

    private void showFileAtIndex(int index) {
        if (index < 0 || index >= currentFiles.size()) return;

        File file = currentFiles.get(index);
        currentRotation = 0; // reset rotation when switching files

        Image image = TiffConverter.toJavaFXImage(file.getImageData(), activeProfile);
        if (image != null) {
            mainPreview.setImage(image);
            mainPreview.setRotate(currentRotation);
        }

        // Highlight the matching thumbnail
        highlightThumbnail(index);

        lblStatus.setText("File " + (index + 1) + " of " + currentFiles.size()
                + " — press W/S to navigate between files, R/L to rotate");
    }

    private void rotateCurrentFile(double degrees) {
        currentRotation = (currentRotation + degrees) % 360;
        mainPreview.setRotate(currentRotation);
    }

    private void highlightThumbnail(int index) {
        for (int i = 0; i < thumbnailContainer.getChildren().size(); i++) {
            if (thumbnailContainer.getChildren().get(i) instanceof VBox thumbBox) {
                if (i == index) {
                    // Selected thumbnail — primary border
                    thumbBox.setStyle("""
                    -fx-padding: 6;
                    -fx-background-color: #F1F5F9;
                    -fx-border-color: #2D3D4F;
                    -fx-border-width: 2;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                """);
                } else {
                    // Restore default — you'll need to know if it's a barcode
                    // Get the file to check isBarcode
                    if (i < currentFiles.size()) {
                        File f = currentFiles.get(i);
                        thumbBox.setStyle(f.isBarcode()
                                ? """
                            -fx-padding: 6;
                            -fx-background-color: #FFF5F5;
                            -fx-border-color: #2D3D4F;
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
                          """);
                    }
                }
            }
        }
    }

    @FXML
    private void onBackClicked(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/user.fxml")
            );

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

    // GUI method to check if barcode has been scanned
    private boolean checkBarcodeAndAlert(File file) throws InterruptedException {
        if(!file.isBarcode()) return true; // not a barcode,continue normally

        String barcodeValue = file.getBarcodeValue();
        if(barcodeValue == null || barcodeValue.isBlank()) return true;

        // Check if barcode already exists in DB
        if(documentManager.isBarcodeAlreadyScanned(barcodeValue)) return true;

        CountDownLatch latch = new CountDownLatch(1); // Thread 1 is scanning, 2 shows the popup
        boolean[] shouldContinue = {true}; // we have this array because later we can modify it inside the lambda expression Platform.later()


        Platform.runLater(()->{
            try{
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/dk/easv/weblagerexam/BarcodeAlert.fxml")));
                Parent root = loader.load();

                BarcodeAlertController controller = loader.getController();
                controller.setBarcodeValue(barcodeValue);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Duplicate Barcode");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                shouldContinue[0] = controller.isContinueScanning();

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown(); // signal background thread to continue count goes from 1 to 0
            }
        });
    latch.await(); // background thread waits here until user closes popup
    return shouldContinue[0]; // resumes or stop scanning
}
}