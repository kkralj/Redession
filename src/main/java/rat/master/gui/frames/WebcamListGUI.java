package rat.master.gui.frames;

import rat.master.MasterMain;
import rat.master.gui.models.WebcamDevicesTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WebcamListGUI extends JFrame {

    private List<WebcamListGUIObserver> observerList = new ArrayList<>();

    private JTable webcamDevicesTable;

    private WebcamDevicesTableModel tableModel = new WebcamDevicesTableModel();

    private JPopupMenu popupMenuRow, popupMenuEmptyRow;

    public WebcamListGUI() {
        setMinimumSize(new Dimension(400, 300));
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        JPanel centerMarginPanel = new JPanel(new BorderLayout());
        centerMarginPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(centerMarginPanel, BorderLayout.CENTER);

        webcamDevicesTable = new JTable(tableModel);
        webcamDevicesTable.setFillsViewportHeight(true);
        webcamDevicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        webcamDevicesTable.setDefaultRenderer(String.class, new JTableRenderer());
        centerMarginPanel.add(new JScrollPane(webcamDevicesTable), BorderLayout.CENTER);

        popupMenuRow = new JPopupMenu();

        JMenuItem openWindowAction = new JMenuItem("Open");
        openWindowAction.addActionListener((e) -> {
            MasterMain.getExecutor().execute(this::openSelectedWebcam);
        });
        popupMenuRow.add(openWindowAction);

        popupMenuRow.addSeparator();

        JMenuItem refreshListActionRow = new JMenuItem("Refresh");
        refreshListActionRow.addActionListener((e) -> {
            MasterMain.getExecutor().execute(this::refreshWebcamList);
        });
        popupMenuRow.add(refreshListActionRow);

        /* ---- */

        popupMenuEmptyRow = new JPopupMenu();
        JMenuItem refreshListActionEmptyRow = new JMenuItem("Refresh");
        refreshListActionEmptyRow.addActionListener((e) -> {
            MasterMain.getExecutor().execute(this::refreshWebcamList);
        });
        popupMenuEmptyRow.add(refreshListActionEmptyRow);

        webcamDevicesTable.addMouseListener(new PopClickListener());
    }

    private class JTableRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean
                hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(noFocusBorder);
//            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }

    }

    private class PopClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            int rowAtPoint = webcamDevicesTable.rowAtPoint(e.getPoint());
            if (rowAtPoint == -1) {
                webcamDevicesTable.getSelectionModel().clearSelection();
            }
            if (e.isPopupTrigger()) {
                doPop(e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
        }

        private void doPop(MouseEvent e) {
            int rowAtPoint = webcamDevicesTable.rowAtPoint(e.getPoint());
            if (rowAtPoint > -1) {
                popupMenuRow.show(e.getComponent(), e.getX(), e.getY());
                webcamDevicesTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
            } else {
                popupMenuEmptyRow.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void refreshWebcamList() {
        tableModel.clear();
        List<WebcamListGUIObserver> tmp = new ArrayList<>(observerList);
        for (WebcamListGUIObserver observer : tmp) {
            observer.webcamListRefreshRequest();
        }
    }

    private void openSelectedWebcam() {
        int selectedIndex = webcamDevicesTable.getSelectedRow();
        if (selectedIndex < 0) {
            return;
        }
        int realTableIndex = selectedIndex;
//        int realTableIndex = webcamDevicesTable.getRowSorter().convertRowIndexToModel(selectedIndex);
//        if (realTableIndex < 0) {
//            return;
//        }

        List<WebcamListGUIObserver> tmp = new ArrayList<>(observerList);
        for (WebcamListGUIObserver observer : tmp) {
            observer.webcamSelected(tableModel.getDeviceName(realTableIndex));
        }
    }

    public void register(WebcamListGUIObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(WebcamListGUIObserver observer) {
        observerList.remove(observer);
    }

    public WebcamDevicesTableModel getListModel() {
        return tableModel;
    }
}
