package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.User;

import java.util.HashMap;
import java.util.Map;

public class MockAuthService {

    //Added in case we wanted to try logging in before we got database set up
    //(And also bc i was tired of "database" errors xdd)

    private final Map<String, String> users = new HashMap<>();

    public MockAuthService() {
        users.put("admin", "1234");
        users.put("user", "password");
    }

    public User login(String name, String password) {
        if (users.containsKey(name)
                && users.get(name).equals(password)) {

            return new User(name, "Admin");
        }
        return null;
    }
}
