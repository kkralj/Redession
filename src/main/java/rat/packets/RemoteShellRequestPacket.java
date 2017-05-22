package rat.packets;

import java.io.Serializable;

public class RemoteShellRequestPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 1182064760729574373L;

    private PacketType packetType;

    private String command;

    public RemoteShellRequestPacket(String command) {
        this.command = command;
        packetType = PacketType.REMOTE_SHELL_DATA;
    }

    public RemoteShellRequestPacket() {
    }

    public String getCommand() {
        return command;
    }

    @Override
    public void execute() {
    }

    @Override
    public PacketType getPacketType() {
        return packetType;
    }

    public void startRemoteShell() {
        packetType = PacketType.START_REMOTE_SHELL;
    }

    public void stopRemoteShell() {
        packetType = PacketType.STOP_REMOTE_SHELL;
    }
}
