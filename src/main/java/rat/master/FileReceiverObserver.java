package rat.master;

import rat.packets.FileDataPacket;

public interface FileReceiverObserver {
    void updateProgress(FileDataPacket dataPacket, long count, double downloadSpeed);
}
