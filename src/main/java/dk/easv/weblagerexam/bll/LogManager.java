package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Log;
import dk.easv.weblagerexam.be.LogLevel;
import dk.easv.weblagerexam.be.LogType;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class LogManager {
    private DAOManager dao = new DAOManager();

    private int currentUserId() {return SessionManager.getCurrentUser().getId();}

    private void add(int userId, String action, String description, LogType type, LogLevel level) {
        String username = "SYSTEM";
        if (SessionManager.getCurrentUser() != null) {username = SessionManager.getCurrentUser().getUsername();}

        Log log = new Log(userId, action, description, type, level
        );

        log.setUsername(username);
        dao.getLogDAO().addLog(log);
    }

    private static final int SYSTEM_USER = 24; // for logging system events where no user is involved

    public List<Log> getAllLogs() {
        return dao.getLogDAO().getAllLogs();
    }

//Files
    public void logFileCreated( int userId, int fileNumber ) {
        add(
                userId, "FILE CREATED", "File created: " + fileNumber,
                LogType.AUDIT,
                LogLevel.INFO
        );
    }

    public void logFileDeleted( int userId, String fileName ) {
        add(
                userId, "FILE DELETED", "File deleted: " + fileName + " by "
                        + SessionManager.getCurrentUser().getUsername(),
                LogType.AUDIT,
                LogLevel.INFO
        );
    }

    public void logFileMoved( int fileNumber, int fromDocId, int toDocId) {
        add(currentUserId(), "FILE MOVED",
                "File #" + fileNumber + " moved from Doc #" + fromDocId + " to Doc #" + toDocId,
                LogType.AUDIT,
                LogLevel.INFO);
    }
// Documents
    public void logDocumentCreated(int documentId, int boxId) {
        add(currentUserId(), "DOCUMENT CREATED", "Document #" + documentId + " created in Box #" + boxId,
                LogType.AUDIT,
                LogLevel.INFO);
    }

    public void logDocumentFinalized(int docId, int boxId) {
        add(currentUserId(), "DOCUMENT FINALIZED", "Document #" + docId + " finalized in Box #" + boxId,
                LogType.AUDIT,
                LogLevel.INFO);
    }

    public void logManualDocumentSplit(int userId, int originalDocNumber, int newDocNumber, int splitAtFile) {
        add(userId, "DOCUMENT MANUALLY SPLIT",
                "Doc #" + originalDocNumber + " manually split at file #" + splitAtFile
                        + " → new Doc #" + newDocNumber,
                LogType.AUDIT,
                LogLevel.INFO);
    }
    public void logDuplicateBarcodeAccepted(int userId, String barcodeContent) {
        add(userId, "DUPLICATE BARCODE ACCEPTED",
                "Duplicate barcode accepted: " + barcodeContent,
                LogType.AUDIT,
                LogLevel.WARN
        );
    }


//Boxes
    public void logBoxCreated( int userId, int boxId, String username) {
        add(
                userId, "BOX CREATED", "Box #" + boxId + " was created by " + username,
                LogType.AUDIT,
                LogLevel.INFO
        );
    }

    public void logBoxDeleted(int userId, int boxId) {
        add(
                userId, "BOX DELETED", "Box #" + boxId + " deleted",
                LogType.AUDIT,
                LogLevel.INFO
        );
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
        add(adminId, "PROFILE ASSIGNED",
                "Profile '" + profileName + "' assigned to user #" + targetUserId,
                LogType.AUDIT,
                LogLevel.INFO);
    }

    public void logProfileRemoved(int adminId, String profileName, String username) {

        add(
                adminId, "PROFILE REMOVED", "Profile '" + profileName +
                        "' removed from user " + username,
                LogType.AUDIT,
                LogLevel.INFO
        );
    }

//users

    public void logUserCreated(String createdUsername)
    {
        String adminUsername = SessionManager.getCurrentUser().getUsername();
        add(currentUserId(), "NEW USER CREATED",
                "Admin " + adminUsername + " created user "
                        + createdUsername,
                LogType.AUDIT,
                LogLevel.INFO
        );
    }

    public void logUserDeactivated(int adminId, String adminUsername, String targetUsername)
    {
        add(adminId,
                "USER DEACTIVATED",
                "Admin " + adminUsername + " deactivated user " + targetUsername,
                LogType.AUDIT,
                LogLevel.INFO);
    }

    public void logUserActivated(int adminId, String adminUsername, String targetUsername
    ) {
        add(adminId,
                "USER ACTIVATED",
                "Admin " + adminUsername + " activated user " + targetUsername,
                LogType.AUDIT,
                LogLevel.INFO);
    }

    public void logUserUpdated(int adminId, String adminUsername, String targetUsername)
    {
        add(adminId,
                "USER UPDATED",
                "Admin " + adminUsername +
                        " updated user " + targetUsername,
                LogType.AUDIT,
                LogLevel.INFO);
    }

    public void logPasswordChanged(
            String targetUsername) {

        add(
                currentUserId(), "PASSWORD CHANGED", "Password changed for user " + targetUsername,
                LogType.SECURITY,
                LogLevel.INFO
        );
    }

//Exports

    public void logBoxExported(int boxId, String mode) {
        add(currentUserId(), "BOX EXPORTED",
                "Box #" + boxId + " exported as " + mode,
                LogType.AUDIT,
                LogLevel.INFO);
    }

    public void logExportFailed(int boxId, String reason) {
        add(
                currentUserId(), "EXPORT FAILED", "Box #" + boxId + " export failed: " + reason,
                LogType.APPLICATION,
                LogLevel.ERROR
        );
    }

//Login

    public void logLoginSuccess(String username) {
        add(
                currentUserId(), "LOGIN SUCCESS", "User " + username + " logged in",
                LogType.SECURITY,
                LogLevel.INFO
        );
    }
    public void logLogout(String username) {
        add(
                currentUserId(), "LOGOUT", "User " + username + " logged out",
                LogType.SECURITY,
                LogLevel.INFO
        );
    }

    public void logLoginFailed(String initials) {
        add(SYSTEM_USER,
                "LOGIN FAILED", "Failed login attempt for initials: " + initials,
                LogType.SECURITY,
                LogLevel.WARN
        );
            //LogUserId because we don't have a userId to log, since the login failed and db won't let me put 0
    }

}
