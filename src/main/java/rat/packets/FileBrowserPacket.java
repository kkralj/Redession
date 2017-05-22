package rat.packets;

import java.io.Serializable;

public class FileBrowserPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 6079296454184750040L;

    private String operation;

    private String path;

    private String folderPath;

    private int index;

    public FileBrowserPacket(String operation, String path) {
        this.operation = operation;
        this.path = path;
    }

    public FileBrowserPacket(String operation, String path, String folderPath) {
        this(operation, path);
        this.folderPath = folderPath;
    }

    public FileBrowserPacket(String operation, int index, String path, String folderPath) {
        this(operation, path);
        this.index = index;
        this.folderPath = folderPath;
    }

    @Override
    public void execute() {
    }

    public int getIndex() {
        return index;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getOperation() {
        return operation;
    }

    public String getPath() {
        return path;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.FILE_BROWSER;
    }
}
