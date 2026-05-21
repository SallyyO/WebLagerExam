package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.dal.FileDAO;

import java.util.List;

public class FileManager {
    private final FileDAO fileDAO = new FileDAO();

    LogManager logManager = new LogManager();

    public List<File> getAllFiles() {
        return fileDAO.getAllFiles();
    }

    public List<File> searchFiles(String searchText) {
        return fileDAO.searchFiles(searchText);
    }

    public void hardDeleteFile(int fileId) {
        fileDAO.hardDeleteFile(fileId);
    }
}
