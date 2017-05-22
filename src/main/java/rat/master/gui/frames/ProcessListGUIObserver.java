package rat.master.gui.frames;

public interface ProcessListGUIObserver {

    void processRefreshRequested();

    void killProcess(int pid);
}
