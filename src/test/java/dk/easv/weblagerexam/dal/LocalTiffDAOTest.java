package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalTiffDAOTest {
    @Test
    void firstFileIsAlwaysBarcodeWhenFetchNext() {
        LocalTiffDAO dao = new LocalTiffDAO();
        File first = dao.fetchNext();
        assertTrue(first.isBarcode());
    }

    @Test
    void afterBarcodeNextFilesAreNotBarcodes() {
        LocalTiffDAO dao = new LocalTiffDAO();
        dao.fetchNext(); // barcode
        for (int i = 0; i < 3; i++) {
            assertFalse(dao.fetchNext().isBarcode());
        }
    }

    @Test
    void barcodeShouldAppearAgainAfterCycle() {
        LocalTiffDAO dao = new LocalTiffDAO();
        dao.fetchNext(); // first barcode


        for (int i = 0; i < 4; i++) {
            dao.fetchNext();
        }

        // Next should be a barcode again
        assertTrue(dao.fetchNext().isBarcode());
    }


}
