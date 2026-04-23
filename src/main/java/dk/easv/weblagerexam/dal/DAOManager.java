package dk.easv.weblagerexam.dal;

public class DAOManager {
    private UserDAO userDAO = new UserDAO();

    public DAOManager() {
        userDAO = new UserDAO();
    }

    public UserDAO getUserDAO()
    {
        return userDAO;
    }
}
