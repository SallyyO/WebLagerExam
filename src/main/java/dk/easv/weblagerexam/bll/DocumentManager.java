package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Document;
import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.dal.ApiDAO;
import dk.easv.weblagerexam.dal.DocumentDAO;

import java.util.ArrayList;
import java.util.List;

public class DocumentManager {

    private final ApiDAO apiDao;
    private final DocumentDAO documentDAO;

    public DocumentManager() {
        apiDao = new ApiDAO();
        documentDAO = new DocumentDAO();
    }

    private Document currentDocument = new Document();
    private int totalScans = 0;
    private int totalDocuments = 0;
    private final List<Document> completedDocuments = new ArrayList<>();
    private Box activeBox;

    public void setActiveBox(Box box) {
        this.activeBox = box;
    }

    //Was used when we fetched from the api
    public ScanResult processNextScan() throws Exception {
        File file = apiDao.fetchNext();
        totalScans++;

        if (file.isBarcode()) {
            // Finalize whatever was being built
            if (!currentDocument.isEmpty()) {
                documentDAO.saveDocument(currentDocument);
                completedDocuments.add(currentDocument);
                totalDocuments++;
            }

            // Start the NEW document with the barcode page as its first file
            currentDocument = new Document();
            currentDocument.addFile(file); // barcode is always page 1

            return ScanResult.BARCODE;
        } else {
            currentDocument.addFile(file);
            return ScanResult.PAGE_ADDED;
        }
    }

    /**
     * Same logic as processNextScan() but accepts a pre-fetched File.
     * Used for the local tiff-stuff rn
     */
    public ScanResult processFileScan(File file) throws Exception {
        totalScans++;

        if (file.isBarcode()) {
            if (!currentDocument.isEmpty()) {
                documentDAO.saveDocument(currentDocument);
                completedDocuments.add(currentDocument);
                totalDocuments++;
            }
            currentDocument = new Document();
            currentDocument.addFile(file); // barcode is page 1 of new doc
            return ScanResult.BARCODE;
        } else {
            currentDocument.addFile(file);
            return ScanResult.PAGE_ADDED;
        }
    }

    public void finalizeLastDocument() throws Exception {
        if (!currentDocument.isEmpty()) {
            documentDAO.saveDocument(currentDocument);
            completedDocuments.add(currentDocument);
            totalDocuments++;
            currentDocument = new Document();
        }
    }

    public void saveMetadata(int documentId, String metadata) throws Exception {
        documentDAO.updateMetadata(documentId, metadata);
    }

    // Getters

    public Document getCurrentDocument() {return currentDocument;}

    public boolean hasMore() throws Exception {return apiDao.hasMore();}

    public int getTotalScans() {return totalScans;}

    public int getTotalDocuments() {return totalDocuments;}

    public int getTotalAvailable() throws Exception {return apiDao.getTotalCount();}

    public List<Document> getCompletedDocuments() {return completedDocuments;}

    public enum ScanResult {PAGE_ADDED, BARCODE}
}
