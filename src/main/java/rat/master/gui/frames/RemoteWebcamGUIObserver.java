package rat.master.gui.frames;

public interface RemoteWebcamGUIObserver {

    void webcamSettingsUpdated(int FPS, int frameWidth, int frameHeight);

    void startWebcamRecord(int frameId, String deviceName);

    void stopWebcamRecord(int frameId);
}
