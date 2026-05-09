package dk.easv.weblagerexam.be;

public class Box {
    private int id;
    private int userId;
    private int profileId; // foreign key back to Profile
    private Profile profile; //gets populated after selection in the prescan
    private boolean isDeleted;

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
    @Override
    public String toString() {
        return "Box #" + id;
    }

}
