package dk.easv.weblagerexam.bll;

import dk.easv.weblagerexam.be.Client;
import dk.easv.weblagerexam.dal.ClientDAO;
import dk.easv.weblagerexam.dal.DAOManager;

import java.util.List;

public class ClientManager {

    private final DAOManager daoManager = new DAOManager();

    public List<Client> getAllClients() {
        return daoManager.getClientDAO().getAllClients();
    }

    public Client getClientById(int id) {
        return daoManager.getClientDAO().getClientById(id);
    }

    public void createClient(Client client) {
        daoManager.getClientDAO().createClient(client);
    }

    public void updateClient(Client client) {
        daoManager.getClientDAO().updateClient(client);
    }

    public void deleteClient(int id) {
        daoManager.getClientDAO().deleteClient(id);
    }
}
