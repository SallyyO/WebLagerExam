package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;

public class ExportManager {
    // Export a list of TIFF byte arrays as one multi-page TIFF file
    // a list of TIFF images stored as byte arrays,each byte[] represents one page/image
    public void exportMultiPageTiff(List<byte[]>tiffPages, String exportFolderPath, String profileName, String boxId) throws Exception{
        if(tiffPages==null || tiffPages.isEmpty()){
            throw new IllegalArgumentException("No pages to export");
        }

        // Build file name: {profileName}_{boxId}.tiff
        String fileName = profileName+"_"+boxId+".tif";
        File outputFile = new File(exportFolderPath, fileName);

        // Get TIFF image writer of Java
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
        if(!writers.hasNext()){
            throw new Exception("No TIFF image writer found");
        }
        ImageWriter writer = writers.next(); // gets the first available TIFF writer

        //creates a stream connected to the output TIFF file
        try(FileImageOutputStream output = new FileImageOutputStream(outputFile)){
            writer.setOutput(output); // prepare or tell the writer to write multiple images into one TIFF
            writer.prepareWriteSequence(null);

            for(byte[] pageBytes : tiffPages){
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(pageBytes)); // convert pageBytes to raw TIFF bytes, makes bytes readable like a file,converts  Bytes into a Bufferedimage
                if(image != null){
                    writer.writeToSequence(new IIOImage(image,null,null),null); // adds the image as another page in tiff
                }
            }
            writer.endWriteSequence(); // end the writing process

        } finally {
            writer.dispose(); // release resources used by tiff writer
        }
    }

}
