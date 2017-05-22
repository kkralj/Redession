package rat.master.gui.frames;

import rat.master.MasterMain;
import rat.master.gui.models.ProcessListTableModel;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessListGUI extends JFrame {

    private List<ProcessListGUIObserver> observerList = new ArrayList<>();

    private ProcessListTableModel tableModel = new ProcessListTableModel();

    private JTable processListTable;

    public ProcessListGUI() {
        setMinimumSize(new Dimension(300, 300));
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        processListTable = new JTable(tableModel);
        processListTable.setAutoCreateRowSorter(true);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);

        processListTable.setDefaultRenderer(String.class, new MainRATWindow.JTableRenderer());
        processListTable.setDefaultRenderer(Long.class, new MainRATWindow.JTableRenderer());
        processListTable.setDefaultRenderer(Integer.class, new MainRATWindow.JTableRenderer());

        JScrollPane fileListScrollPane = new JScrollPane(processListTable);

        mainPanel.add(fileListScrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener((e) -> notifyObserververs());
        mainPanel.add(refreshButton, BorderLayout.SOUTH);

        final JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem killProcessOption = new JMenuItem("Kill");
        killProcessOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                killSelectedProcess();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                }
                notifyObserververs();
            });
        });
        popupMenu.add(killProcessOption);

        processListTable.setComponentPopupMenu(popupMenu);

        popupMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int rowAtPoint = processListTable.rowAtPoint(
                            SwingUtilities.convertPoint(popupMenu, new Point(0, 0), processListTable));
                    if (rowAtPoint > -1) {
                        processListTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                    }
                });
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
    }

    private void killSelectedProcess() {
        int selectedIndex = processListTable.getSelectedRow();
        if (selectedIndex < 0) {
            return;
        }
        int realTableIndex = processListTable.getRowSorter().convertRowIndexToModel(selectedIndex);
        if (realTableIndex < 0) {
            return;
        }

        List<ProcessListGUIObserver> tmp = new ArrayList<>(observerList);
        for (ProcessListGUIObserver observer : tmp) {
            observer.killProcess(tableModel.getPID(realTableIndex));
        }
    }

    public ProcessListTableModel getTableModel() {
        return tableModel;
    }

    public void register(ProcessListGUIObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(ProcessListGUIObserver observer) {
        observerList.remove(observer);
    }

    private void notifyObserververs() {
        MasterMain.getExecutor().submit(() -> {
            List<ProcessListGUIObserver> tmp = new ArrayList<>(observerList);
            for (ProcessListGUIObserver observer : tmp) {
                observer.processRefreshRequested();
            }
        });
    }
}
