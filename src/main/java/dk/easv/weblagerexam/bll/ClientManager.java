package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Client;
import dk.easv.weblagerexam.be.User;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class ClientManager {

    private final DAOManager dao = new DAOManager();
    private final LogManager logManager = new LogManager();

    public List<Client> getAllClients() {
        return dao.getClientDAO().getAllClients();
    }

    public Client getClientById(int id) {
        return dao.getClientDAO().getClientById(id);
    }

    public void createClient(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Client name cannot be empty"
            );
        }

        Client client = new Client(name);

        dao.getClientDAO().createClient(client);
        logManager.logClientCreated(client.getName());
    }

    public void updateClient(Client client) {
        Client oldClientName = dao.getClientDAO().getClientById(client.getId());

        dao.getClientDAO().updateClient(client);

        if (!oldClientName.getName().equals(client.getName())) {

            logManager.logClientUpdated(
                    oldClientName.getName(),
                    client.getName()
            );
        }
    }

    public void setClientActive(int clientId, boolean active) {

        Client targetClient = dao.getClientDAO().getClientById(clientId);

        // Don't log if nothing changed
        if (targetClient.isActive() == active) {
            return;
        }

        dao.getClientDAO().setClientActive(
                clientId,
                active
        );

        User admin = SessionManager.getCurrentUser();

        if (active) {
            logManager.logClientActivated(
                    admin.getId(),
                    admin.getUsername(),
                    targetClient.getName()
            );

        } else {
            logManager.logClientDeactivated(
                    admin.getId(),
                    admin.getUsername(),
                    targetClient.getName()
            );
        }
    }
}
