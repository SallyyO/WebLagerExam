package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Client;
import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.ProfileSettings;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileDAO {

    ConnectionManager conMan = new ConnectionManager();
    private final Map<Integer, Profile> profileCache = new HashMap<>();

    // Get all profiles
    public List<Profile> getAllProfiles() {

        String sql = """
                SELECT id,
                       name,
                       clientId,
                       settingstype,
                       settingsvalue
                FROM Profiles
                """;

        return fetchProfiles(sql, null);
    }

    public void saveProfile(Profile profile) {
        String sql = """
                INSERT INTO Profiles (name, clientId, settingstype, settingsvalue)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, profile.getName());
            if (profile.getClientId() > 0) {
                stmt.setInt(2, profile.getClientId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, profile.getSettings() != null
                    ? profile.getSettings().name()
                    : null);
            if (profile.getSettingsValue() != null) {
                stmt.setDouble(4, profile.getSettingsValue());
            } else {
                stmt.setNull(4, java.sql.Types.DOUBLE);
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

    // Get profiles that are assigned to a user
    public List<Profile> getProfilesForUser(int userId) {
        String sql = """
                SELECT p.id,
                       p.name,
                       p.clientId,
                       p.settingstype,
                       p.settingsvalue
                FROM Profiles p
                INNER JOIN UsersProfiles up
                    ON p.id = up.profileId
                WHERE up.userId = ?
                """;

        List<Profile> result = fetchProfiles(sql, userId);

        return result;
    }

    //Assign profile to a user
    public void assignProfileToUser(int userId, int profileId) {

        String sql = """
                INSERT INTO UsersProfiles (userId, profileId)
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
                DELETE FROM UsersProfiles
                WHERE userId = ?
                  AND profileId = ?
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

    public void assignProfileToClient(int profileId, int clientId)
    {
        String sql = """
        UPDATE Profiles
        SET clientId = ?
        WHERE id = ?
        """;

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            stmt.setInt(2, profileId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeProfileFromClient(int profileId)
    {
        String sql = """
        UPDATE Profiles
        SET clientId = NULL
        WHERE id = ?
        """;

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt =
                     con.prepareStatement(sql)) {

            stmt.setInt(1, profileId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Profile> fetchProfiles(String sql, Integer parameter) {

        List<Profile> profiles = new ArrayList<>();

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt = con.prepareStatement(sql);

            if (parameter != null) {
                stmt.setInt(1, parameter);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                // Read enum/string from DB
                String typeStr = rs.getString("settingstype");

                ProfileSettings type = typeStr != null
                        ? ProfileSettings.valueOf(typeStr)
                        : null;

                // Read nullable double
                Double value = rs.getObject("settingsvalue") != null
                        ? rs.getDouble("settingsvalue")
                        : null;

                Profile profile = new Profile(
                        rs.getInt("id"),
                        rs.getString("name"),
                        type,
                        value
                );

                profile.setClientId(
                        rs.getInt("clientId")
                );

                profiles.add(profile);
            }

            return profiles;

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

    public Profile getProfileById(int profileId) {

        if (profileCache.containsKey(profileId)) {
            return profileCache.get(profileId);
        }

        String sql = """
                SELECT id,
                       name,
                       clientId,
                       settingstype,
                       settingsvalue
                FROM Profiles
                WHERE id = ?
                """;

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, profileId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String typeStr = rs.getString("settingstype");

                ProfileSettings type = typeStr != null
                        ? ProfileSettings.valueOf(typeStr)
                        : null;

                Double value = rs.getObject("settingsvalue") != null
                        ? rs.getDouble("settingsvalue")
                        : null;

                Profile profile = new Profile(
                        rs.getInt("id"),
                        rs.getString("name"),
                        type,
                        value
                );

                profileCache.put(profileId, profile);
                return profile;
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Profile> getProfilesForClient(int clientId) {

        String sql = """
                SELECT id,
                       name,
                       clientId,
                       settingstype,
                       settingsvalue
                FROM Profiles
                WHERE clientId = ?
                """;

        return fetchProfiles(sql, clientId);
    }

    public List<Profile> getAllProfilesWithClients() {

        String sql = """
                SELECT p.id,
                       p.name,
                       p.settingstype,
                       p.settingsvalue,
                       p.clientId,
                       c.name AS clientName
                FROM Profiles p
                LEFT JOIN Clients c
                    ON p.clientId = c.id
                """;

        List<Profile> profiles = new ArrayList<>();

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                String typeStr = rs.getString("settingstype");

                ProfileSettings type = typeStr != null
                        ? ProfileSettings.valueOf(typeStr)
                        : null;

                Double value = rs.getObject("settingsvalue") != null
                        ? rs.getDouble("settingsvalue")
                        : null;

                Profile profile = new Profile(
                        rs.getInt("id"),
                        rs.getString("name"),
                        type,
                        value
                );

                int clientId = rs.getInt("clientId");

                if (!rs.wasNull()) {

                    Client client = new Client(
                            clientId,
                            rs.getString("clientName")
                    );

                    profile.setClient(client);
                }

                profiles.add(profile);
            }

            return profiles;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
