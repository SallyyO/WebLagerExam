package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class BoxManager {

    private final DAOManager dao = new DAOManager();
    private final LogManager logManager = new LogManager();

    public void createBox(int profileId) {
        if (profileId <= 0) {
            throw new IllegalArgumentException(
                    "Profile ID must be greater than 0"
            );
        }
        dao.getBoxDAO().addBox(new Box(profileId)
        );
    }

    public int saveBox(Box box) {
        if (box == null) {
            throw new IllegalArgumentException(
                    "Box cannot be null"
            );
        }
        int boxDbId = dao.getBoxDAO().saveBox(box);

        logManager.logBoxCreated(
                box.getUserId(),
                box.getBoxId(),      // user-entered box number
                SessionManager.getCurrentUser().getUsername()
        );
        return boxDbId;
    }

    public List<Box> getBoxesByProfileId(int profileId) {
        if (profileId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid profile ID"
            );
        }

        return dao.getBoxDAO().getBoxesByProfileId(profileId);
    }

    public List<Box> getAllBoxes() {
        return dao.getBoxDAO().getAllBoxes();
    }

    public List<Box> getBoxesByUser(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user ID"
            );
        }

        return dao.getBoxDAO().getBoxesByUser(userId);
    }

    public void deleteBox(int boxId) {
        if (boxId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid box ID"
            );
        }
        dao.getBoxDAO().deleteBox(boxId);
        logManager.logBoxDeleted(
                SessionManager.getCurrentUser().getId(),
                boxId
        );
    }

    public Box getBoxById(int boxId) {
        Box box = dao.getBoxDAO().getBoxById(boxId);

        if (box != null) {
            Profile profile =
                    dao.getProfileDAO().getProfileById
                            (box.getProfileId());

            box.setProfile(profile);
        }

        return box;
    }
}
