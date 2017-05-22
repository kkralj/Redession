package rat.packets;

import java.io.Serializable;

public class DesktopDataPacket implements IPacket, Serializable {

    private static final long serialVersionUID = -3371702232658185649L;

    private int imageWidth, imageHeight;
    private byte[] imageData;
    private int frameId;

    public DesktopDataPacket(int imageWidth, int imageHeight, byte[] imageData, int frameId) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageData = imageData;
        this.frameId = frameId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public int getFrameId() {
        return frameId;
    }

    @Override
    public void execute() {

    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DESKTOP_DATA;
    }
}
