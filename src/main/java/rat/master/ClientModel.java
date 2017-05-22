package rat.master;

import rat.master.gui.models.IClientObserver;
import rat.packets.BasicInformationPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientModel {

    private static int clientCount = 0;
    private int clientID = ++clientCount;

    private ClientController clientController;

    private List<IClientObserver> observers = new ArrayList<>();

    private BasicInformationPacket informationPacket = new BasicInformationPacket();

    public BasicInformationPacket getInformationPacket() {
        return informationPacket;
    }

    public void setInformationPacket(BasicInformationPacket informationPacket) {
        this.informationPacket = Objects.requireNonNull(informationPacket);
        notifyObservers();
    }

    public void setController(ClientController controller) {
        this.clientController = Objects.requireNonNull(controller);
    }

    private void notifyObservers() {
        List<IClientObserver> tmp = new ArrayList<>(observers);
        for (IClientObserver observer : tmp) {
            observer.clientDataChanged(clientController);
        }
    }

    public void register(IClientObserver observer) {
        if (!observers.contains(Objects.requireNonNull(observer))) {
            observers.add(observer);
        }
    }

    public void unregister(IClientObserver observer) {
        observers.remove(observer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientModel that = (ClientModel) o;

        return clientID == that.clientID;
    }

    @Override
    public int hashCode() {
        return clientID;
    }
}
