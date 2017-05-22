package rat.master.gui.models;

import rat.master.WebcamInfo;

import javax.swing.table.AbstractTableModel;
import java.awt.*;

public class WebcamDevicesTableModel extends AbstractTableModel {

    private static final String[] columnNames = new String[]{"Source", "Resolution"};

    private WebcamInfo[] webcamInformations = new WebcamInfo[0];

    @Override
    public int getRowCount() {
        return webcamInformations.length;
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
        if (i1 == 0) {
            return webcamInformations[i].getName();
        } else if (i1 == 1) {
            Dimension resolution = webcamInformations[i].getResolution();
            return ((int)resolution.getWidth())+"x"+((int)resolution.getHeight());
        }
        return "";
    }

    @Override
    public Class getColumnClass(int column) {
        return String.class; // default
    }

    public void updateWebcamList(WebcamInfo[] webcamInformations) {
        this.webcamInformations = webcamInformations;
        fireTableDataChanged();
    }

    public String getDeviceName(int realTableIndex) {
        return webcamInformations[realTableIndex].getName();
    }

    public void clear() {
        webcamInformations = new WebcamInfo[0];
        fireTableDataChanged();
    }
}
