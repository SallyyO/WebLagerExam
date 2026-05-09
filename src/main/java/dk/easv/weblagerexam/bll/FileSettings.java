package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class FileSettings {

    /**
     * Applies a profile's settings to a BufferedImage.
     * Returns the edited image (original is never modified).
     * If profile is null, returns the original unchanged.
     */
    public static BufferedImage apply(BufferedImage source, Profile profile) {
        if (profile == null || source == null) return source;

        return switch (profile.getSettings()) {
            case GRAYSCALE   -> toGrayscale(source);
            case ROTATE      -> rotate(source, profile.getSettingsValue());
            case ROTATE_AUTO -> rotateToHorizontal(source);
            case BRIGHTEN    -> brighten(source, profile.getSettingsValue());
        };
    }

    // Settings

    private static BufferedImage toGrayscale(BufferedImage src) {
        BufferedImage result = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = result.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return result;
    }

    private static BufferedImage rotate(BufferedImage src, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int w = src.getWidth();
        int h = src.getHeight();

        // New canvas size to fit the rotated image without clipping
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage result = new BufferedImage(newW, newH, src.getType());
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // White background (so rotated corners aren't black)
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newW, newH);

        // Rotate around the center of the new canvas
        AffineTransform transform = new AffineTransform();
        transform.translate((newW - w) / 2.0, (newH - h) / 2.0);
        transform.rotate(radians, w / 2.0, h / 2.0);
        g.drawRenderedImage(src, transform);
        g.dispose();
        return result;
    }

    private static BufferedImage rotateToHorizontal(BufferedImage src) {
        // If the image is taller than wide (portrait), rotate 90° to make it landscape
        if (src.getHeight() > src.getWidth()) {
            return rotate(src, 90);
        }
        return src; // already horizontal
    }

    private static BufferedImage brighten(BufferedImage src, double amount) {
        int increase = (int) Math.min(255, Math.max(0, amount));

        BufferedImage result = new BufferedImage(
                src.getWidth(), src.getHeight(), src.getType());

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);

                int r = Math.min(255, ((rgb >> 16) & 0xFF) + increase);
                int g = Math.min(255, ((rgb >> 8)  & 0xFF) + increase);
                int b = Math.min(255, ( rgb        & 0xFF) + increase);

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }
}
