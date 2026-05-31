package dk.easv.weblagerexam.be;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String role;
    private String initials;
    private String password;
    private String salt;
    private boolean isDeleted;
    private boolean active;

    private List<Profile> profiles;

    public User(int id, String username, String role, String initials, String password, String salt)
    {
        this.id = id;
        this.username = username;
        this.role = role;
        this.initials = initials;
        this.password = password;
        this.salt = salt;
        this.profiles = new ArrayList<>();
    }

    public User(int id, String username, String role, String initials, String password, String salt, boolean active)
    {
        this.id = id;
        this.username = username;
        this.role = role;
        this.initials = initials;
        this.password = password;
        this.salt = salt;
        this.active = active;
        this.profiles = new ArrayList<>();
    }

    public User(String username, String role, String initials, String password, String salt)
    {
        this.username = username;
        this.role = role;
        this.initials = initials;
        this.password = password;
        this.salt = salt;
    }

    public User(String initials, String password){
        this.initials = initials;
        this.password = password;
    }

    public User(int id, String username, String role, boolean active){
        this.id = id;
        this.username = username;
        this.role = role;
        this.active = active;
    }

    public User(){}

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public String getInitials() {return initials;}
    public void setInitials(String initials) {this.initials = initials;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getSalt() {return salt;}
    public void setSalt(String salt) {this.salt = salt;}

    public List<Profile> getProfiles() {return profiles;}
    public void setProfiles(List<Profile> profiles) {this.profiles = profiles;}

    public boolean isDeleted() {return isDeleted;}
    public void setDeleted(boolean deleted) {this.isDeleted = deleted;}

    public boolean isActive() {return active;}
    public void setActive(boolean active) {this.active = active;}
}
