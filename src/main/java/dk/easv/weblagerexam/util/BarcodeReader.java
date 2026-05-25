package dk.easv.weblagerexam.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class BarcodeReader {

    /**
     * Attempts to decode a barcode from image bytes.
     * Returns the decoded string (e.g. "4643582") or null if none found.
     */
    public static String decode(byte[] imageData) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) return null;

            // Downsample for speed
            int targetWidth = 800;
            int targetHeight = (int)(image.getHeight() * (800.0 / image.getWidth()));
            BufferedImage scaled = new BufferedImage(
                    targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            scaled.getGraphics().drawImage(
                    image.getScaledInstance(targetWidth, targetHeight,
                            java.awt.Image.SCALE_SMOOTH), 0, 0, null);

            LuminanceSource source = new BufferedImageLuminanceSource(scaled);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();

        } catch (NotFoundException e) {
            return null; // no barcode found
        } catch (Exception e) {
            System.err.println("BarcodeReader error: " + e.getMessage());
            return null;
        }
    }
}