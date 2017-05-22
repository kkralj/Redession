package rat.master.gui.frames;

public interface RemoteDesktopListGUIObserver {

    void remoteDesktopSelected(String deviceName);

    void remoteDesktopListRefreshRequest();
}
