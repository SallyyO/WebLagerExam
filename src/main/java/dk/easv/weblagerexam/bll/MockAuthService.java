package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.User;

import java.util.HashMap;
import java.util.Map;

public class MockAuthService {

    //Added in case we wanted to try logging in before we got database set up
    //(And also bc i was tired of "database" errors xdd)

    private final Map<String, User> users = new HashMap<>();

    public MockAuthService() {
        users.put("admin", new User("admin", "Admin", "", "1234", ""));
        users.put("user", new User("user", "User", "", "1234", ""));
    }

    public User login(String email, String password) {
        User user = users.get(email);

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
