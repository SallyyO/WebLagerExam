package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Document;
import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.dal.ApiDAO;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.dal.DocumentDAO;

import java.util.ArrayList;
import java.util.List;

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

    // Getters

    public Document getCurrentDocument() {return currentDocument;}

    public boolean hasMore() throws Exception {return dao.getApiDAO().hasMore();}

    public int getTotalScans() {return totalScans;}

    public int getTotalDocuments() {return totalDocuments;}

    public int getTotalAvailable() throws Exception {return dao.getApiDAO().getTotalCount();}

    public List<Document> getCompletedDocuments() {return completedDocuments;}

    public enum ScanResult {PAGE_ADDED, BARCODE}
}
