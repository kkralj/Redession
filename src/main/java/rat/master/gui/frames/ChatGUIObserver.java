package rat.master.gui.frames;

public interface ChatGUIObserver {
    void sendChatMessage(String message);

    void sendNudge();
}
