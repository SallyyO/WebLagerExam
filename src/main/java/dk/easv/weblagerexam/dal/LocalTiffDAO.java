package dk.easv.weblagerexam.dal;



import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.util.BarcodeReader;

import java.io.InputStream;
import java.util.*;

public class LocalTiffDAO {

    // For now, instead of getting tiffs from the api, i found the ones that worked on my computer xdd
    private final List<String> normalFiles = new ArrayList<>(List.of(
            "TestTiffs/02.tiff",
            "TestTiffs/15.tiff",
            "TestTiffs/24.tiff"
    ));

    private final String barcodeFile = "TestTiffs/01.tiff"; //(if possible tho, i'd like 1 more)
    private final Random random = new Random();

    private boolean barcodeDeliveredThisCycle = false; // no barcode has been delieverd yet
    private int scansThisCycle = 0; // how many normal files have been returned after the barcode
    private static final int FILES_PER_DOCUMENT = 4;

    // Shuffle queue — refills when empty so no repeats
    private final Queue<String> shuffleQueue = new LinkedList<>();

    private String nextNormalFile() {
        if (shuffleQueue.isEmpty()) {
            List<String> shuffled = new ArrayList<>(normalFiles);
            Collections.shuffle(shuffled, random);
            shuffleQueue.addAll(shuffled);
        }
        return shuffleQueue.poll();
    }

    public File fetchNext() {
        if (!barcodeDeliveredThisCycle) {
            barcodeDeliveredThisCycle = true;
            scansThisCycle = 0;
            return loadFile(barcodeFile, true);
        }

        scansThisCycle++;
        if (scansThisCycle >= FILES_PER_DOCUMENT) {
            barcodeDeliveredThisCycle = false;
        }

        return loadFile(nextNormalFile(), false);
    }

    private File loadFile(String path, boolean isBarcode) {
        try {
            InputStream is = getClass().getResourceAsStream("/" + path);
            if (is == null) throw new RuntimeException("Could not find: /" + path);
            byte[] data = is.readAllBytes();

            File file = new File(data, isBarcode);
            file.setFileName(path.contains("/")
                    ? path.substring(path.lastIndexOf('/') + 1)
                    : path);

            // Decode barcode content and store it in the File object
            if (isBarcode) {
                String content = BarcodeReader.decode(data);
                file.setBarcodeContent(content);
            }

            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}