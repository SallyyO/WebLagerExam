package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.dal.DAOManager;

public class PasswordManager {
    DAOManager dao = new DAOManager();
    User user;

   public boolean checkLogin(String initials, String password) {
        try {
            user = dao.getUserDAO().getUser(initials);
            return PasswordHasher.verifyPassword(password, user.getPassword(), user.getSalt());
        }  catch (Exception e) {
            return false;
        }
    }

    public void AddUser(String role, String username, String initials, String password) throws Exception {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);
        User newUser = new User(username, role, initials, hash, salt);
        dao.getUserDAO().addUser(newUser);
    }

    public User getUser() {
        return user;
    }

    public void editUser(int id,String username, String initials, String role, String password) throws Exception {
        User existingUser = dao.getUserDAO().getUserById(id);

       if(password == null || password.isEmpty() ) {
           password = existingUser.getPassword();
           System.out.println("LN33 password: " + password);
       }
       else{
           password = PasswordHasher.hashPassword(password, existingUser.getSalt());
       }
       User editedUser = new User(id, username, role, initials, password, existingUser.getSalt());

       dao.getUserDAO().editUser(editedUser);
    }




}

