package dk.easv.weblagerexam.be;

import java.util.ArrayList;
import java.util.List;

public class Box {
    private int id;
    private int userId;
    private int profileId; // foreign key back to Profile
    private Profile profile; //gets filled after selection in the prescan
    private boolean isDeleted;
    private int boxId;
    private String metaData;
    private List<Document> documents = new ArrayList<>();


    public Box(int id, int profileId) {
        this.id = id;
        this.profileId = profileId;
    }

    public Box (int id, int userId, int profileId) {
        this.id = id;
        this.userId = userId;
        this.profileId = profileId;
    }

    public Box (int userId){
        this.userId = userId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getProfileId() {
        return profileId;
    }
    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }
    public int getBoxId() {return boxId;}
    public void setBoxId(int boxId) {this.boxId = boxId;}

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public List<Document> getDocuments() {return documents;}
    public void setDocuments(List<Document> documents) {this.documents = documents;}

    @Override
    public String toString() {
        return "Box #" + id;
    }

}
