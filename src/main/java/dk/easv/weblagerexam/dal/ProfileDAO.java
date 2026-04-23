package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO {
    private ConnectionManager conMan = new ConnectionManager();
    BoxDAO boxDAO = new BoxDAO();

    public void addProfile(Profile profile) {
        String sql = "INSERT INTO Profiles (name) VALUES (?)";
        try(Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);{
                ps.setString(1, profile.getName());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Profile> getAllProfiles() {
        List <Profile> profiles = new ArrayList<>();
        String sql = "SELECT * FROM Profiles";
        try(Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Profile profile = new Profile(rs.getInt("id"), rs.getString("name"));
                // Load boxes for this profile
                List<Box> boxes = boxDAO.getBoxesByProfileId(profile.getId());
                profile.setBoxes(boxes);
                profiles.add(profile);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return profiles;
    }
}
