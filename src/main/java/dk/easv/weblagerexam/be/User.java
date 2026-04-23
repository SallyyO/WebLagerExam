package dk.easv.weblagerexam.be;

public class User {
    private int id;
    private String name;
    private String role;
    private String email;
    private String password;
    private String salt;


    public User(int id, String name, String role, String email, String password, String salt)
    {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
        this.salt = salt;
    }

    public User(String name, String role, String email, String password, String salt)
    {
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
        this.salt = salt;
    }

    public User(String name, String password){
        this.name = name;
        this.password = password;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getRole() {return role;}

    public void setRole(String role) {this.role = role;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public String getSalt() {return salt;}

    public void setSalt(String salt) {this.salt = salt;}
}
