package rat.master.gui.frames;

import rat.master.ClientController;

public interface MainWindowObserver {

    void mainWindowRequest(ClientController client, String operation);

    void bindPortRequest(int connectionPort, int transferPort) throws Exception; // TODO: new class, portException

    void unbindPortRequest(int connectionPort, int transferPort);
}
