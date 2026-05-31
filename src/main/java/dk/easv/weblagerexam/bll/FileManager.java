package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class FileManager {
    private final DAOManager dao = new DAOManager();

    LogManager logManager = new LogManager();

    public List<File> getAllFiles() {
        return dao.getFileDAO().getAllFiles();
    }

    public List<File> searchFiles(String searchText) {
        return dao.getFileDAO().searchFiles(searchText);
    }

    public void hardDeleteFile(int fileId) {
        dao.getFileDAO().hardDeleteFile(fileId);
    }
}
