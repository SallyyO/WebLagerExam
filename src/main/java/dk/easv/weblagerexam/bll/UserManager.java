package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class UserManager {
    private DAOManager dao = new DAOManager();
    private LogManager logManager = new LogManager();

    public List<User> getAllUsers() throws Exception {
        return dao.getUserDAO().getAllUsers();

    }

    public void createUser(String username, String role, String initials, String password) throws Exception {
        // fields can't be empty
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("Username can't be empty");
        }
        if (role == null || role.trim().isEmpty()
                || !role.equals("Admin")
                || !role.equals("User")) {
            throw new Exception("Role can't be empty and need to be \"Admin\" or \"User\"");
        }
        if (initials == null || initials.trim().isEmpty()) {
            throw new Exception("Initials can't be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Password can't be empty");
        }

        // No duplicate usernames
        /*for(User existing : dao.getUserDAO().getAllUsers()){
            if(existing.getUsername().equalsIgnoreCase(username.trim())){
                throw new IllegalArgumentException("Username already exists");
            }*/
        if (dao.getUserDAO().getUser(username) != null) {
            throw new Exception("Username already exists");
        }

        // hash the password before saving
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);

        dao.getUserDAO().addUser(new User(username.trim(), role, initials.trim(), hash, salt));

    }

    public void deleteUser(int userID) throws Exception {
        dao.getUserDAO().softDeleteUser(userID);
        logManager.logFileDeleted(userID, "User #" + userID);
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


    public List<User> searchUsers(String searchText) {return dao.getUserDAO().searchUsers(searchText);}
}
