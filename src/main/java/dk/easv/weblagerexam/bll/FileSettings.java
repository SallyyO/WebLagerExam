package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class FileSettings {

    /**
     * Applies a profile's settings to the BufferedImage.
     * Returns the edited image (original is never modified).
     */
    public static BufferedImage apply(BufferedImage source, Profile profile) {
        if (profile == null || source == null) return source;

        return switch (profile.getSettings()) {
            case GRAYSCALE -> toGrayscale(source);
            case ROTATE -> rotate(source, profile.getSettingsValue()); // rotates using profiles value
            case ROTATE_AUTO -> rotateToHorizontal(source);
            case BRIGHTEN -> brighten(source, profile.getSettingsValue());

            case RAVENCLAW -> useHouseColor(source, RAVENCLAW_BLUE);
            case GRYFFINDOR -> useHouseColor(source, GRYFFINDOR_RED);
            case SLYTHERIN -> useHouseColor(source, SLYTHERIN_GREEN);
            case HUFFLEPUFF -> useHouseColor(source, HUFFLEPUFF_GOLD);
        };
    }


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

        // loop through every pixel
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);

                int r = Math.min(255, ((rgb >> 16) & 0xFF) + increase);
                int g = Math.min(255, ((rgb >> 8) & 0xFF) + increase);
                int b = Math.min(255, (rgb & 0xFF) + increase);

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    private static final Color RAVENCLAW_BLUE = new Color(34, 47, 91);
    private static final Color GRYFFINDOR_RED = new Color(116, 0, 1);
    private static final Color SLYTHERIN_GREEN = new Color(26, 71, 42);
    private static final Color HUFFLEPUFF_GOLD = new Color(236, 185, 57);

    private static BufferedImage useHouseColor(BufferedImage src, Color tint) {

        BufferedImage result = new BufferedImage(
                src.getWidth(),
                src.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {

                int rgb = src.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int gray = (r + g + b) / 3;

                int newR = gray * tint.getRed() / 255;
                int newG = gray * tint.getGreen() / 255;
                int newB = gray * tint.getBlue() / 255;

                result.setRGB(x, y, (newR << 16) | (newG << 8) | newB);
            }
        }

        return result;
    }
}
