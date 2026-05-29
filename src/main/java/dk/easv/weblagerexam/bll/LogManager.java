package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Log;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class LogManager {
    private DAOManager dao = new DAOManager();

    private int currentUserId() {return SessionManager.getCurrentUser().getId();}

    private void add(int userId, String action, String description) {
        dao.getLogDAO().addLog(new Log(userId, action, description));
    }

    private static final int SYSTEMUser = 24; // for logging system events where no user is involved

    public List<Log> getAllLogs() {
        return dao.getLogDAO().getAllLogs();
    }

//Files
    public void logFileCreated( int userId, String fileName ) {
        Log log = new Log(userId,"FILE CREATED", "File created:" + fileName);
        dao.getLogDAO().addLog(log);
    }

    public void logFileDeleted( int userId, String fileName ) {
        Log log = new Log(userId,"FILE DELETED", "This file was deleted: " + fileName + "by" + userId);
        dao.getLogDAO().addLog(log);
    }

    public void logFileMoved( int fileNumber, int fromDocId, int toDocId) {
        add(currentUserId(), "FILE MOVED",
                "File #" + fileNumber + " moved from Doc #" + fromDocId + " to Doc #" + toDocId);
    }
// Documents
public void logDocumentCreated(int docId, int boxId) {
    add(currentUserId(), "DOCUMENT CREATED", "Document #" + docId + " created in Box #" + boxId);
}
    public void logDocumentDeleted(int userId, int docId) {
        add(userId, "DOCUMENT DELETED", "Document #" + docId + " deleted");
    }
    public void logManualDocumentSplit(int userId, int originalDocId, int newDocId, int splitAtFile) {
        add(userId, "DOCUMENT MANUALLY SPLIT",
                "Doc #" + originalDocId + " split at file #" + splitAtFile
                        + " → new Doc #" + newDocId);
    }
    public void logDuplicateBarcodeAccepted(int userId, String barcodeContent) {
        add(userId, "DUPLICATE BARCODE ACCEPTED",
                "Duplicate barcode accepted: " + barcodeContent
        );
    }


//Boxes
    public void logBoxCreated( int userId, int boxId, String username) {
        Log log = new Log(userId,"BOX CREATED", "Box #" + boxId + " was created by " + username);
        dao.getLogDAO().addLog(log);
    }

    public void logBoxDeleted(int userId, int boxId) {
        Log log = new Log(userId,"BOX DELETED", "Box deleted: Box #" + boxId);
        dao.getLogDAO().addLog(log);
    }


// Profiles

    /*
    public void logProfileCreated(int userId, String profileName) {
        add(userId, "PROFILE_CREATED", "Profile created: " + profileName);
    }
    public void logProfileDeleted(int userId, String profileName) {
        add(userId, "PROFILE_DELETED", "Profile deleted: " + profileName);
    }

   */
    public void logProfileAssigned(int adminId, int targetUserId, String profileName) {
        add(adminId, "PROFILE_ASSIGNED",
                "Profile '" + profileName + "' assigned to user #" + targetUserId);
    }

//users

    public void logUserCreated(String createdUsername)
    {
        String adminUsername = SessionManager.getCurrentUser().getUsername();
        add(currentUserId(), "NEW USER CREATED",
                "Admin " + adminUsername + " created user "
                        + createdUsername
        );
    }
    public void logUserDeleted( String userName ) {
        Log log = new Log(currentUserId(),"USER DELETED", "User deleted:" + userName);
        dao.getLogDAO().addLog(log);
    }
//Exports

    public void logBoxExported(int boxId, String mode) {
        add(currentUserId(), "BOX EXPORTED",
                "Box #" + boxId + " exported as " + mode);
    }

//Login
public void logLoginFailed(String initials) {
        add(SYSTEMUser,
                "LOGIN FAILED", "Failed login attempt for initials: " + initials
    );
        //LogUserId because we don't have a userId to log, since the login failed and db won't let me put 0
}


}
