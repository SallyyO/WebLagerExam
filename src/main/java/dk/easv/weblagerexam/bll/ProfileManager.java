package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.dal.ProfileDAO;

import java.util.List;

public class ProfileManager {
    private final DAOManager dao =  new DAOManager();
     private final LogManager  logManager = new LogManager();

    public void createProfile(String name) {
        // name cannot be empty
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be null");
        }
        // name shouldn't be too short
        if(name.trim().length() < 3) {
            throw new IllegalArgumentException("Profile name cannot be less than 3 characters");
        }
        // no same profile names
        for(Profile existing : dao.getProfileDAO().getAllProfiles())
        if(existing.getName().equalsIgnoreCase(name.trim())){
            throw new IllegalArgumentException("Profile already exists");
        }

        dao.getProfileDAO().addProfile(new Profile(name.trim()));
    }

    public void deleteProfile(Profile profileID) throws Exception {
        dao.getProfileDAO().softDeleteProfile(profileID);

        logManager.logProfileDeleted(profileID.getId(), "Profile #" + profileID.getName());

    }

    public List<Profile> getAllProfiles() {
        return dao.getProfileDAO().getAllProfiles();
    }

}
