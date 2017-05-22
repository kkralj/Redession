package rat.packets;

import java.io.Serializable;

public class FileReceiverPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 8668490906919918927L;

    private int port;

    public FileReceiverPacket(int port) {
        this.port = port;
    }

    @Override
    public void execute() {
    }

    public int getPort() {
        return port;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.FILE_RECEIVER_PACKET;
    }
}
