package rat.packets;

import java.io.Serializable;

public class CloseChatPacket implements IPacket, Serializable {
    private static final long serialVersionUID = -1985201961143805255L;

    @Override
    public void execute() {
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.CLOSE_CHAT;
    }
}
