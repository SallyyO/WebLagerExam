package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.ProfileSettings;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class FileSettingsTest {

    @Test
    void applyReturnsOriginalImageWhenSourceOrProfileIsMissing() {
        BufferedImage image = createImage(2, 2, Color.RED);

        assertSame(image, FileSettings.apply(image, null));
        assertEquals(null, FileSettings.apply(null, profile(ProfileSettings.GRAYSCALE, 0)));
    }

    @Test
    void brightenIncreasesChannelsAndCapsAt255() {
        BufferedImage image = createImage(1, 1, new Color(250, 100, 10));

        BufferedImage result = FileSettings.apply(image, profile(ProfileSettings.BRIGHTEN, 20));
        Color color = new Color(result.getRGB(0, 0));

        assertEquals(255, color.getRed());
        assertEquals(120, color.getGreen());
        assertEquals(30, color.getBlue());
        assertNotSame(image, result);
    }

    @Test
    void brightenNegativeAmountLeavesPixelUnchanged() {
        BufferedImage image = createImage(1, 1, new Color(25, 50, 75));

        BufferedImage result = FileSettings.apply(image, profile(ProfileSettings.BRIGHTEN, -10));
        Color color = new Color(result.getRGB(0, 0));

        assertEquals(25, color.getRed());
        assertEquals(50, color.getGreen());
        assertEquals(75, color.getBlue());
    }

    @Test
    void rotateAutoRotatesPortraitImagesToLandscape() {
        BufferedImage image = createImage(2, 4, Color.BLUE);

        BufferedImage result = FileSettings.apply(image, profile(ProfileSettings.ROTATE_AUTO, 0));

        assertEquals(4, result.getWidth());
        assertEquals(2, result.getHeight());
    }

    @Test
    void rotateAutoReturnsLandscapeImagesUnchanged() {
        BufferedImage image = createImage(4, 2, Color.BLUE);

        BufferedImage result = FileSettings.apply(image, profile(ProfileSettings.ROTATE_AUTO, 0));

        assertSame(image, result);
    }

    @Test
    void gryffindorTintUsesExpectedHouseColor() {
        BufferedImage image = createImage(1, 1, new Color(255, 255, 255));

        BufferedImage result = FileSettings.apply(image, profile(ProfileSettings.GRYFFINDOR, 0));
        Color color = new Color(result.getRGB(0, 0));

        assertEquals(116, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(1, color.getBlue());
    }

    private static Profile profile(ProfileSettings setting, double value) {
        return new Profile(1, "Test", setting, value);
    }

    private static BufferedImage createImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }
}
