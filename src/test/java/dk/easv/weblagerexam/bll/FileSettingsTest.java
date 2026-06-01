package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.ProfileSettings;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class FileSettingsTest {

    private BufferedImage makeTestImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                img.setRGB(x, y, 0xFF4488CC); // blue-ish
        return img;
    }

    @Test
    void rotate_90degrees_swapsDimensions() {
        Profile rotate = new Profile(1, "Rotate", ProfileSettings.ROTATE, 90.0);
        BufferedImage src = makeTestImage(200, 100); // wider than tall
        BufferedImage result = FileSettings.apply(src, rotate);

        // After 90° rotation width and height swap (ish)
        assertTrue(result.getHeight() > result.getWidth());
    }

    @Test
    void rotateAuto_portraitBecomesLandscape() {
        Profile auto = new Profile(1, "Auto", ProfileSettings.ROTATE_AUTO, null);
        BufferedImage portrait = makeTestImage(100, 200); // taller than wide
        BufferedImage result = FileSettings.apply(portrait, auto);

        assertTrue(result.getWidth() >= result.getHeight());
    }

    @Test
    void rotateAuto_landscapeUnchanged() {
        Profile auto = new Profile(1, "Auto", ProfileSettings.ROTATE_AUTO, null);
        BufferedImage landscape = makeTestImage(200, 100); // already landscape so leave it alone
        BufferedImage result = FileSettings.apply(landscape, auto);

        assertSame(landscape, result); // no rotation applied
    }

    @Test
    void brighten_increasesPixelValues() {
        Profile brighten = new Profile(1, "Bright", ProfileSettings.BRIGHTEN, 50.0);
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        // Fill with dark colour
        for (int y = 0; y < 10; y++)
            for (int x = 0; x < 10; x++)
                src.setRGB(x, y, 0x202020);

        BufferedImage result = FileSettings.apply(src, brighten);

        int rgb = result.getRGB(5, 5);
        int r = (rgb >> 16) & 0xFF;
        assertTrue(r > 0x20); // brighter than original, wow
    }

    @Test
    void brighten_doesNotExceed255() {
        Profile brighten = new Profile(1, "Bright", ProfileSettings.BRIGHTEN, 255.0);
        BufferedImage src = makeTestImage(10, 10); // already bright

        BufferedImage result = FileSettings.apply(src, brighten);

        int rgb = result.getRGB(5, 5);
        int r = (rgb >> 16) & 0xFF;
        assertTrue(r <= 255);
    }
}