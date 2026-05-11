package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.*;
import dk.easv.weblagerexam.be.File;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class OtherExportManager {

    /**
     * Exports all documents in a box to a folder named {profileName}_{boxId}.
     * Each document becomes a subfolder. Each file is saved as a TIFF.
     * Profile transforms are applied to every image during export.
     * Pretty sure this is how they want it to be.
     */
    public Path exportBox(Box box, List<Document> documents, Path exportRoot)
            throws IOException {

        // Folder name: profileName_boxId or just boxId if no profile
        String profileName = (box.getProfile() != null)
                ? box.getProfile().getName().replaceAll("[^a-zA-Z0-9]", "_")
                : "NoProfile";
        String folderName = profileName + "_" + box.getId();

        Path boxFolder = exportRoot.resolve(folderName);
        Files.createDirectories(boxFolder);

        for (int d = 0; d < documents.size(); d++) {
            Document doc = documents.get(d);
            // Subfolder per document: doc_1, doc_2, ...
            Path docFolder = boxFolder.resolve("doc_" + (d + 1));
            Files.createDirectories(docFolder);

            for (File file : doc.getFiles()) {
                BufferedImage image = ImageIO.read(
                        new ByteArrayInputStream(file.getImageData()));

                if (image == null) continue;

                // Apply profile settings
                BufferedImage settingsSet = FileSettings.apply(
                        image, box.getProfile());

                // Save as TIFF
                String fileName = file.getFileName() != null
                        ? file.getFileName()
                        : "page_" + file.getFileNumber() + ".tiff";

                Path outPath = docFolder.resolve(fileName);
                ImageIO.write(settingsSet, "TIFF", outPath.toFile());
            }
        }

        return boxFolder; // return path to the exported folder for reference
    }
}