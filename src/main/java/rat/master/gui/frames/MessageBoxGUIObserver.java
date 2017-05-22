package rat.master.gui.frames;

public interface MessageBoxGUIObserver {
    public void sendMessage(String title, String message, int messageBoxType);
}
