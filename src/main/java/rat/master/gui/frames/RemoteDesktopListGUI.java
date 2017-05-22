package rat.master.gui.frames;

import rat.master.MasterMain;
import rat.master.gui.models.RemoteDesktopDevicesTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteDesktopListGUI extends JFrame {

    private List<RemoteDesktopListGUIObserver> observerList = new ArrayList<>();

    private JTable desktopDevicesTable;

    private RemoteDesktopDevicesTableModel tableModel = new RemoteDesktopDevicesTableModel();

    private JPopupMenu popupMenuRow, popupMenuEmptyRow;


    public RemoteDesktopListGUI() {
        setMinimumSize(new Dimension(400, 300));
        initGUI();
    }


    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        JPanel centerMarginPanel = new JPanel(new BorderLayout());
        centerMarginPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(centerMarginPanel, BorderLayout.CENTER);

        desktopDevicesTable = new JTable(tableModel);
        desktopDevicesTable.setFillsViewportHeight(true);
        desktopDevicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        desktopDevicesTable.setDefaultRenderer(String.class, new JTableRenderer());
        centerMarginPanel.add(new JScrollPane(desktopDevicesTable), BorderLayout.CENTER);

        popupMenuRow = new JPopupMenu();

        JMenuItem openWindowAction = new JMenuItem("Open");
        openWindowAction.addActionListener((e) -> {
            MasterMain.getExecutor().execute(this::openSelectedDesktop);
        });
        popupMenuRow.add(openWindowAction);

        popupMenuRow.addSeparator();

        JMenuItem refreshListActionRow = new JMenuItem("Refresh");
        refreshListActionRow.addActionListener((e) -> {
            MasterMain.getExecutor().execute(this::refreshDesktopList);
        });
        popupMenuRow.add(refreshListActionRow);

        /* ---- */

        popupMenuEmptyRow = new JPopupMenu();
        JMenuItem refreshListActionEmptyRow = new JMenuItem("Refresh");
        refreshListActionEmptyRow.addActionListener((e) -> {
            MasterMain.getExecutor().execute(this::refreshDesktopList);
        });
        popupMenuEmptyRow.add(refreshListActionEmptyRow);

        desktopDevicesTable.addMouseListener(new PopClickListener());
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
            int rowAtPoint = desktopDevicesTable.rowAtPoint(e.getPoint());
            if (rowAtPoint == -1) {
                desktopDevicesTable.getSelectionModel().clearSelection();
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
            int rowAtPoint = desktopDevicesTable.rowAtPoint(e.getPoint());
            if (rowAtPoint > -1) {
                popupMenuRow.show(e.getComponent(), e.getX(), e.getY());
                desktopDevicesTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
            } else {
                popupMenuEmptyRow.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void refreshDesktopList() {
        tableModel.clear();
        List<RemoteDesktopListGUIObserver> tmp = new ArrayList<>(observerList);
        for (RemoteDesktopListGUIObserver observer : tmp) {
            observer.remoteDesktopListRefreshRequest();
        }
    }

    private void openSelectedDesktop() {
        int selectedIndex = desktopDevicesTable.getSelectedRow();
        if (selectedIndex < 0) {
            return;
        }
        int realTableIndex = selectedIndex;
//        int realTableIndex = webcamDevicesTable.getRowSorter().convertRowIndexToModel(selectedIndex);
//        if (realTableIndex < 0) {
//            return;
//        }

        List<RemoteDesktopListGUIObserver> tmp = new ArrayList<>(observerList);
        for (RemoteDesktopListGUIObserver observer : tmp) {
            observer.remoteDesktopSelected(tableModel.getDeviceName(realTableIndex));
        }
    }

    public void register(RemoteDesktopListGUIObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(RemoteDesktopListGUIObserver observer) {
        observerList.remove(observer);
    }

    public RemoteDesktopDevicesTableModel getListModel() {
        return tableModel;
    }
}
