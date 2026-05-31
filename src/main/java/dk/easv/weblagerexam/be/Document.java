package dk.easv.weblagerexam.be;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Document {
    private int id;
    private int boxId;
    private String metadata; // user fills this in after scanning?
    private List<File> files = new ArrayList<>();
    private LocalDateTime createdAt = LocalDateTime.now();
    private int documentNumber;
    private boolean isDeleted;
    private int fileCount;

    public Document() {
        this.createdAt = LocalDateTime.now();
    }

    public void addFile(File file) {
        file.setFileNumber(files.size() + 1);
        files.add(file);
    }

    public void reorderFiles(int oldIndex, int newIndex) {
        if (oldIndex < 0 || newIndex < 0
                || oldIndex >= files.size()
                || newIndex >= files.size()) return;

        File moved = files.remove(oldIndex);
        files.add(newIndex, moved);

        // Re-number pages to show the new order
        for (int i = 0; i < files.size(); i++) {
            files.get(i).setFileNumber(i + 1);
        }
    }

    public List<File> getFiles()  { return files; }
    public void setFiles(List<File> files) { this.files = files; }
    public int getFileCount(){ return files.size(); }
    public int setFileCount(int fileCount){ this.fileCount = fileCount; return files.size(); }

    public boolean isEmpty(){return files.isEmpty(); }

    public int getId(){ return id; }
    public void setId(int id){ this.id = id; }

    public int getBoxId() { return boxId; }
    public void setBoxId(int boxId) { this.boxId = boxId; }

    public String getMetadata(){ return metadata; }
    public void setMetadata(String metadata){ this.metadata = metadata; }

    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }

    public int getDocumentNumber() {return documentNumber;}
    public void setDocumentNumber(int documentNumber) {this.documentNumber = documentNumber;}

    public boolean isDeleted() {return isDeleted;}
    public void setDeleted(boolean deleted) {isDeleted = deleted;}

}