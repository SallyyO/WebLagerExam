package dk.easv.weblagerexam.gui;

import dk.easv.weblagerexam.be.*;
import dk.easv.weblagerexam.bll.DocumentManager;
import dk.easv.weblagerexam.bll.SessionManager;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.dal.DocumentDAO;
import dk.easv.weblagerexam.util.LogoutUtil;
import dk.easv.weblagerexam.util.TiffConverter;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

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
    private double currentRotation = 0; // current rotation of the displayed image

    @FXML private VBox treeContainer;
    @FXML private Button btnRefreshTree;
    @FXML private Button btnExport;

    // Tracks which document is currently expanded in the tree
    private final Set<Integer> expandedDocumentIds = new HashSet<>();

    private VBox dragSource = null;
    private final DocumentManager documentManager = new DocumentManager();
    private final DAOManager dao = new DAOManager();

    private final BlockingQueue<File> scanQueue = new LinkedBlockingQueue<>(100); //max 100 scans in the queue
    private Thread processorThread;
    private Thread scannerThread;

    //Shortcuts for outside the scanning process
    private final EventHandler<KeyEvent> keyHandler = event -> {
        switch (event.getCode()) {
            case ENTER -> { startScan(); event.consume(); }
            case P -> { pauseScan(); event.consume(); }
            case S -> { stopScan(); event.consume(); }
            case B -> { onExportClicked(); event.consume(); }
        }
    };

    @FXML
    public void initialize() {

        User user = SessionManager.getCurrentUser();
        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblInitials.setText(user.getInitials());
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
                        case E -> nextFile();
                        case Q  -> previousFile();
                        case R     -> rotateCurrentFile(90);
                        case L     -> rotateCurrentFile(-90);
                    }
                });
            }
        });

        loadTree();
    }

    // Lets the background thread see changes immediately
    private volatile boolean paused = false;
    private volatile boolean stopped = false;



    @FXML
    public void startScan() {

        Box selectedBox = showPreScanDialog();

        if (selectedBox == null) {
            return;}

        activeProfile = selectedBox.getProfile();
        activeBox = selectedBox;
        loadTree();

        documentManager.setActiveBox(activeBox);
        documentManager.setActiveBoxId(activeBox.getId());

        paused = false;
        stopped = false;

        btnStartScan.setDisable(true);
        btnPauseScan.setDisable(false);
        btnStopScan.setDisable(false);

        btnPauseScan.setText("Pause");

        progressBar.setVisible(true);
        progressBar.setManaged(true);
        progressBar.setProgress(-1);

        lblStatus.setText("Scanning...");

        startScannerThread();

        startProcessorThread();
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
            progressBar.setProgress(0);
            lblStatus.setText("Paused — "
                    + documentManager.getTotalScans() + " scanned so far");
        } else {
            btnPauseScan.setText("Pause");
            progressBar.setProgress(-1);
            lblStatus.setText("Resuming...");
        }
    }

    @FXML
    public void stopScan() {

        stopped = true;
        paused = false;

        if (scannerThread != null) {
            scannerThread.interrupt();
        }

        if (processorThread != null) {
            processorThread.interrupt();
        }

        lblStatus.setText("Stopping...");

        btnPauseScan.setDisable(true);
        btnStopScan.setDisable(true);
    }

    private void startScannerThread() {

        scannerThread = new Thread(() -> {
            try {
                while (!stopped) {
                    // Wait if paused
                    while (paused && !stopped) {
                        Thread.sleep(50);
                    }
                    if (stopped) break;

                    // Only fetch when queue is nearly empty
                    if (scanQueue.size() < 2) {
                        File file = dao.getLocalTiffDAO().fetchNext();
                        if (file != null) {
                            scanQueue.put(file);
                        }
                    }

                    Thread.sleep(100);
                }
            } catch (Exception e) {
                if (!stopped) {
                    System.err.println("Scanner thread error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        scannerThread.setDaemon(true);
        scannerThread.start();
    }

    private void startProcessorThread() {

        processorThread = new Thread(() -> {

            try {

                while (!stopped) {

                    // Pause processing
                    while (paused && !stopped) {
                        Thread.sleep(50);
                    }

                    if (stopped) {
                        break;
                    }

                    // Wait for next scan
                    File file = scanQueue.take();

                    DocumentManager.ScanResult result =
                            documentManager.processFileScan(file);

                    if (result == DocumentManager.ScanResult.BARCODE) { //update the tree when a new document is created
                        Platform.runLater(this::loadTree);
                    }

                    Image image = TiffConverter.toJavaFXImageThumbnail(
                            file.getImageData(),
                            activeProfile
                    );

                    // DUPLICATE BARCODE
                    if (result ==
                            DocumentManager.ScanResult.DUPLICATE_BARCODE) {

                        paused = true;

                        Platform.runLater(() -> {

                            lblStatus.setText(
                                    "Scanned: "
                                            + documentManager.getTotalScans()
                                            + " file(s) — "
                                            + documentManager.getTotalDocuments()
                                            + " completed document(s)"
                            );

                            progressBar.setProgress(0);

                            showDuplicateBarcodeWarning(
                                    file.getBarcodeContent(),
                                    file,
                                    image
                            );
                        });

                        continue;
                    }

                    // NORMAL UI UPDATE
                    Platform.runLater(() -> {

                        progressBar.setProgress(1.0);

                        new Thread(() -> {

                            try {
                                Thread.sleep(200);
                            }
                            catch (InterruptedException ignored) {}

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

                        loadDocumentWithImage(
                                documentManager.getCurrentDocument(),
                                file,
                                image
                        );
                    });
                    Thread.sleep(300);
                }

            } catch (Exception e) {

                System.err.println(
                        "Processor thread error: "
                                + e.getMessage()
                );

                e.printStackTrace();
            }

            try {
                documentManager.finalizeLastDocument();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Platform.runLater(() -> {

                List<Document> completed =
                        documentManager.getCompletedDocuments();

                if (!completed.isEmpty()) {
                    loadDocument(completed.getLast());
                }

                lblStatus.setText(
                        (stopped ? "Stopped" : "Done")
                                + " — "
                                + documentManager.getTotalDocuments()
                                + " document(s), "
                                + documentManager.getTotalScans()
                                + " total scan(s)"
                );

                btnStartScan.setDisable(false);
                btnPauseScan.setDisable(true);
                btnStopScan.setDisable(true);

                progressBar.setVisible(false);
                progressBar.setManaged(false);
            });

        });

        processorThread.setDaemon(true);

        processorThread.start();
    }

    // Shows the files

    public void loadDocument(Document document) {
        thumbnailContainer.getChildren().clear();
        currentFiles = new ArrayList<>(document.getFiles());
        currentFileIndex = 0;
        currentRotation  = 0;

        for (int i = 0; i < currentFiles.size(); i++) {
            File file = currentFiles.get(i);
            // Thumbnail size only — saves memory
            Image thumbImage = TiffConverter.toJavaFXImageThumbnail(
                    file.getImageData(), activeProfile);
            if (thumbImage == null) thumbImage = new WritableImage(120, 160);
            addThumbnail(document, file, thumbImage, i);
        }

        if (!currentFiles.isEmpty()) {
            // Full res only for main preview
            Image first = TiffConverter.toJavaFXImage(
                    currentFiles.getFirst().getImageData(), activeProfile);
            mainPreview.setImage(first);
            mainPreview.setRotate(0);
        }

        mainScrollPane.requestFocus(); //For the keyboard shortcuts
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

            // Load full-res on demand on a background thread
            new Thread(() -> {
                Image fullRes = TiffConverter.toJavaFXImage(
                        file.getImageData(), activeProfile);
                Platform.runLater(() -> {
                    if (fullRes != null) mainPreview.setImage(fullRes);
                    else mainPreview.setImage(image); // fallback to thumbnail
                });
            }).start();
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

        ContextMenu menu = new ContextMenu();
        MenuItem splitItem = new MenuItem("Split document here?");

        splitItem.setOnAction(e ->
                splitDocumentAtFile(document, file));

        menu.getItems().add(splitItem);

        thumbBox.setOnContextMenuRequested(e ->
                menu.show(thumbBox, e.getScreenX(), e.getScreenY()));
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
        currentFiles = files;

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);

            // Use thumbnail size for sidebar — not full res
            Image thumbImage = file == latestFile
                    ? latestImage  // already converted on background thread
                    : TiffConverter.toJavaFXImageThumbnail(file.getImageData(), activeProfile);
            if (thumbImage == null) thumbImage = new WritableImage(120, 160);

            addThumbnail(document, file, thumbImage, i);
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

        Image image = TiffConverter.toJavaFXImageThumbnail(
                file.getImageData(),
                activeProfile
        );
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

    private void showDuplicateBarcodeWarning(String barcodeContent, File file, Image image) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Duplicate Barcode");
        alert.setHeaderText("This barcode has already been scanned");
        alert.setContentText(
                "Barcode ID: " + barcodeContent + "\n\n"
                        + "Please check if this document has already been scanned.\n"
                        + "What would you like to do?"
        );

        /* Image logo = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/Images/LogoBlueH.png")
                )
        );

        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(80);
        logoView.setPreserveRatio(true);

        alert.setGraphic(logoView);

         */

        ButtonType btnSkip = new ButtonType("Skip [ S ]", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnAccept = new ButtonType("Continue [ ENTER ]", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnSkip, btnAccept);

        Button skipButton =
                (Button) alert.getDialogPane().lookupButton(btnSkip);

        Button acceptButton =
                (Button) alert.getDialogPane().lookupButton(btnAccept);

        skipButton.getStyleClass().addAll("button", "button-danger");
        acceptButton.getStyleClass().addAll("button", "button-primary");

        alert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/dk/easv/weblagerexam/CSS/app.css")
                ).toExternalForm()
        );

        alert.showAndWait().ifPresentOrElse(choice -> {

            if (choice == btnAccept) {

                documentManager.approveDuplicateBarcode(barcodeContent);

                try {

                    documentManager.forceProcessBarcode(file);

                } catch (Exception e) {

                    System.err.println(
                            "Error force-processing barcode: "
                                    + e.getMessage()
                    );

                    paused = false;
                    progressBar.setProgress(-1);

                    return;
                }

                // We don't call loadDocumentWithImage here (converts images on JavaFX thread)
                // Instead we start a background thread to convert, then update UI
                // It kinda crashed when i tried it with the image loading bc of the heavy processing on the JavaFX thread
                Document current = documentManager.getCurrentDocument();
                paused = false;

                new Thread(() -> {

                    List<File> files = new ArrayList<>(current.getFiles());

                    List<Image> images = new ArrayList<>();

                    for (File f : files) {

                        Image img = f == file
                                ? image
                                : TiffConverter.toJavaFXImageThumbnail(
                                f.getImageData(),
                                activeProfile
                        );

                        images.add(
                                img != null
                                        ? img
                                        : new WritableImage(120, 160)
                        );
                    }

                    Platform.runLater(() -> {

                        thumbnailContainer.getChildren().clear();

                        currentFiles = files;

                        for (int i = 0; i < files.size(); i++) {

                            addThumbnail(
                                    current,
                                    files.get(i),
                                    images.get(i),
                                    i
                            );
                        }

                        if (!images.isEmpty()) {

                            mainPreview.setImage(images.getFirst());

                            currentFileIndex = 0;
                            currentRotation = 0;

                            mainPreview.setRotate(0);
                        }

                        lblStatus.setText(
                                "Scanned: "
                                        + documentManager.getTotalScans()
                                        + " file(s) — "
                                        + documentManager.getTotalDocuments()
                                        + " completed document(s)"
                        );

                        progressBar.setProgress(-1);

                    });

                }).start();

            } else {

                // Skip clicked
                paused = false;
                progressBar.setProgress(-1);
            }

        }, () -> {

            // Dialog closed with X button

            paused = false;
            progressBar.setProgress(-1);

        });
    }

    // For the tree

    @FXML
    private void refreshTree() {
        loadTree();
    }

    public void loadTree() {
        treeContainer.getChildren().clear();

        // No active box yet
        if (activeBox == null) {

            Label empty = new Label("No active box");
            empty.setStyle("""
            -fx-text-fill: #64748B;
            -fx-font-size: 11px;
            -fx-padding: 6;
        """);

            treeContainer.getChildren().add(empty);

            return;
        }

        // only show the current box
        treeContainer.getChildren().add(
                buildBoxNode(activeBox)
        );
    }

    private VBox buildBoxNode(Box box) {
        VBox boxNode = new VBox(2);

        HBox boxRow = new HBox(6);
        boxRow.getStyleClass().addAll("radius-md");
        boxRow.setStyle("-fx-padding: 5 6 5 6; -fx-cursor: hand;");

        Label arrow = new Label("▶");
        arrow.getStyleClass().add("text-helper");
        arrow.setMinWidth(12);

        // Shows the user-entered box number, falls back to DB id if box_id is 0 (it shouldn't be tho)
        String displayId = box.getBoxId() > 0
                ? String.valueOf(box.getBoxId())
                : String.valueOf(box.getId());
        Label boxLabel = new Label("Box #" + displayId);
        boxLabel.getStyleClass().add("label-medium");

        boxRow.getChildren().addAll(arrow, boxLabel);

        VBox docContainer = new VBox(1);
        docContainer.setVisible(false);
        docContainer.setManaged(false);
        docContainer.setStyle("-fx-padding: 0 0 0 14;");

        boxRow.setOnMouseEntered(e ->
                boxRow.setStyle("-fx-padding: 5 6 5 6; -fx-cursor: hand; " +
                        "-fx-background-color: #F1F5F9; -fx-background-radius: 6;"));
        boxRow.setOnMouseExited(e ->
                boxRow.setStyle("-fx-padding: 5 6 5 6; -fx-cursor: hand;"));

        boxRow.setOnMouseClicked(e -> {
            boolean expanded = docContainer.isVisible();
            if (!expanded) {
                docContainer.getChildren().clear();
                try {
                    List<Document> docs = dao.getDocumentDAO()
                            .getDocumentsForBox(box.getId());
                    for (Document doc : docs) {
                        docContainer.getChildren().add(buildDocumentNode(doc));
                    }
                    if (docs.isEmpty()) {
                        Label empty = new Label("No documents");
                        empty.getStyleClass().add("text-helper");
                        empty.setStyle("-fx-padding: 3 6;");
                        docContainer.getChildren().add(empty);
                    }
                } catch (Exception ex) {
                    System.err.println("Could not load documents: " + ex.getMessage());
                }
                arrow.setText("▼");
            } else {
                arrow.setText("▶");
            }
            docContainer.setVisible(!expanded);
            docContainer.setManaged(!expanded);
        });

        boxNode.getChildren().addAll(boxRow, docContainer);
        return boxNode;
    }

    private VBox buildDocumentNode(Document doc) {
        VBox docNode = new VBox(1);

        HBox docRow = new HBox(6);
        docRow.setStyle("-fx-padding: 4 6 4 6; -fx-cursor: hand;");

        Label arrow = new Label("▶");
        arrow.getStyleClass().add("text-helper");
        arrow.setMinWidth(12);

        Label docLabel = new Label("Doc #" + doc.getDocumentNumber());
        docLabel.getStyleClass().add("label-regular");

        docRow.getChildren().addAll(arrow, docLabel);
        // Lets user drag and drop files to another document
        docRow.setOnDragOver(event -> {

            Dragboard dragb = event.getDragboard();
            if (dragb.hasString()) {

                event.acceptTransferModes(TransferMode.MOVE);

                docRow.setStyle("""
                            -fx-padding: 4 6 4 6;
                            -fx-cursor: hand;
                            -fx-background-color: #F1F5F9;
                            -fx-border-color: #2D3D4F;
                            -fx-border-radius: 6;
                            -fx-background-radius: 6;
                        """);
            }
            event.consume();
        });
        docRow.setOnDragDropped(event -> {

            Dragboard db = event.getDragboard();

            boolean success = false;

            if (db.hasString()) {

                try {

                    String[] parts = db.getString().split(":");

                    int fileId = Integer.parseInt(parts[0]);
                    int sourceDocId = Integer.parseInt(parts[1]);

                    // Don't allow dropping into same doc
                    if (sourceDocId != doc.getId()) {

                        DAOManager daoManager = new DAOManager();

                        daoManager.getDocumentDAO().moveFileToDocument(fileId, doc.getId());

                        // Renumber both docs after moving a file
                        Document sourceDoc =
                                daoManager.getDocumentDAO().getDocumentById(sourceDocId);

                        Document targetDoc =
                                daoManager.getDocumentDAO().getDocumentById(doc.getId());

                        daoManager.getDocumentDAO().renumberFiles(sourceDoc);
                        daoManager.getDocumentDAO().renumberFiles(targetDoc);

                        loadTree();

                        lblStatus.setText(
                                "Moved file to Doc #"
                                        + doc.getDocumentNumber()
                        );

                        success = true;
                    }


                } catch (Exception e) {

                    e.printStackTrace();

                    lblStatus.setText(
                            "Could not move file"
                    );
                }
            }

            event.setDropCompleted(success);

            event.consume();
        });
        docRow.setOnDragExited(event -> {

            docRow.setStyle("""
                        -fx-padding: 4 6 4 6;
                        -fx-cursor: hand;
                    """);

            event.consume();
        });

        VBox fileContainer = new VBox(1);
        fileContainer.setVisible(false);
        fileContainer.setManaged(false);
        fileContainer.setStyle("-fx-padding: 0 0 0 14;");

        docRow.setOnMouseEntered(e ->
                docRow.setStyle("-fx-padding: 4 6 4 6; -fx-cursor: hand; " +
                        "-fx-background-color: #F1F5F9; -fx-background-radius: 6;"));
        docRow.setOnMouseExited(e ->
                docRow.setStyle("-fx-padding: 4 6 4 6; -fx-cursor: hand;"));

        docRow.setOnMouseClicked(e -> {
            boolean expanded = fileContainer.isVisible();
            if (!expanded) {
                fileContainer.getChildren().clear();
                try {
                    List<File> files = dao.getDocumentDAO()
                            .getFilesForDocument(doc.getId());
                    for (File file : files) {
                        fileContainer.getChildren().add(buildFileNode(doc, file));
                    }
                    if (files.isEmpty()) {
                        Label empty = new Label("No files");
                        empty.getStyleClass().add("text-helper");
                        empty.setStyle("-fx-padding: 2 6;");
                        fileContainer.getChildren().add(empty);
                    }
                } catch (Exception ex) {
                    System.err.println("Could not load files: " + ex.getMessage());
                }
                arrow.setText("▼");
            } else {
                arrow.setText("▶");
            }
            fileContainer.setVisible(!expanded);
            fileContainer.setManaged(!expanded);
        });

        docNode.getChildren().addAll(docRow, fileContainer);
        return docNode;
    }

    private HBox buildFileNode(Document doc, File file) {

        HBox fileRow = new HBox(6);
        fileRow.setStyle("-fx-padding: 3 6 3 6; -fx-cursor: hand;");

        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 8px; -fx-text-fill: "
                + (file.isBarcode() ? "#2D3D4F;" : "#E2E4E8;"));

        String label = file.isBarcode() ? "Barcode" : "File #" + file.getFileNumber();
        Label fileLabel = new Label(label);
        fileLabel.getStyleClass().add("text-helper");

        fileRow.getChildren().addAll(dot, fileLabel);

        fileRow.setOnMouseEntered(e ->
                fileRow.setStyle("-fx-padding: 3 6 3 6; -fx-cursor: hand; " +
                        "-fx-background-color: #F1F5F9; -fx-background-radius: 6;"));
        fileRow.setOnMouseExited(e ->
                fileRow.setStyle("-fx-padding: 3 6 3 6; -fx-cursor: hand;"));

        fileRow.setOnMouseClicked(e -> navigateToFile(doc, file));
        // Lets the user drag files (so they can move it to another document)
        fileRow.setOnDragDetected(event -> {

            Dragboard db = fileRow.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();

            // fileId:sourceDocumentId
            content.putString(file.getId() + ":" + doc.getId());

            db.setContent(content);

            fileRow.setOpacity(0.6);

            event.consume();
        });

        return fileRow;
    }

    /**
     * Loads the full file data from DB and shows it in the main preview.
     * The tree only stores IDs, so we need to get the image when a file is clicked.
     */
    private void navigateToFile(Document doc, File file) {
        new Thread(() -> {
            File fullFile = dao.getDocumentDAO().getFileById(file.getId());
            if (fullFile == null || fullFile.getImageData() == null) {
                Platform.runLater(() ->
                        lblStatus.setText("File #" + file.getId() + " has no image data"));
                return;
            }

            Image fullRes = TiffConverter.toJavaFXImage(
                    fullFile.getImageData(), activeProfile);

            Platform.runLater(() -> {
                if (fullRes != null) {
                    mainPreview.setImage(fullRes);
                    mainPreview.setRotate(0);
                    currentRotation = 0;
                    lblStatus.setText("Viewing File #" + fullFile.getFileNumber()
                            + " from Doc #" + doc.getDocumentNumber());
                }
            });
        }).start();
    }

    private void splitDocumentAtFile(Document originalDoc, File splitFile) {

        try {

            List<File> originalFiles = originalDoc.getFiles();

            int splitIndex = -1;

            for (int i = 0; i < originalFiles.size(); i++) {

                if (originalFiles.get(i).getId() == splitFile.getId()) {

                    splitIndex = i;
                    break;
                }
            }

            // Need at least 1 file before and after
            if (splitIndex <= 0 || splitIndex >= originalFiles.size()) {

                lblStatus.setText("Cannot split at this position");
                return;
            }

            Document newDoc =
                    documentManager.createManualSplitDocument(activeBox.getId());

            // Copy files over AFTER split point
            List<File> movedFiles = new ArrayList<>();

            for (int i = splitIndex; i < originalFiles.size(); i++) {

                movedFiles.add(originalFiles.get(i));
            }

            // Remove moved files from original
            originalFiles.subList(splitIndex, originalFiles.size()).clear();

            // Move files into new document
            for (File file : movedFiles) {

                file.setDocumentId(newDoc.getId());

                dao.getDocumentDAO().updateFileDocument(file);

                newDoc.getFiles().add(file);
            }

            // Renumber both docs
            dao.getDocumentDAO().renumberFiles(originalDoc);
            dao.getDocumentDAO().renumberFiles(newDoc);

            dao.getDocumentDAO().updateFileOrder(originalDoc);
            dao.getDocumentDAO().updateFileOrder(newDoc);

            loadTree();
            loadDocument(newDoc);

            lblStatus.setText(
                    "Created Doc #" + newDoc.getDocumentNumber()
                            + " from split"
            );

        } catch (Exception e) {

            e.printStackTrace();

            lblStatus.setText("Could not split document");
        }
    }

    @FXML
    private void onExportClicked() {
        try {
            Box activeBox = documentManager.getActiveBox();
            if (activeBox == null) {
                showAlert("No active box to export");
                return;}

            List<Document> documents =
                    dao.getDocumentDAO().getDocumentsForBox(activeBox.getId());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dk/easv/weblagerexam/export.fxml"));
            Parent root = loader.load();

            ExportController controller = loader.getController();
            controller.setup(activeBox, documents.size());

            Stage stage = new Stage();
            stage.setTitle("Export");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Export failed:\n" + e.getMessage());
        }
    }

    private void showAlert(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}