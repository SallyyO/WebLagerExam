package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Box;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoxDAO {
    private ConnectionManager conMan = new ConnectionManager();

    public void addBox(Box box) {
        String sql = "INSERT INTO Boxes (profileId) VALUES ( ?)";
        try (Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, box.getProfileId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int saveBox(Box box) {
        String sql = "INSERT INTO Box (user_id, profile_id) VALUES (?, ?)";
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, box.getUserId());
            if (box.getProfileId() > 0) {
                stmt.setInt(2, box.getProfileId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                box.setId(keys.getInt(1));
                return box.getId();
            }
            throw new RuntimeException("No ID generated for Box");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Box> getBoxesByProfileId(int profileId) {
        List<Box> boxes = new ArrayList<>();
        String sql = "SELECT * FROM Boxes WHERE profileId = ?";
        try (Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, profileId);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                boxes.add(new Box(resultSet.getInt("id"), resultSet.getInt("profileId")));

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return boxes;
    }

    public void deleteBox(int boxId){
        String sql = "DELETE FROM Boxes WHERE id = ?";
        try(Connection con = conMan.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1,boxId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Box> getAllBoxes() {
        String sql = "SELECT id, profile_id FROM Box";
        List<Box> boxes = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                boxes.add(new Box(rs.getInt("id"), rs.getInt("profile_id")));
            }
            return boxes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Box> getBoxesByUser(int userId) {
        String sql = "SELECT id, user_id, profile_id FROM Box WHERE user_id = ?";
        List<Box> boxes = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                boxes.add(new Box(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("profile_id")
                ));
            }
            return boxes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void softDeleteBox(int boxId){
        String sql = "UPDATE  Boxes SET isDeleted = 1 WHERE id = ?";
        try(Connection con = conMan.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1,boxId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
