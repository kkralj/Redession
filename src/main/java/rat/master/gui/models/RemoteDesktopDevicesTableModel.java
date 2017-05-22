package rat.master.gui.models;

import rat.master.DesktopScreenInfo;

import javax.swing.table.AbstractTableModel;

public class RemoteDesktopDevicesTableModel extends AbstractTableModel {

    private static final String[] columnNames = new String[]{"Source", "Resolution"};

    private DesktopScreenInfo[] desktopInformations = new DesktopScreenInfo[0];

    @Override
    public int getRowCount() {
        return desktopInformations.length;
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
    public Class getColumnClass(int column) {
        return String.class; // default
    }

    @Override
    public Object getValueAt(int i, int i1) {
        if (i1 == 0) {
            return "Screen " + desktopInformations[i].getId();
        } else if (i1 == 1) {
            int width = desktopInformations[i].getWidth();
            int height = desktopInformations[i].getHeight();
            return width + "x" + height;
        }
        return "";
    }

    public void updateRemoteDesktopList(DesktopScreenInfo[] desktopInformations) {
        this.desktopInformations = desktopInformations;
        fireTableDataChanged();
    }

    public void clear() {
        desktopInformations = new DesktopScreenInfo[0];
        fireTableDataChanged();
    }

    public String getDeviceName(int realTableIndex) {
        return desktopInformations[realTableIndex].getId();
    }
}
