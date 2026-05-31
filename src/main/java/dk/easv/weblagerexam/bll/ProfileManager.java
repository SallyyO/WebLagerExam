package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Client;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.ProfileSettings;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class ProfileManager {

    private final DAOManager dao =  new DAOManager();
    private final LogManager  logManager = new LogManager();



    public Profile createProfile(String name, ProfileSettings settings, Double value) {
        Profile profile = new Profile(name);
        profile.setSettings(settings);
        profile.setSettingsValue(value);
        dao.getProfileDAO().saveProfile(profile);

        logManager.logProfileCreated(profile.getName());

        return profile;
    }

    public List<Profile> getAllProfiles() {
        return dao.getProfileDAO().getAllProfiles();
    }

    public List<Profile> getProfilesForUser(int userId) {
        return dao.getProfileDAO().getProfilesForUser(userId);
    }

    public List<Profile> getAllProfilesWithClients() {
        return dao.getProfileDAO().getAllProfilesWithClients();
    }
    public List<Profile> getProfilesForClient(int clientId) {return dao.getProfileDAO().getProfilesForClient(clientId);}

    public Profile getProfileById(int profileId) {
        return dao.getProfileDAO().getProfileById(profileId);
    }

    public void assignProfileToUser(int userId, int profileId) {
        dao.getProfileDAO().assignProfileToUser(userId, profileId);

        Profile profile = dao.getProfileDAO().getProfileById(profileId);
        User admin = SessionManager.getCurrentUser();

        logManager.logProfileAssigned(admin.getId(), userId, profile.getName());
    }

    public void removeProfileFromUser(int userId, int profileId) {
        dao.getProfileDAO().removeProfileFromUser(userId, profileId);

        User targetUser = dao.getUserDAO().getUserById(userId);
        Profile profile = dao.getProfileDAO().getProfileById(profileId);
        User admin = SessionManager.getCurrentUser();

        logManager.logProfileRemoved(
                admin.getId(),
                profile.getName(),
                targetUser.getUsername()
        );
    }

    public void assignProfileToClient(int profileId, int clientId)
    {
        dao.getProfileDAO().assignProfileToClient(profileId, clientId);

        Profile profile = dao.getProfileDAO().getProfileById(profileId);

        Client client = dao.getClientDAO().getClientById(clientId);

        logManager.logProfileAssignedToClient(
                profile.getName(),
                client.getName()
        );
    }

    public void removeProfileFromClient(int profileId)
    {
        //Fetch before removal for logging so everything is still available
        Profile profile = dao.getProfileDAO().getProfileById(profileId);
        Client client = dao.getClientDAO().getClientById(profile.getClientId());

        dao.getProfileDAO().removeProfileFromClient(profileId);

        if (client != null) {
            logManager.logProfileRemovedFromClient(
                    profile.getName(),
                    client.getName()
            );
        }
    }

    public void deleteProfile(Profile profileID) throws Exception {
        dao.getProfileDAO().softDeleteProfile(profileID);
    }
}
