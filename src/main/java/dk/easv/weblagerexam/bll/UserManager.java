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
                || (!role.equals("Admin")
                && !role.equals("User"))) {
            throw new Exception(
                    "Role must be either admin or user"
            );
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

        User newUser = new User(username.trim(), role, initials.trim(), hash, salt);
        dao.getUserDAO().addUser(newUser);

        logManager.logUserCreated(newUser.getUsername());
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

        Profile profile = dao.getProfileDAO().getProfileById(profileId);

        logManager.logProfileAssigned(
                SessionManager.getCurrentUser().getId(),
                userId,
                profile.getName()
        );
    }

    public void removeAllProfilesFromUser(int userId) throws Exception {
        dao.getUserDAO().removeAllProfilesFromUser(userId);
    }

    public List<Profile> getProfilesForUser(int userId) throws Exception {
        return dao.getUserDAO().getProfilesForUser(userId);
    }

    public List<User> searchUsers(String searchText) {return dao.getUserDAO().searchUsers(searchText);}


    public void setUserActive(int userId, boolean active) {

        User targetUser = dao.getUserDAO().getUserById(userId);

        if (targetUser == null) {return;}

        // Don't log if nothing changed
        if (targetUser.isActive() == active) {return;}

        dao.getUserDAO().setUserActive(userId, active);

        User admin = SessionManager.getCurrentUser();

        if (active) {
            logManager.logUserActivated(
                    admin.getId(),
                    admin.getUsername(),
                    targetUser.getUsername()
            );
        } else {
            logManager.logUserDeactivated(
                    admin.getId(),
                    admin.getUsername(),
                    targetUser.getUsername()
            );
        }
    }
}
