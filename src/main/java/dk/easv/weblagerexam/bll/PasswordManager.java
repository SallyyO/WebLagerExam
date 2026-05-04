package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.dal.DAOManager;

public class PasswordManager {
    DAOManager dao = new DAOManager();
    User user;

   public boolean checkLogin(String login, String password) {
        try {
            user = dao.getUserDAO().getUser(login);
            System.out.println("PasswordManager: " + user.getUsername());
            return PasswordHasher.verifyPassword(password, user.getPassword(), user.getSalt());
        }  catch (Exception e) {
            return false;
        }
    }

    public void AddUser(String role, String username, String login, String password) throws Exception {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);
        User newUser = new User( username,role, login, hash, salt);
        dao.getUserDAO().addUser(newUser);
    }

    public User getUser() {
        return user;
    }
}

