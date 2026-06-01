package dk.easv.weblagerexam.be;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    private int id;
    private String name;
    private ProfileSettings settings;
    private Double settingsValue;
    private int clientId;

    public Profile(int id, String name, ProfileSettings settings, Double settingsValue) {
        this.id = id;
        this.name = name;
        this.settings = settings;
        this.settingsValue = settingsValue;
    }

    public ProfileSettings getSettings() {return settings;}
    public void setSettings(ProfileSettings settings) {this.settings = settings;}

    public Double getSettingsValue() {return settingsValue;}
    public void setSettingsValue(Double settingsValue) {this.settingsValue = settingsValue;}

    private List<Box> boxes;
    private Client client;

    public Profile(int id, String name) {
        this.id = id;
        this.name = name;
        this.boxes = new ArrayList<>();
    }

    public Profile(int id, String name, int clientId, ProfileSettings settings, Double settingsValue) {
        this.id = id;
        this.name = name;
        this.clientId = clientId;
        this.settings = settings;
        this.settingsValue = settingsValue;
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

    public int getClientId() {return clientId;}
    public void setClientId(int clientId) {this.clientId = clientId;}


    @Override
    public String toString() {return name;}

    public Client getClient() {return client;}
    public void setClient(Client client) {this.client = client;}


}
