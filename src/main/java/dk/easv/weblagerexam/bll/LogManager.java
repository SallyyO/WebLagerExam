package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Log;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class LogManager {
    private DAOManager dao = new DAOManager();

    public void logFileCreated( int userId, String fileName ) {
        Log log = new Log(userId,"FILE_CREATED", "FIle created:" + fileName);
        dao.getLogDAO().addLog(log);
    }

    public void logFileDeleted( int userId, String fileName ) {
        Log log = new Log(userId,"FILE_DELETED", "File deleted:" + fileName);
        dao.getLogDAO().addLog(log);
    }

    public List<Log> getAllLogs() {
        return dao.getLogDAO().getAllLogs();
    }
    public void logProfileCreated( int userId, String profileName ) {
        Log log = new Log(userId,"PROFILE_CREATED", "Profile created:" + profileName);
        dao.getLogDAO().addLog(log);
    }

    public void logProfileDeleted( int userId, String profileName ) {
        Log log = new Log(userId,"PROFILE_DELETED", "Profile deleted:" + profileName);
        dao.getLogDAO().addLog(log);
    }

    public void logBoxCreated( int userId, int boxId ) {
        Log log = new Log(userId,"BOX_CREATED", "Box created: Box #" + boxId);
        dao.getLogDAO().addLog(log);
    }

    public void logBoxDeleted(int userId, int boxId) {
        Log log = new Log(1,"BOX_DELETED", "Box deleted: Box #" + boxId);
        dao.getLogDAO().addLog(log);
    }

    public void logUserCreated( int userId, String userName ) {
        Log log = new Log(userId,"USER_CREATED", "User created:" + userName);
        dao.getLogDAO().addLog(log);
    }
    public void logUserDeleted( int userId, String userName ) {
        Log log = new Log(1,"USER_DELETED", "User deleted:" + userName);
        dao.getLogDAO().addLog(log);
    }


}
