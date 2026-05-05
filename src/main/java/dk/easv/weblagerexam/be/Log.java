package dk.easv.weblagerexam.be;

import java.time.Instant;
import java.time.LocalDateTime;

public class Log {
    private Long id;
    private int userId;
    private String action;
    private String description;
    private Instant createdAt;


    public Log(Long id, int userId, String action, String description, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.createdAt = createdAt;

    }

    public Log(int userId, String action, String description){
        this.userId = userId;
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
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant  createdAt) {
        this.createdAt = createdAt;
    }
    @Override
    public String toString() {
        return "[" + createdAt + "]" + action + "-" + description;

    }

}
