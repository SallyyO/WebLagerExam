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

    public void setActiveBox(Box box) {
        this.activeBox = box;
    }
    public void setActiveBoxId(int boxId) {this.activeBoxId = boxId;}

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
                dao.getDocumentDAO().saveDocument(currentDocument);
                completedDocuments.add(currentDocument);
                totalDocuments++;
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

    public void finalizeLastDocument() throws Exception {
        if (!currentDocument.isEmpty()) {
            currentDocument.setBoxId(activeBoxId);
            dao.getDocumentDAO().saveDocument(currentDocument);
            completedDocuments.add(currentDocument);
            totalDocuments++;
            currentDocument = new Document();
        }
    }

    public void saveMetadata(int documentId, String metadata) throws Exception {
        dao.getDocumentDAO().updateMetadata(documentId, metadata);
    }

    public File getLatestFileForUser(int userId) {
        return dao.getDocumentDAO().getLatestFileForUser(userId);
    }

    public Box getBoxForFile(int fileId) {
        return dao.getDocumentDAO().getBoxForFile(fileId);
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
            dao.getDocumentDAO().saveDocument(currentDocument);
            completedDocuments.add(currentDocument);
            totalDocuments++;
        }
        currentDocument = new Document();
        currentDocument.addFile(file);
    }

    public void approveDuplicateBarcode(String barcodeContent) {

        if (barcodeContent != null) {
            approvedDuplicateBarcodes.add(barcodeContent);
        }
    }


    public Document getCurrentDocument() {return currentDocument;}

    public boolean hasMore() throws Exception {return dao.getApiDAO().hasMore();}

    public int getTotalScans() {return totalScans;}

    public int getTotalDocuments() {return totalDocuments;}

    public int getTotalAvailable() throws Exception {return dao.getApiDAO().getTotalCount();}

    public List<Document> getCompletedDocuments() {return completedDocuments;}

    public enum ScanResult {PAGE_ADDED, BARCODE, DUPLICATE_BARCODE}

}
