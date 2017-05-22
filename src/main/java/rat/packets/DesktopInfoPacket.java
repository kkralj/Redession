package rat.packets;

import rat.master.DesktopScreenInfo;

import java.io.Serializable;

public class DesktopInfoPacket implements IPacket, Serializable {

    private static final long serialVersionUID = -4991577136657511053L;

    private DesktopScreenInfo[] desktopScreenInformations;

    private String desktopDeviceName;

    private int desktopJobId, desktopGUIId;

    private int frameWidth, frameHeight;

    private PacketType packetType = PacketType.DESKTOP_LIST;

    public DesktopInfoPacket() {
    }

    public DesktopInfoPacket(int desktopGUIId, DesktopScreenInfo[] desktopScreenInformations) {
        this.desktopGUIId = desktopGUIId;
        this.desktopScreenInformations = desktopScreenInformations;
    }

    public DesktopInfoPacket(int desktopGUIId, int frameWidth, int frameHeight) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.desktopGUIId = desktopGUIId;
    }

    @Override
    public void execute() {
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public int getDesktopGUIId() {
        return desktopGUIId;
    }

    public void setDesktopGUIId(int desktopGUIId) {
        this.desktopGUIId = desktopGUIId;
    }

    public int getDesktopJobId() {
        return desktopJobId;
    }

    public void setDesktopJobId(int desktopJobId) {
        this.desktopJobId = desktopJobId;
    }

    public void setDesktopDeviceName(String desktopDeviceName) {
        this.desktopDeviceName = desktopDeviceName;
    }

    public DesktopScreenInfo[] getDesktopScreenInformations() {
        return desktopScreenInformations;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }

    public String getDesktopDeviceName() {
        return desktopDeviceName;
    }

    @Override
    public PacketType getPacketType() {
        return packetType;
    }
}
