package dk.easv.weblagerexam.be;

import java.time.Instant;
import java.time.LocalDateTime;

public class Log {
    private Long id;
    private int userId;
    private String username;
    private String action;
    private String description;
    private LocalDateTime timestamp;


    public Log(Long id, int userId, String action, String description, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;

    }

    public Log(int userId, String action, String description){
        this.userId = userId;
        this.action = action;
        this.description = description;
    }

    public Log(String username, String action, String description) {
        this.username = username;
        this.action = action;
        this.description = description;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getUsername() {return username;}
    public void setUsername(String username) { this.username = username; }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    @Override
    public String toString() {
        return "[" + timestamp + "]" + action + "-" + description;

    }

}
