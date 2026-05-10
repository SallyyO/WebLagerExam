package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.ProfileSettings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO {

    ConnectionManager conMan = new ConnectionManager();

    public void addProfile(Profile profile) {
        String sql = "INSERT INTO Profiles (name, settings, settingsvalue) VALUES (?, ?, ?)";
        try (Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, profile.getName());

            // enum → String for DB
            ps.setString(2, profile.getSettings().name());

            // nullable Double handling
            if (profile.getSettingsValue() != null) {
                ps.setDouble(3, profile.getSettingsValue());
            } else {
                ps.setNull(3, java.sql.Types.DOUBLE);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Get all profiles
    public List<Profile> getAllProfiles() {

        String sql = """
                SELECT id,
                       name,
                       settingstype,
                       settingsvalue
                FROM Profiles
                """;

        return fetchProfiles(sql, null);
    }

    // Get profiles that are assigned to a user
    public List<Profile> getProfilesForUser(int userId) {

        String sql = """
                SELECT p.id,
                       p.name,
                       p.settingstype,
                       p.settingsvalue
                FROM Profiles p
                INNER JOIN UserProfile up
                    ON p.id = up.profile_id
                WHERE up.user_id = ?
                """;

        return fetchProfiles(sql, userId);
    }

    //Assign profile to a user
    public void assignProfileToUser(int userId, int profileId) {

        String sql = """
                INSERT INTO UsersProfile (user_id, profile_id)
                VALUES (?, ?)
                """;

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setInt(1, userId);
            stmt.setInt(2, profileId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Remove profile from a user
    public void removeProfileFromUser(int userId, int profileId) {

        String sql = """
                DELETE FROM UsersProfile
                WHERE user_id = ?
                  AND profile_id = ?
                """;

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setInt(1, userId);
            stmt.setInt(2, profileId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // shared helper
    private List<Profile> fetchProfiles(String sql, Integer userId) {

        List<Profile> profiles = new ArrayList<>();

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt = con.prepareStatement(sql);

            if (userId != null) {
                stmt.setInt(1, userId);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                // Read enum/string from DB
                String typeStr = rs.getString("settingstype");

                ProfileSettings type =
                        typeStr != null
                                ? ProfileSettings.valueOf(typeStr)
                                : null;

                // Read nullable double
                Double value =
                        rs.getObject("settingsvalue") != null
                                ? rs.getDouble("settingsvalue")
                                : null;

                profiles.add(
                        new Profile(
                                rs.getInt("id"),
                                rs.getString("name"),
                                type,
                                value
                        )
                );
            }

            return profiles;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveProfile(Profile profile) {
        String sql = """
                INSERT INTO Profiles (name, settingstype, settingsvalue)
                VALUES (?, ?, ?)
                """;
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getSettings() != null
                    ? profile.getSettings().name()
                    : null);
            if (profile.getSettingsValue() != null) {
                stmt.setDouble(3, profile.getSettingsValue());
            } else {
                stmt.setNull(3, java.sql.Types.FLOAT);
            }
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                profile.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void softDeleteProfile(Profile profile) {
        String sql = "UPDATE Profiles SET isDeleted=1 WHERE name=?";
        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, profile.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
