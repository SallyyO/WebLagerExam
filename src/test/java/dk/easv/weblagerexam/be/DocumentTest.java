package dk.easv.weblagerexam.be;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentTest {

    @Test
    void scanningFilesAssignsIncrementalFileNumbers() {
        Document doc = new Document();
        doc.addFile(new File(new byte[]{1}, false));
        doc.addFile(new File(new byte[]{2}, false));
        doc.addFile(new File(new byte[]{3}, false));

        assertEquals(1, doc.getFiles().get(0).getFileNumber());
        assertEquals(2, doc.getFiles().get(1).getFileNumber());
        assertEquals(3, doc.getFiles().get(2).getFileNumber());
    }

    @Test
    void reorderingFilesMovesFileAndRenumbers() {
        Document doc = new Document();
        File a = new File(new byte[]{1}, false);
        File b = new File(new byte[]{2}, false);
        File c = new File(new byte[]{3}, false);
        doc.addFile(a);
        doc.addFile(b);
        doc.addFile(c);

        doc.reorderFiles(0, 2); // move first to last

        assertEquals(c, doc.getFiles().get(1)); // c moved up
        assertEquals(a, doc.getFiles().get(2)); // a is now last
        assertEquals(1, doc.getFiles().get(0).getFileNumber());
        assertEquals(2, doc.getFiles().get(1).getFileNumber());
        assertEquals(3, doc.getFiles().get(2).getFileNumber());
    }

    @Test
    void reorderingFilesOutOfBoundsDoesNothing() {
        Document doc = new Document();
        doc.addFile(new File(new byte[]{1}, false));

        // Should not do or throw anything
        assertDoesNotThrow(() -> doc.reorderFiles(0, 5));
        assertDoesNotThrow(() -> doc.reorderFiles(-1, 0));
    }

    @Test
    void isEmptyIsTrueWhenNoFiles() {
        assertTrue(new Document().isEmpty());
    }
    @Test
    void isEmptyIsFalseWhenNotEmpty() {
        Document doc = new Document();
        doc.addFile(new File(new byte[]{1}, false));
        assertFalse(doc.isEmpty());
    }

    @Test
    void fileCountPrefersLoadedFilesOverDbCount() {
        Document doc = new Document();
        doc.setFileCount(5);
        doc.addFile(new File(new byte[]{1}, false));
        doc.addFile(new File(new byte[]{2}, false));
        assertEquals(2, doc.getFileCount()); // uses list, not the DB value
    }

}
