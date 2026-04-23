package dk.easv.weblagerexam.be;

public class Box {
    private int id;
    private int profileId; // foreign key back to Profile

    public Box(int id, int profileId) {
        this.id = id;
        this.profileId = profileId;
    }
    public Box(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getProfileId() {
        return profileId;
    }
    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
    @Override
    public String toString() {
        return "Box #" + id;
    }

}
