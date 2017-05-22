package rat.master.gui.models;

import rat.packets.DirectoryListingPacket;

public interface IFileDirectoryObserver {

    void directoryChanged(DirectoryListingPacket packet);
}
