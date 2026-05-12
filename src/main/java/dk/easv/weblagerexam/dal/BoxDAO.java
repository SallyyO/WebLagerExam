package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Box;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoxDAO {
    private ConnectionManager conMan = new ConnectionManager();

    public void addBox(Box box) {
        String sql = "INSERT INTO Box (box_id, meta_data,userId, profileId) VALUES (?,?,?,?)";
        try (Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, box.getBoxId());
            ps.setString(2,box.getMetaData());
            ps.setInt(3, box.getUserId());
            ps.setInt(4, box.getProfileId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    public int saveBox(Box box) {
        String sql = "INSERT INTO Box (box_id, userId, profileId) VALUES (?, ?, ?)";

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, box.getBoxId());
            stmt.setString(2, box.getMetaData());
            stmt.setInt(3, box.getUserId());
            stmt.setInt(4, box.getProfileId());

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
        String sql = "SELECT * FROM Box WHERE profileId = ? AND is_deleted=0";
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
        String sql = "DELETE FROM Box WHERE id = ?";
        try(Connection con = conMan.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1,boxId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Box> getAllBoxes() {
        String sql = "SELECT id, profileId FROM Box";
        List<Box> boxes = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                boxes.add(new Box(rs.getInt("id"), rs.getInt("profileId")));
            }
            return boxes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Box> getBoxesByUser(int userId) {
        String sql = "SELECT id, userId, profileId FROM Box WHERE userId = ?";
        List<Box> boxes = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                boxes.add(new Box(
                        rs.getInt("id"),
                        rs.getInt("userId"),
                        rs.getInt("profileId")
                ));
            }
            return boxes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void softDeleteBox(int boxId){
        String sql = "UPDATE  Box SET isDeleted = 1 WHERE id = ?";
        try(Connection con = conMan.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1,boxId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
