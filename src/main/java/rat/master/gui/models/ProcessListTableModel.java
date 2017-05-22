package rat.master.gui.models;

import rat.packets.ProcessInfo;
import rat.packets.ProcessListPacket;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ProcessListTableModel extends AbstractTableModel implements IProcessListObserver {

    private List<ProcessInfo> processList = new ArrayList<>();

    private static final String[] columnNames = new String[]{"PID", "Process name", "Memory (KiB)", "Location"};

    @Override
    public int getRowCount() {
        return processList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int i) {
        return columnNames[i];
    }

    @Override
    public Object getValueAt(int i, int i1) {
        ProcessInfo process = processList.get(i);

        switch (i1) {
            case 0:
                return process.getPID();
            case 1:
                return process.getName();
            case 2:
                return process.getMemory();
            case 3:
                return process.getLocation();
            default:
                return "";
        }
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2:
                return Long.class;
            case 3:
                return String.class;
            default:
                return String.class;
        }
    }

    @Override
    public void processListChanged(ProcessListPacket packet) {
        Objects.requireNonNull(packet);
        this.processList = Objects.requireNonNull(packet.getProcessList());

        Collections.sort(processList);

        SwingUtilities.invokeLater(() -> {
            fireTableDataChanged();
        });
    }

    public int getPID(int selectedIndex) {
        return (int) getValueAt(selectedIndex, 0);
    }
}
