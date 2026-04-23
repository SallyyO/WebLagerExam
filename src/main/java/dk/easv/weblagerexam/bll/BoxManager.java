package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.dal.BoxDAO;

public class BoxManager {
    private BoxDAO boxDAO ;

    public BoxManager() {
        boxDAO = new BoxDAO();

    }
    public void createBox(int profileId){
        //profileId mustbe valid
        if(profileId <= 0){
            throw new IllegalArgumentException("Profile ID must be greater than 0");
        }
        boxDAO.addBox(new Box(profileId));
    }
    public void deleteBox(int boxId){
        if(boxId <= 0){
            throw new IllegalArgumentException("Box ID must be greater than 0");
        }
        boxDAO.deleteBox(boxId);
    }
}
