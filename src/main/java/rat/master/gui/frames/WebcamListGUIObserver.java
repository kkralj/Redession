package rat.master.gui.frames;

public interface WebcamListGUIObserver {

    void webcamSelected(String deviceName);

    void webcamListRefreshRequest();
}
