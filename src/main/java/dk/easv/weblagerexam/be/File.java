package dk.easv.weblagerexam.be;

import javafx.scene.image.Image;

public class File {
    private int id;
    private String fileName;
    private int pageOrder;
    private int rotation;
    private boolean isDeleted;
    private int document_id;

    private int fileNumber;
    private byte[] imageData;
    private boolean isBarcode;
    private String barcodeContent;

    private transient Image thumbnailCache;
    private transient Image fullImageCache;
    private transient int cachedProfileId = -1;
    private transient int fullImageProfileId = -1;

    public File(byte[] imageData, boolean isBarcode) {
        this.imageData = imageData;
        this.isBarcode = isBarcode;
    }

    public void clearImageCache() {
        thumbnailCache = null;
        fullImageCache = null;
    }


    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public int getFileNumber() {return fileNumber;}
    public void setFileNumber(int fileNumber) {this.fileNumber = fileNumber;}

    public byte[] getImageData() {return imageData;}
    public void setImageData(byte[] imageData) {this.imageData = imageData;}

    public int getDocumentId() {return document_id;}
    public void setDocumentId(int document_id) {this.document_id = document_id;}

    public boolean isBarcode() {return isBarcode;}
    public void setBarcode(boolean isBarcode) {this.isBarcode = isBarcode;}

    public String getBarcodeContent() { return barcodeContent; }
    public void setBarcodeContent(String barcodeContent) { this.barcodeContent = barcodeContent; }

    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {isDeleted = deleted;}

    public Image getThumbnailCache() {return thumbnailCache;}
    public void setThumbnailCache(Image thumbnailCache) {this.thumbnailCache = thumbnailCache;}

    public Image getFullImageCache() {return fullImageCache;}
    public void setFullImageCache(Image fullImageCache) {this.fullImageCache = fullImageCache;}

    public int getCachedProfileId() {return cachedProfileId;}
    public void setCachedProfileId(int cachedProfileId) {this.cachedProfileId = cachedProfileId;}

    public int getFullImageProfileId() {return fullImageProfileId;}
    public void setFullImageProfileId(int fullImageProfileId) {this.fullImageProfileId = fullImageProfileId;}
}
