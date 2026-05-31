package dk.easv.weblagerexam.util;

import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.bll.FileSettings;
import javafx.embed.swing.SwingFXUtils;
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
        return SwingFXUtils.toFXImage(buffered, null);
    }

    public static Image toJavaFXImageThumbnail(byte[] imageData, Profile profile) {
        if (imageData == null || imageData.length == 0) return null;
        try {
            BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(imageData));
            if (buffered == null) return null;

            // Downscale to thumbnail size BEFORE applying profile settings (to reduce memory)
            int thumbWidth  = 120;
            int thumbHeight = (int)(buffered.getHeight() * (120.0 / buffered.getWidth()));
            BufferedImage small = new BufferedImage(thumbWidth, thumbHeight,
                    BufferedImage.TYPE_INT_RGB);
            small.getGraphics().drawImage(
                    buffered.getScaledInstance(thumbWidth, thumbHeight,
                            java.awt.Image.SCALE_SMOOTH), 0, 0, null);
            buffered.flush(); // release the full-res image immediately
            buffered = null;

            // Apply profile settings on the small image
            BufferedImage settingsApplied = FileSettings.apply(small, profile);
            return toFXImage(settingsApplied);

        } catch (Exception e) {
            System.err.println("TiffConverter thumbnail error: " + e.getMessage());
            return null;
        }
    }

    public static Image getThumbnail(File file, Profile profile) {

        int profileId =
                profile != null
                        ? profile.getId()
                        : -1;

        if (file.getThumbnailCache() != null
                && file.getCachedProfileId() == profileId) {
            return file.getThumbnailCache();
        }

        Image image = toJavaFXImageThumbnail(file.getImageData(), profile);

        file.setThumbnailCache(image);
        file.setCachedProfileId(profileId);

        return image;
    }

    public static Image getFullImage(File file, Profile profile) {

        int profileId = profile != null
                ? profile.getId()
                : -1;

        if (file.getFullImageCache() != null
                && file.getFullImageProfileId() == profileId) {
            return file.getFullImageCache();
        }

        Image image = toJavaFXImage(file.getImageData(), profile);

        file.setFullImageCache(image);
        file.setFullImageProfileId(profileId);

        return image;
    }


}