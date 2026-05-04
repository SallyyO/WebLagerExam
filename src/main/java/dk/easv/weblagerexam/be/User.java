package dk.easv.weblagerexam.be;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String role;
    private String email;
    private String password;
    private String salt;

    private List<Profile> profiles;

    public User(int id, String username, String role, String email, String password, String salt)
    {
        this.id = id;
        this.username = username;
        this.role = role;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.profiles = new ArrayList<>();
    }

    public User(String username, String role, String email, String password, String salt)
    {
        this.username = username;
        this.role = role;
        this.email = email;
        this.password = password;
        this.salt = salt;
    }

    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    public User(int id, String username, String role){
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public String getRole() {return role;}

    public void setRole(String role) {this.role = role;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public String getSalt() {return salt;}

    public void setSalt(String salt) {this.salt = salt;}

    public List<Profile> getProfiles() {return profiles;}

    public void setProfiles(List<Profile> profiles) {this.profiles = profiles;}

}
