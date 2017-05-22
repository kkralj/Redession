package rat.packets;

import java.io.Serializable;

public class WebcamDataPacket implements IPacket, Serializable {

    private static final long serialVersionUID = -6115765261982047657L;

    private int frameId;

    private byte[] imageData;

    public WebcamDataPacket(int frameId, byte[] imageData) {
        this.frameId = frameId;
        this.imageData = imageData;
    }

    public int getFrameId() {
        return frameId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    @Override
    public void execute() {
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.WEBCAM_DATA;
    }
}
