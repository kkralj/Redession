package rat.master.gui.models;

import rat.packets.DriveListingPacket;

public interface IDrivesObserver {

    void drivesChanged(DriveListingPacket packet);
}
