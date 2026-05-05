package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.dal.BoxDAO;
import dk.easv.weblagerexam.dal.DAOManager;

public class BoxManager {
    private DAOManager dao =  new DAOManager();
    LogManager logManager = new LogManager();

    public void createBox(int profileId){
        //profileId mustbe valid
        if(profileId <= 0){
            throw new IllegalArgumentException("Profile ID must be greater than 0");
        }
        dao.getBoxDAO().addBox(new Box(profileId));
    }
    public void deleteBox(int boxId){
        if(boxId <= 0){
            throw new IllegalArgumentException("Box ID must be greater than 0");
        }
        dao.getBoxDAO().softDeleteBox(boxId);

        logManager.logBoxDeleted(boxId, boxId );
    }
}
