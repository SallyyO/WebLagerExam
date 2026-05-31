package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Document;
import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.util.ExportMode;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class ExportManager {

    public Path exportBox(Box box, List<Document> documents, Path exportRoot, ExportMode mode) throws Exception {
        try {
            if (documents == null || documents.isEmpty()) {
                throw new IllegalArgumentException("No documents to export");
            }

            String profileName = (box.getProfile() != null) ? goodNameSir(box.getProfile().getName()) : "NoProfile";
            String folderName = profileName + "_" + box.getBoxId();
            Path boxFolder = exportRoot.resolve(folderName);

            Files.createDirectories(boxFolder);
            for (int d = 0; d < documents.size(); d++) {

                Document doc = documents.get(d);
                if (mode == ExportMode.MULTI_PAGE) {
                    exportMultiPageDocument(
                            box,
                            doc,
                            boxFolder,
                            d + 1
                    );

                } else {
                    exportSinglePageDocument(
                            box,
                            doc,
                            boxFolder,
                            d + 1
                    );
                }
            }

            new LogManager().logBoxExported(box.getBoxId(), mode.name());
            return boxFolder;

        } catch (Exception e) {
            new LogManager().logExportFailed(box.getBoxId(), e.getMessage());
            throw e;
        }
    }

    //One document exported as many tiff-files
    private void exportSinglePageDocument(Box box, Document doc, Path boxFolder, int documentNumber) throws Exception {

        Path documentFolder = boxFolder.resolve("document_" + documentNumber);
        Files.createDirectories(documentFolder);

        for (File file : doc.getFiles()) {

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getImageData()));
            if (image == null) continue;

            // Apply profile settings
            BufferedImage processed = FileSettings.apply(image, box.getProfile());

            String filename = "file_" + file.getFileNumber() + ".tiff";
            Path outputPath = documentFolder.resolve(filename);
            ImageIO.write(processed, "TIFF", outputPath.toFile());
        }
    }

    //One document exported as one multi-page tiff-file
    private void exportMultiPageDocument(Box box, Document doc, Path boxFolder, int documentNumber) throws Exception {

        String filename = "document_" + documentNumber + ".tiff";
        Path outputPath = boxFolder.resolve(filename);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");

        if (!writers.hasNext()) {throw new Exception("No TIFF writer found");}

        ImageWriter writer = writers.next();
        try (FileImageOutputStream output = new FileImageOutputStream(outputPath.toFile())) {

            writer.setOutput(output);
            writer.prepareWriteSequence(null);

            for (File file : doc.getFiles()) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getImageData()));
                if (image == null) continue;

                // Apply profile settings
                BufferedImage processed = FileSettings.apply(image, box.getProfile());
                writer.writeToSequence(new IIOImage(processed, null, null), null);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    private String goodNameSir(String text) {
        return text.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}