package rat.master.gui.models;

import rat.master.ClientController;

public interface IClientObserver {

    void clientConnected(ClientController clientController);

    void clientDataChanged(ClientController clientController);

    void clientDisconnected(ClientController clientController);
}
