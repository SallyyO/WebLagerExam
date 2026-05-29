package dk.easv.weblagerexam.dal;

public class DAOManager {
    private UserDAO userDAO = new UserDAO();
    private ProfileDAO profileDAO = new ProfileDAO();
    private BoxDAO boxDAO = new BoxDAO();
    private LogDAO logDAO = new LogDAO();
    private DocumentDAO documentDAO = new DocumentDAO();
    private LocalTiffDAO localTiffDAO = new LocalTiffDAO();
    private ApiDAO apiDAO = new ApiDAO();
    private FileDAO fileDAO = new FileDAO();
    private ClientDAO clientDAO = new ClientDAO();


    public UserDAO getUserDAO() {
        return userDAO;
    }

    public ProfileDAO getProfileDAO() {
        return profileDAO;
    }

    public BoxDAO getBoxDAO() {
        return boxDAO;
    }

    public LogDAO getLogDAO() {
        return logDAO;
    }

    public DocumentDAO getDocumentDAO() {
        return documentDAO;
    }

    public LocalTiffDAO getLocalTiffDAO() {
        return localTiffDAO;
    }

    public ApiDAO getApiDAO() {
        return apiDAO;
    }

    public FileDAO getFileDAO() {return fileDAO;};

    public ClientDAO getClientDAO() {return clientDAO;}
}

