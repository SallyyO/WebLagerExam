package dk.easv.weblagerexam.be;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DocumentTest {

    @Test
    void addFileAssignsSequentialFileNumbers() {
        Document document = new Document();
        File first = new File(new byte[]{1}, false);
        File second = new File(new byte[]{2}, false);

        document.addFile(first);
        document.addFile(second);

        assertEquals(2, document.getFileCount());
        assertEquals(1, first.getFileNumber());
        assertEquals(2, second.getFileNumber());
    }

    @Test
    void reorderingFilesMovesFileAndRenumbersAllFiles() {
        Document document = new Document();
        File first = new File(new byte[]{1}, false);
        File second = new File(new byte[]{2}, false);
        File third = new File(new byte[]{3}, false);
        document.addFile(first);
        document.addFile(second);
        document.addFile(third);

        document.reorderFiles(0, 2);

        assertSame(second, document.getFiles().get(0));
        assertSame(third, document.getFiles().get(1));
        assertSame(first, document.getFiles().get(2));
        assertEquals(1, second.getFileNumber());
        assertEquals(2, third.getFileNumber());
        assertEquals(3, first.getFileNumber());
    }

    @Test
    void reorderFilesIgnoresInvalidIndexes() {
        Document document = new Document();
        File first = new File(new byte[]{1}, false);
        File second = new File(new byte[]{2}, false);
        document.addFile(first);
        document.addFile(second);

        document.reorderFiles(-1, 1);
        document.reorderFiles(0, 2);

        assertSame(first, document.getFiles().get(0));
        assertSame(second, document.getFiles().get(1));
        assertEquals(1, first.getFileNumber());
        assertEquals(2, second.getFileNumber());
    }
}
