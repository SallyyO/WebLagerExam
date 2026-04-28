package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class UserManager {
    private DAOManager dao = new DAOManager();

    public List<User> getAllUsers() throws Exception{
        return dao.getUserDAO().getAllUsers();

    }
    public void assignProfileToUser(int userId, int profileId) throws Exception {
        // checking if it is already assigned
        List<Profile> existing = dao.getUserDAO().getProfilesForUser(userId);
        for (Profile p : existing) {
            if (p.getId() == profileId) {
                throw new IllegalArgumentException("Profile with id " + profileId + " already exists");
            }
        }
        dao.getUserDAO().addProfileToUser(userId, profileId);
    }

    public void removeProfileFromUser(int userId, int profileId) throws Exception {
        dao.getUserDAO().deleteProfileFromUser(userId, profileId);
    }

    public List<Profile> getProfilesForUser(int userId) throws Exception {
        return dao.getUserDAO().getProfilesForUser(userId);
    }
}
