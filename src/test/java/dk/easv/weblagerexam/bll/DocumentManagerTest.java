package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.dal.DocumentDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DocumentManagerTest {

    private DocumentManager manager;
    private DocumentDAO mockDocumentDAO;
    private DAOManager mockDao;

    @BeforeEach
    void setUp() {
        mockDocumentDAO = Mockito.mock(DocumentDAO.class);
        mockDao = Mockito.mock(DAOManager.class);
        Mockito.when(mockDao.getDocumentDAO()).thenReturn(mockDocumentDAO);
        Box box = Mockito.mock(Box.class);
        box.setBoxId(1);

        User testUser = new User();
        testUser.setId(1);

        SessionManager.setCurrentUser(testUser);

        manager = new DocumentManager(mockDao);
        manager.setActiveBoxId(1);
        manager.setActiveBox(box);
    }

    @Test
    void newFileAddsToCurrentDocument() throws Exception {
        File file = new File(new byte[]{1, 2, 3}, false);

        DocumentManager.ScanResult result = manager.processFileScan(file);

        assertEquals(DocumentManager.ScanResult.PAGE_ADDED, result);
        assertEquals(1, manager.getCurrentDocument().getFileCount());
        assertEquals(1, manager.getTotalScans());
    }

    @Test
    void newBarcodeSavesCurrentDocAndStartsNew() throws Exception {
        // Scan two normal files first
        manager.processFileScan(new File(new byte[]{1}, false));
        manager.processFileScan(new File(new byte[]{2}, false));

        // Hitting barcode!?
        File barcode = new File(new byte[]{3}, true);
        barcode.setBarcodeContent("4643582");
        DocumentManager.ScanResult result = manager.processFileScan(barcode);

        assertEquals(DocumentManager.ScanResult.BARCODE, result);
        assertEquals(1, manager.getTotalDocuments()); // one doc saved
        assertEquals(1, manager.getCurrentDocument().getFileCount()); // barcode is page 1 of new doc
        Mockito.verify(mockDocumentDAO, Mockito.times(1))
                .saveDocument(Mockito.any());
    }

    @Test
    void totalScansCountsEverything() throws Exception {
        manager.processFileScan(new File(new byte[]{1}, false));
        manager.processFileScan(new File(new byte[]{2}, false));
        File barcode = new File(new byte[]{3}, true);
        barcode.setBarcodeContent("4643582");
        manager.processFileScan(barcode);

        assertEquals(3, manager.getTotalScans()); // all 3 counted including barcode
    }

    @Test
    void duplicateBarcodeReturnsDuplicate() throws Exception {
        File barcode1 = new File(new byte[]{1}, true);
        barcode1.setBarcodeContent("4643582");
        manager.processFileScan(new File(new byte[]{0}, false)); // need something to save
        manager.processFileScan(barcode1); // first time — OK

        File barcode2 = new File(new byte[]{2}, true);
        barcode2.setBarcodeContent("4643582"); // same content
        DocumentManager.ScanResult result = manager.processFileScan(barcode2);

        assertEquals(DocumentManager.ScanResult.DUPLICATE_BARCODE, result);
    }


    @Test
    void approvedBarcodeAllowsRepeatWithoutWarning() throws Exception {
        File barcode = new File(new byte[]{1}, true);
        barcode.setBarcodeContent("4643582");
        manager.processFileScan(new File(new byte[]{0}, false));
        manager.processFileScan(barcode); // first scan

        manager.approveDuplicateBarcode("4643582"); // user approved it

        File barcode2 = new File(new byte[]{2}, true);
        barcode2.setBarcodeContent("4643582");
        DocumentManager.ScanResult result = manager.processFileScan(barcode2);

        // user approved so it should not give a warning
        assertEquals(DocumentManager.ScanResult.BARCODE, result);
    }
}

