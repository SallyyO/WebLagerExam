package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.File;
import dk.easv.weblagerexam.dal.LocalTiffDAO;

public class LocalTiffManager {
    private final LocalTiffDAO localTiffDAO =
            new LocalTiffDAO();

    public File fetchNext() {
        return localTiffDAO.fetchNext();
    }
}
