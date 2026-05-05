package dk.easv.weblagerexam.be;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    private int id;
    private String name;
    private int customerId;
    private boolean isDeleted;

    public Profile(int id, String name,int customerId) {
        this.id = id;
        this.name = name;
    }
    public Profile(String name){
        this.name = name;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }


    @Override
    public String toString() {
        return name;
    }


}
