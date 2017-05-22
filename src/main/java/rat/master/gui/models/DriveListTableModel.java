package rat.master.gui.models;

import rat.packets.DriveListingPacket;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class DriveListTableModel extends AbstractTableModel implements IDrivesObserver {

    private List<String> drives = new ArrayList<>();

    @Override
    public String getColumnName(int i) {
        return "Drives";
    }

    @Override
    public int getRowCount() {
        return drives.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int i, int i1) {
        return drives.get(i);
    }

    @Override
    public void drivesChanged(DriveListingPacket packet) {
        this.drives = packet.getDrives();

        SwingUtilities.invokeLater(() -> {
            fireTableDataChanged();
        });
    }
}
