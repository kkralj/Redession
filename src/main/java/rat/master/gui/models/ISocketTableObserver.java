package rat.master.gui.models;

public interface ISocketTableObserver {
    void portAdded(int port);

    void portRemoved(int port);
}
