package dk.easv.weblagerexam.be;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    private int id;
    private String name;
    private List<Box> boxes; // one profile has many boxes

    public Profile(int id, String name) {
        this.id = id;
        this.name = name;
        this.boxes = new ArrayList<>();
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
    public List<Box> getBoxes() {
        return boxes;
    }
    public void setBoxes(List<Box> boxes) {
        this.boxes = boxes;
    }

    @Override
    public String toString() {
        return name;
    }


}
