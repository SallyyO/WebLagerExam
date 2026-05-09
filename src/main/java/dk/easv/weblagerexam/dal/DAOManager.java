package dk.easv.weblagerexam.dal;

public class DAOManager {
    private UserDAO userDAO = new UserDAO();
    private ProfileDAO profileDAO = new ProfileDAO();
    private BoxDAO boxDAO = new BoxDAO();
    private LogDAO logDAO = new LogDAO();

    public DAOManager() {
        userDAO = new UserDAO();
    }

    public UserDAO getUserDAO()
    {
        return userDAO;
    }
    public ProfileDAO  getProfileDAO()
    {
        return profileDAO;
    }
    public BoxDAO getBoxDAO(){
        return boxDAO;
    }
    public LogDAO getLogDAO(){
        return logDAO;
    }
}
