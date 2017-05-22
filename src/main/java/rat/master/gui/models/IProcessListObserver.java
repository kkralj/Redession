package rat.master.gui.models;

import rat.packets.ProcessListPacket;

public interface IProcessListObserver {

    void processListChanged(ProcessListPacket packet);
}
