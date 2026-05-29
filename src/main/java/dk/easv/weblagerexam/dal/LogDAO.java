package dk.easv.weblagerexam.dal;


import dk.easv.weblagerexam.be.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {
    private ConnectionManager conMan = new ConnectionManager();

    public void addLog(Log log) {
        String sql = "INSERT INTO Log (userId,action,description) VALUES (?,?,?)";
        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to write log: " + e.getMessage());
            // ^even if the log fails, the program will keep running
        }
        ;
    }


    public List<Log> getAllLogs() {
        String sql = """
                SELECT l.id, l.userId, l.action, l.description, l.timestamp,
                       u.username
                FROM Log l
                INNER JOIN Users u ON l.userId = u.id
                ORDER BY l.timestamp DESC
                """;
        List<Log> logs = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Log log = new Log(
                        rs.getInt("userId"),
                        rs.getString("action"),
                        rs.getString("description")
                );
                log.setId(rs.getLong("id"));
                log.setUsername(rs.getString("username"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch logs: " + e.getMessage());
        }
        return logs;
    }

    public List<Log> getLogsForUser(int userId) {
        String sql = """
                SELECT id, userId, action, description, timestamp
                FROM Log
                WHERE userId = ?
                ORDER BY timestamp DESC
                """;
        List<Log> logs = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Log log = new Log(
                        rs.getInt("userId"),
                        rs.getString("action"),
                        rs.getString("description")
                );
                log.setId(rs.getLong("id"));
                log.setUsername(rs.getString("username"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch logs: " + e.getMessage());
        }
        return logs;
    }
}

