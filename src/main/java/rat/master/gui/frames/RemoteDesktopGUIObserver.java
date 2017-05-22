package rat.master.gui.frames;

public interface RemoteDesktopGUIObserver {

    void remoteDesktopSettingsUpdated(int desktopJobId, int frameWidth, int frameHeight);

    void startRemoteDesktopRecord(int frameId, String deviceName, int frameWidth, int frameHeight);

    void stopRemoteDesktopRecord(int frameId);

}
