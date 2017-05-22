package rat.packets;

import rat.master.WebcamInfo;

import java.io.Serializable;

public class WebcamInfoPacket implements IPacket, Serializable {

    private static final long serialVersionUID = -7239893793672326203L;

    private WebcamInfo[] webcamInformations;

    private String deviceName;

    private int webcamJobId, webcamGUIId;

    private PacketType packetType = PacketType.WEBCAM_LIST;

    public WebcamInfoPacket() {
    }

    public WebcamInfoPacket(int webcamGUIId, WebcamInfo[] webcamInformations) {
        this.webcamGUIId = webcamGUIId;
        this.webcamInformations = webcamInformations;
    }

    public WebcamInfoPacket(int webcamGUIId) {
        this.webcamGUIId = webcamGUIId;
    }

    @Override
    public void execute() {
    }

    public int getWebcamGUIId() {
        return webcamGUIId;
    }

    public void setWebcamGUIId(int webcamGUIId) {
        this.webcamGUIId = webcamGUIId;
    }

    public int getWebcamJobId() {
        return webcamJobId;
    }

    public void setWebcamJobId(int webcamJobId) {
        this.webcamJobId = webcamJobId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public WebcamInfo[] getWebcamInformations() {
        return webcamInformations;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }

    @Override
    public PacketType getPacketType() {
        return packetType;
    }
}
