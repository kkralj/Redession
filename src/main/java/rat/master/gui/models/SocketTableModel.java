package rat.master.gui.models;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SocketTableModel extends AbstractTableModel {

    private List<SocketTableEntry> portsList = new ArrayList<>();

    private static final String[] columnNames = new String[]{
            "Port", "Status"
    };

    @Override
    public int getRowCount() {
        return portsList.size();
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
        SocketTableEntry entry = portsList.get(i);

        switch (i1) {
            case 0:
                return entry.getPort();
            case 1:
                return entry.getPortStatus();
            default:
                return "-";
        }
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    public void removePortAtRow(int row) {
        portsList.remove(row);
        fireTableDataChanged();
    }

    public void addPort(int port) {
        portsList.add(new SocketTableEntry(port, "Listening"));
        fireTableDataChanged();
    }

    public int getPortAtRow(int row) {
        return portsList.get(row).getPort();
    }

    private static class SocketTableEntry {

        private String portStatus;
        private int port;
        // icon

        public SocketTableEntry(int port, String portStatus) {
            this.portStatus = portStatus;
            this.port = port;
        }

        public String getPortStatus() {
            return portStatus;
        }

        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SocketTableEntry that = (SocketTableEntry) o;

            return port == that.port;
        }

        @Override
        public int hashCode() {
            return port;
        }
    }
}
