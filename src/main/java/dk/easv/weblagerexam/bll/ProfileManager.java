package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.ProfileSettings;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class ProfileManager {

    private final DAOManager dao =  new DAOManager();
    private final LogManager  logManager = new LogManager();



    public Profile createProfile(String name, ProfileSettings settings, Double value) {
        Profile profile = new Profile(name);
        profile.setSettings(settings);
        profile.setSettingsValue(value);
        dao.getProfileDAO().saveProfile(profile); // sets profile.id from DB

        return profile;
    }

    public List<Profile> getAllProfiles() {
        return dao.getProfileDAO().getAllProfiles();
    }

    public List<Profile> getProfilesForUser(int userId) {
        return dao.getProfileDAO().getProfilesForUser(userId);
    }

    public void assignProfileToUser(int userId, int profileId) {
        dao.getProfileDAO().assignProfileToUser(userId, profileId);
    }

    public void removeProfileFromUser(int userId, int profileId) {
        dao.getProfileDAO().removeProfileFromUser(userId, profileId);
    }

    public void deleteProfile(Profile profileID) throws Exception {
        dao.getProfileDAO().softDeleteProfile(profileID);
    }
    public List<Profile> getProfilesForClient(int clientId) {
        return dao.getProfileDAO().getProfilesForClient(clientId);
    }
}
