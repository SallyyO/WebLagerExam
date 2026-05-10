package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.File;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.*;

public class ApiDAO {
    private static final String BASE_URL =
            "https://studentiffapi-production.up.railway.app";
    private final HttpClient client = HttpClient.newHttpClient();
    private int currentIndex = 0;
    private int totalCount = -1;

    public int getTotalCount() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/getCount"))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            totalCount = Integer.parseInt(response.body().trim());
            System.out.println("Total files available: " + totalCount);
            return totalCount;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMore() {
        if (totalCount == -1) getTotalCount();
        return currentIndex < totalCount;
    }

    public File fetchNext() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/getRandomFile"))
                    .GET()
                    .build();
            HttpResponse<byte[]> response =
                    client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] data = response.body();
            System.out.println("Bytes received: " + data.length);

            boolean isBarcode = containsBarcode(data);
            System.out.println("Contains barcode: " + isBarcode);

            currentIndex++;
            return new File(data, isBarcode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uses ZXing to attempt to decode any barcode in the image.
     * Looks at pages to see if there are any barcodes — ZXing will decode them hopefully
     * ignore this stuff for now tho
     */
    private boolean containsBarcode(byte[] data) {
        try {
            BufferedImage image = readTiff(data);
            if (image == null) return false;

            // Downsample a bit for speed
            int targetWidth = 800;
            int targetHeight = (int) (image.getHeight() * (800.0 / image.getWidth()));
            java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(
                    targetWidth, targetHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
            scaled.getGraphics().drawImage(
                    image.getScaledInstance(targetWidth, targetHeight,
                            java.awt.Image.SCALE_SMOOTH), 0, 0, null);

            LuminanceSource source = new BufferedImageLuminanceSource(scaled);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Use multi-barcode reader since the separator sheet has TWO barcodes
            //Probably doesn't matter since the second barcode isn't really usuable rn
            MultipleBarcodeReader multiReader =
                    new GenericMultipleBarcodeReader(new MultiFormatReader());

            Result[] results = multiReader.decodeMultiple(bitmap);

            if (results != null && results.length > 0) {
                for (Result r : results) {
                    System.out.println("  Barcode decoded: " + r.getText()
                            + " [" + r.getBarcodeFormat() + "]");
                }
                return true;
            }
            return false;

        } catch (NotFoundException e) {
            return false; // No barcode found — normal document page
        } catch (Exception e) {
            System.out.println("Barcode detection error: " + e.getMessage());
            return false;
        }
    }


    public static BufferedImage readTiff(byte[] data) {
        try {
            return ImageIO.read(new ByteArrayInputStream(data));
        } catch (Exception e) {
            return null;
        }
    }

    public int getCurrentIndex() { return currentIndex; }
}
