package rat.packets;

public interface IPacket {

    void execute();

    PacketType getPacketType();
}
