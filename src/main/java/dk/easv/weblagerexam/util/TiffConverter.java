package dk.easv.weblagerexam.util;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.bll.FileSettings;
import javafx.scene.image.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class TiffConverter {

    // Converts raw TIFF bytes to a JavaFX Image.
    public static Image toJavaFXImage(byte[] imageData, Profile profile) {
        if (imageData == null || imageData.length == 0) return null;
        try {
            BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(imageData));
            if (buffered == null) {
                System.err.println("TiffConverter: ImageIO.read returned null");
                return null;
            }
            // Apply profile settings before converting to JavaFX image
            BufferedImage settingsSet = FileSettings.apply(buffered, profile);
            return toFXImage(settingsSet);
        } catch (Exception e) {
            System.err.println("TiffConverter error: " + e.getMessage());
            return null;
        }
    }

    // no profile, shows original file without any settings/changes applied to it
    public static Image toJavaFXImage(byte[] imageData) {
        return toJavaFXImage(imageData, null);
    }

    private static WritableImage toFXImage(BufferedImage buffered) {
        int w = buffered.getWidth();
        int h = buffered.getHeight();
        WritableImage fxImage = new WritableImage(w, h);
        PixelWriter pw = fxImage.getPixelWriter();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                pw.setArgb(x, y, buffered.getRGB(x, y));
        return fxImage;
    }
}