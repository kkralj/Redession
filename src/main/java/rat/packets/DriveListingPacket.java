package rat.packets;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DriveListingPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 7443472778559993805L;

    private List<String> drives;

    @Override
    public void execute() {
        drives = new ArrayList<>();

        // FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] paths = File.listRoots();

        for (File path : paths) {
            System.out.println("Adding: " + path.getAbsolutePath().toString());
            drives.add(path.getAbsolutePath().toString());
            // System.out.println("Description: " + fsv.getSystemTypeDescription(path));
        }
    }

    public List<String> getDrives() {
        return drives;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DRIVE_LISTING;
    }
}
