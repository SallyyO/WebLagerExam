package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.dal.ProfileDAO;

import java.util.List;

public class ProfileManager {
    private ProfileDAO profileDAO;
    public ProfileManager() {
        profileDAO = new ProfileDAO();
    }

    public void createProfile(String name) {
        // name cannot be empty
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be null");
        }
        // name shouldn't be too shirt
        if(name.trim().length() < 3) {
            throw new IllegalArgumentException("Profile name cannot be less than 3 characters");
        }
        // no same profile names
        for(Profile existing : profileDAO.getAllProfiles())
        if(existing.getName().equalsIgnoreCase(name.trim())){
            throw new IllegalArgumentException("Profile already exists");
        }

        profileDAO.addProfile(new Profile(name.trim()));
    }

    public List<Profile> getAllProfiles() {
        return profileDAO.getAllProfiles();
    }

}
