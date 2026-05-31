package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Document;
import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.dal.ApiDAO;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.dal.DocumentDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentManager {

    private final ApiDAO apiDao;
    private final DocumentDAO documentDAO;
    private final DAOManager dao = new DAOManager();
    private final LogManager logManager = new LogManager();

    public DocumentManager() {
        apiDao = new ApiDAO();
        documentDAO = new DocumentDAO();
    }

    private Document currentDocument = new Document();
    private int totalScans = 0;
    private int totalDocuments = 0;
    private final List<Document> completedDocuments = new ArrayList<>();
    private Box activeBox;
    private int activeBoxId = 0;
    private final Set<String> seenBarcodes = ConcurrentHashMap.newKeySet();
    private final Set<String> approvedDuplicateBarcodes = ConcurrentHashMap.newKeySet();
    private int currentDocumentNumber = 1;

    public void setActiveBox(Box box) {
        this.activeBox = box;
    }
    public Box getActiveBox() {
        return activeBox;
    }

    public void setActiveBoxId(int boxId) {
        this.activeBoxId = boxId;
        currentDocumentNumber = 1;}

    /**
     * Processes a scanned or fetched file
     * works with both the locally stored ones and with files fetched from the api
     */
    public ScanResult processFileScan(File file) throws Exception {
        totalScans++;
        if (file.isBarcode()) {
            // Check for duplicate BEFORE doing anything else
            String duplicate = checkDuplicateBarcode(file);
            if (duplicate != null) {
                return ScanResult.DUPLICATE_BARCODE; // caller handles the warning
            }

            if (!currentDocument.isEmpty()) {
                currentDocument.setBoxId(activeBoxId);
                currentDocument.setDocumentNumber(currentDocumentNumber);
                dao.getDocumentDAO().saveDocument(currentDocument);
                currentDocumentNumber++;
                completedDocuments.add(currentDocument);
                totalDocuments++;
                logManager.logDocumentCreated(currentDocument.getId(), activeBox.getBoxId());
            }
            currentDocument = new Document();
            currentDocument.addFile(file);


            return ScanResult.BARCODE;
        } else {
            currentDocument.addFile(file);
            return ScanResult.PAGE_ADDED;
        }
    }

    //Just fetches files from API and sends them to the method above this
    public ScanResult processNextScan() throws Exception {
        return processFileScan(dao.getApiDAO().fetchNext());
    }

    // returns the duplicate content if already seen, null if new
    public String checkDuplicateBarcode(File file) {
        if (!file.isBarcode()) return null;
        String content = file.getBarcodeContent();
        if (content == null || content.isBlank()) return null;

        // First time seeing barcode
        if (!seenBarcodes.contains(content)) {
            seenBarcodes.add(content);
            return null;
        }

        // Already approved by user during this session
        if (approvedDuplicateBarcodes.contains(content)) {
            return null;
        }

        // Duplicate not yet approved
        return content;
    }

    /**
     * Forces a duplicate barcode to be treated as a new document split.
     * Called when the user clicks "Accept" on the duplicate warning.
     * Was needed bc... we only have 1 barcode rn hahah
     */
    public void forceProcessBarcode(File file) throws Exception {
        // Re-add to seenBarcodes in case it was a third scan of the same barcode
        if (file.getBarcodeContent() != null) {
            seenBarcodes.add(file.getBarcodeContent());
        }

        // Same logic as a normal barcode hit
        if (!currentDocument.isEmpty()) {
            currentDocument.setBoxId(activeBoxId);
            currentDocument.setDocumentNumber(currentDocumentNumber);
            dao.getDocumentDAO().saveDocument(currentDocument);
            currentDocumentNumber++;
            completedDocuments.add(currentDocument);
            totalDocuments++;
        }
        currentDocument = new Document();
        currentDocument.addFile(file);
    }

    public void approveDuplicateBarcode(String barcodeContent) {
        if (barcodeContent != null) {
            approvedDuplicateBarcodes.add(barcodeContent);

            logManager.logDuplicateBarcodeAccepted(SessionManager.getCurrentUser().getId(), barcodeContent);
        }
    }

    public List<Document> getDocsAndFilesFromBox(int boxId) {
        return dao.getDocumentDAO().getDocsAndFilesFromBox(boxId);
    }
//Documents
    public int saveDocument(Document document) {
        return dao.getDocumentDAO().saveDocument(document);
    }

    public void createDocument(Document document) {
        dao.getDocumentDAO().createDocument(document);
        logManager.logDocumentCreated(document.getId(), document.getBoxId()
        );
    }

    public Document getDocumentById(int documentId) {
        return dao.getDocumentDAO().getDocumentById(documentId);
    }

    public List<Document> getDocumentsForBox(int boxId) {
        return dao.getDocumentDAO().getDocumentsForBox(boxId);
    }

    public int getTotalDocumentCount() {
        return dao.getDocumentDAO().getTotalDocumentCount();
    }

    public Document createManualSplitDocument(int boxId) throws Exception {
        int nextDocumentNumber = getTotalDocuments() + 1;
        Document doc = new Document();

        doc.setBoxId(boxId);
        doc.setDocumentNumber(nextDocumentNumber);

        dao.getDocumentDAO().createDocument(doc);
        completedDocuments.add(doc);

        return doc;
    }

    public void finalizeLastDocument() throws Exception {
        if (!currentDocument.isEmpty()) {
            currentDocument.setBoxId(activeBoxId);
            currentDocument.setDocumentNumber(currentDocumentNumber);
            dao.getDocumentDAO().saveDocument(currentDocument);
            currentDocumentNumber++;
            completedDocuments.add(currentDocument);
            totalDocuments++;
            logManager.logDocumentFinalized(currentDocument.getId(), activeBox.getBoxId());
            currentDocument = new Document();
        }
    }

    //Files
    public List<File> getFilesForDocument(int documentId) {
        return dao.getDocumentDAO().getFilesForDocument(documentId);
    }

    public List<File> getFilesForDocumentWithData(int documentId) {
        return dao.getDocumentDAO().getFilesForDocumentWithData(documentId);
    }

    public File getFileById(int fileId) {
        return dao.getDocumentDAO().getFileById(fileId);
    }

    public File getLatestFileForUser(int userId) {
        return dao.getDocumentDAO().getLatestFileForUser(userId);
    }

    public Box getBoxForFile(int fileId) {
        return dao.getDocumentDAO().getBoxForFile(fileId);
    }

    //Moving/reordering files
    public void updateFileOrder(Document document) {
        dao.getDocumentDAO().updateFileOrder(document);
    }

    public void renumberFiles(Document document) {
        dao.getDocumentDAO().renumberFiles(document);
    }

    public void moveFileToDocument(int fileId, int targetDocumentId) {
        dao.getDocumentDAO().moveFileToDocument(
                        fileId, targetDocumentId
                );
    }

    public void updateFileDocument(File file) {
        dao.getDocumentDAO().updateFileDocument(file);
    }

    public Document getCurrentDocument() {return currentDocument;}

    public boolean hasMore() throws Exception {return dao.getApiDAO().hasMore();}

    public int getTotalScans() {return totalScans;}

    public int getTotalDocuments() {return totalDocuments;}

    public int getTotalAvailable() throws Exception {return dao.getApiDAO().getTotalCount();}

    public List<Document> getCompletedDocuments() {return completedDocuments;}

    public enum ScanResult {PAGE_ADDED, BARCODE, DUPLICATE_BARCODE}

}
