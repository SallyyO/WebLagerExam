package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Profile;
import dk.easv.weblagerexam.be.User;

import javax.print.attribute.standard.Finishings;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    ConnectionManager conMan = new ConnectionManager();

    public User getUser(String initials) {
        try (Connection con = conMan.getConnection()) {
            String sql = "SELECT * FROM Users WHERE initials = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, initials);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String salt = rs.getString("salt");

                return new User(id, username, role, initials, password, salt);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUserById(int id) {
        try (Connection con = conMan.getConnection()) {
            String sql = "SELECT * FROM Users WHERE id = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String salt = rs.getString("salt");
                String initials = rs.getString("initials");

                return new User(id, username, role, initials, password, salt);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addUser(User user) {
        try (Connection con = conMan.getConnection()) {
            String sql = "INSERT INTO Users (role, username, initials, password, salt) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, user.getRole());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getInitials());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getSalt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void editUser(User user) {
        try (Connection con = conMan.getConnection()) {
            String sql;
            if (user.getPassword() != null) {
                 sql = "UPDATE Users SET username = ?, role = ?, initials = ?, password = ? WHERE id = ?";
            } else {
                 sql = "UPDATE Users SET username = ?, role = ?, initials = ? WHERE id = ?";
            }
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getRole());
            stmt.setString(3, user.getInitials());
            if (user.getPassword() != null) {
                stmt.setString(4, user.getPassword());
                stmt.setInt(5, user.getId());
            } else {
                stmt.setInt(4, user.getId());
            }
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

        public void softDeleteUser(int userID) {
        String sql = "UPDATE  Users SET isDeleted = 1 WHERE id = ?";
        try(Connection con = conMan.getConnection();
            PreparedStatement ps =  con.prepareStatement(sql)){
            ps.setInt(1, userID);
            ps.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try (Connection con = conMan.getConnection()) {
            String sql = "SELECT * FROM Users";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                String username = rs.getString("username");
                String initials = rs.getString("initials");
                String password = rs.getString("password");
                String salt = rs.getString("salt");

                users.add(new User(id, username, role, initials, password, salt));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    // Assign a profile to a user
    public void addProfileToUser(int userId, int profileId) {
        String sql = "INSERT INTO UsersProfiles (userId, profileId) VALUES (?, ?) ";
        try ( Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, profileId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Remove a profile from a user
    public void deleteProfileFromUser(int userId, int profileId) {
        String sql = "DELETE FROM UsersProfiles WHERE userId = ? AND profileId = ?";
        try( Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, profileId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // get all profiles assigned to a user
    public List<Profile> getProfilesForUser(int userId) {
        List<Profile> profiles = new ArrayList<>();
        String sql = "SELECT p.id, p.name FROM Profiles p " + "JOIN UsersProfiles up ON p.id = up.profileId " + "WHERE up.userId = ?";
        try( Connection con = conMan.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                profiles.add(new Profile(rs.getInt("id"), rs.getString("name")));

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return profiles;
    }

    public List<User> searchUsers(String searchText) {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM Users WHERE username LIKE ? AND (isDeleted = 0 OR isDeleted IS NULL)";

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                String username = rs.getString("username");
                String initials = rs.getString("initials");
                String password = rs.getString("password");
                String salt = rs.getString("salt");

                users.add(new User(id, username, role, initials, password, salt));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }
}
