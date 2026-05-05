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
            throw new RuntimeException(e);
        }
        ;
    }


    public List<Log> getAllLogs() {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT * FROM Log ORDER BY createdAt DESC";
        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                logs.add(new Log(
                        rs.getLong("id"),
                        rs.getInt("userId"),
                        rs.getString("action"),
                        rs.getString("description"),
                        rs.getTimestamp("createdAt").toInstant()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return logs;

        }
}
