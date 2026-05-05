package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Box;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public List<Box> getBoxesByProfileId(int profileId) {
        List<Box> boxes = new ArrayList<>();
        String sql = "SELECT * FROM Boxes WHERE profileId = ? AND isDeleted=0";
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
