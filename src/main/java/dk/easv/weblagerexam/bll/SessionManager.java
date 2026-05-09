package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.User;

public class SessionManager {

    private static User currentUser;

    public static void setCurrentUser(User user) {currentUser = user;}
    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
