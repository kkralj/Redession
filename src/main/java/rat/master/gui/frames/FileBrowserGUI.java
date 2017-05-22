package rat.master.gui.frames;

import rat.master.MasterMain;
import rat.master.gui.models.DownloadManagerTableModel;
import rat.master.gui.models.DriveListTableModel;
import rat.master.gui.models.FileBrowserTableModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: activity log, execute file, add to downloads list, rename, open downloads folder, swing: folder sorting
public class FileBrowserGUI extends JFrame {

    private List<FileBrowserGUIObserver> observerList = new ArrayList<>();

    private FileBrowserTableModel tableModel = new FileBrowserTableModel();

    private DriveListTableModel driveTableModel = new DriveListTableModel();

    private DownloadManagerTableModel downloadManagerTableModel = new DownloadManagerTableModel();

    private JTable fileListTable;

    public FileBrowserGUI() {
        setMinimumSize(new Dimension(500, 500));
        initGUI();
        pack();
    }

    public DriveListTableModel getDriveTableModel() {
        return driveTableModel;
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        JPanel fileListPanel = new JPanel(new BorderLayout());
        fileListTable = new JTable(tableModel);
        fileListTable.setDefaultRenderer(Object.class, new MainRATWindow.JTableRenderer());
        fileListTable.addMouseListener(folderChangeAction);
        JScrollPane fileListScrollPane = new JScrollPane(
                fileListTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        fileListPanel.add(fileListScrollPane);

        FlowLayout fileListSouthPanelLayout = new FlowLayout(FlowLayout.LEFT);
        JPanel fileListSouthPanel = new JPanel(fileListSouthPanelLayout);
        fileListPanel.add(fileListSouthPanel, BorderLayout.SOUTH);
        fileListSouthPanelLayout.setHgap(2);
        fileListSouthPanelLayout.setVgap(2);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(refreshFolderAction);
        fileListSouthPanel.add(btnRefresh);

        JButton btnParent = new JButton("Parent folder");
        btnParent.addActionListener((e) -> {
            nofiyObservers("list", tableModel.getParentPath(), "");
        });
        fileListSouthPanel.add(btnParent);

        JButton btnCreateFolder = new JButton("Create folder");
        btnCreateFolder.addActionListener(createFolderAction);
        fileListSouthPanel.add(btnCreateFolder);

        JTable driveListTable = new JTable(driveTableModel);
        driveListTable.setDefaultRenderer(Object.class, new MainRATWindow.JTableRenderer());
        driveListTable.addMouseListener(driveChangeAction);
        JScrollPane driveListScrollPane = new JScrollPane(
                driveListTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        driveListScrollPane.setMaximumSize(new Dimension(175, 20));
        driveListScrollPane.setPreferredSize(new Dimension(175, 20));

        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, driveListScrollPane, fileListPanel);

        JPanel southPanel = new JPanel(new BorderLayout());
        JTable downloadManagerTable = new JTable(downloadManagerTableModel);
        downloadManagerTable.setPreferredScrollableViewportSize(new Dimension(0, 125));
        JPanel downloadManagerPanel = new JPanel(new BorderLayout());
        downloadManagerPanel.setBorder(new TitledBorder("Download manager"));
        downloadManagerPanel.add(new JScrollPane(downloadManagerTable), BorderLayout.CENTER);
        southPanel.add(downloadManagerPanel, BorderLayout.CENTER);

        JSplitPane totalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerSplitPane, southPanel);
        mainPanel.add(totalSplitPane, BorderLayout.CENTER);

        initPopupMenu();
    }

    private void initPopupMenu() {
        final JPopupMenu popupMenu = new JPopupMenu();
        fileListTable.setComponentPopupMenu(popupMenu);

        JMenuItem refreshRequest = new JMenuItem("Refresh");
        refreshRequest.addActionListener(refreshFolderAction);
        popupMenu.add(refreshRequest);

        JMenuItem downloadFileRequest = new JMenuItem("Download");
        downloadFileRequest.addActionListener(downloadFileAction);
        popupMenu.add(downloadFileRequest);

        JMenuItem deleteFileRequest = new JMenuItem("Delete");
        deleteFileRequest.addActionListener(deleteFileAction);
        popupMenu.add(deleteFileRequest);

        JMenuItem createFolderRequest = new JMenuItem("Create folder");
        createFolderRequest.addActionListener(createFolderAction);
        popupMenu.add(createFolderRequest);

        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int rowAtPoint = fileListTable.rowAtPoint(
                            SwingUtilities.convertPoint(popupMenu, new Point(0, 0), fileListTable));
                    if (rowAtPoint > -1) {
                        fileListTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
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

    private ActionListener refreshFolderAction = (e) -> {
        nofiyObservers("list", tableModel.getFolderPath(), "");
    };

    private ActionListener createFolderAction = (e) -> {
        JPanel dialogPanel = new JPanel(new GridLayout(0, 1));
        JTextField txtFolderName = new JTextField("New folder");
        dialogPanel.add(new JLabel("Enter new folder name:"));
        dialogPanel.add(txtFolderName);

        int result = JOptionPane.showConfirmDialog(
                null, dialogPanel, "Enter name of the folder",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String folderPath = tableModel.mergePaths(tableModel.getFolderPath(), txtFolderName.getText());
            nofiyObservers("createFolder", folderPath, tableModel.getFolderPath());
        }

    };

    private ActionListener deleteFileAction = (e) -> {
        int[] rows = fileListTable.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] > 0) {
                nofiyObservers("delete", tableModel.getPath(rows[i]), tableModel.getFolderPath());
            }
        }
    };

    private ActionListener downloadFileAction = (e) -> {
        int[] rows = fileListTable.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] > 0) {
                nofiyObservers("download", tableModel.getPath(rows[i]), tableModel.getFolderPath());
            }
        }
    };

    private MouseListener folderChangeAction = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            JTable table = (JTable) mouseEvent.getSource();
            Point p = mouseEvent.getPoint();
            int row = table.rowAtPoint(p);
            if (mouseEvent.getClickCount() == 2 && row != -1) {
                if (row == 0) {
                    nofiyObservers("list", tableModel.getParentPath(), "");
                } else if (tableModel.getValueAt(row, 1).equals("Folder")) {
                    nofiyObservers("list", tableModel.getPath(row), "");
                }
            }
        }
    };

    private MouseListener driveChangeAction = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            JTable table = (JTable) mouseEvent.getSource();
            Point p = mouseEvent.getPoint();
            int row = table.rowAtPoint(p);
            if (mouseEvent.getClickCount() == 2 && row != -1) {
                nofiyObservers("list", (String) driveTableModel.getValueAt(row, 0), "");
            }
        }
    };

    public FileBrowserTableModel getTableModel() {
        return tableModel;
    }

    public DownloadManagerTableModel getDownloadManagerTableModel() {
        return downloadManagerTableModel;
    }

    public void register(FileBrowserGUIObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(FileBrowserGUIObserver observer) {
        observerList.remove(observer);
    }

    private void nofiyObservers(String operation, String path, String folder) {
        MasterMain.getExecutor().submit(() -> {
            List<FileBrowserGUIObserver> tmp = new ArrayList<>(observerList);
            for (FileBrowserGUIObserver observer : tmp) {
                observer.fileBrowserRequest(operation, path, folder);
            }
        });
    }

}
