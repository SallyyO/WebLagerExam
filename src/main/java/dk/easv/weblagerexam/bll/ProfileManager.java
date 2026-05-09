package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.ProfileSettings;
import dk.easv.weblagerexam.dal.DAOManager;
import dk.easv.weblagerexam.dal.ProfileDAO;

import java.util.List;

public class ProfileManager {

    private final ProfileDAO profileDAO = new ProfileDAO();
    private final DAOManager dao =  new DAOManager(); //Idk why it didn't want to work, i kept getting errors. This will be changed tho
    private final LogManager  logManager = new LogManager();



    public Profile createProfile(String name, ProfileSettings settings, Double value) {
        Profile profile = new Profile(name);
        profile.setSettings(settings);
        profile.setSettingsValue(value);
        profileDAO.saveProfile(profile); // sets profile.id from DB

        return profile;
    }

    public List<Profile> getAllProfiles() {
        return profileDAO.getAllProfiles();
    }

    public List<Profile> getProfilesForUser(int userId) {
        return profileDAO.getProfilesForUser(userId);
    }

    public void assignProfileToUser(int userId, int profileId) {
        profileDAO.assignProfileToUser(userId, profileId);
    }

    public void removeProfileFromUser(int userId, int profileId) {
        profileDAO.removeProfileFromUser(userId, profileId);
    }

    public void deleteProfile(Profile profileID) throws Exception {
        dao.getProfileDAO().softDeleteProfile(profileID);

        logManager.logProfileDeleted(profileID.getId(), "Profile #" + profileID.getName());

    }
}
