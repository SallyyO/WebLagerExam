package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    ConnectionManager conMan = new ConnectionManager();

    // Get all clients
    public List<Client> getAllClients() {

        String sql = """
                SELECT id,
                       name
                FROM Clients
                ORDER BY name
                """;

        List<Client> clients = new ArrayList<>();

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                clients.add(
                        new Client(
                                rs.getInt("id"),
                                rs.getString("name")
                        )
                );
            }

            return clients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Get client by id
    public Client getClientById(int clientId) {

        String sql = """
                SELECT id,
                       name
                FROM Clients
                WHERE id = ?
                """;

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clientId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                return new Client(
                        rs.getInt("id"),
                        rs.getString("name")
                );
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Create client
    public void saveClient(Client client) {

        String sql = """
                INSERT INTO Clients (name)
                VALUES (?)
                """;

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, client.getName());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                client.setId(keys.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Update client
    public void updateClient(Client client) {

        String sql = """
                UPDATE Clients
                SET name = ?
                WHERE id = ?
                """;

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, client.getName());
            stmt.setInt(2, client.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Delete client
    public void deleteClient(int clientId) {

        String sql = """
                DELETE FROM Clients
                WHERE id = ?
                """;

        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clientId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createClient(Client client) {
        String sql = """
                INSERT INTO Clients (name)
                VALUES (?)
                """;
        try (Connection con = conMan.getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            stmt.setString(1, client.getName());
            stmt.executeUpdate();

            ResultSet keys =
                    stmt.getGeneratedKeys();
            if (keys.next()) {
                client.setId(
                        keys.getInt(1)
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
