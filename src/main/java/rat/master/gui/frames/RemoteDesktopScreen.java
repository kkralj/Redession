package rat.master.gui.frames;

import rat.master.MasterMain;
import rat.packets.ScreenshotDataPacket;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteDesktopScreen extends JFrame {

    private List<RemoteDesktopScreenObserver> observerList = new ArrayList<>();

    private JPanel imagePanel = new JPanel();
    private JSlider framesPerSecond;

    public static final int DEFAULT_FPS = 20;
    public static final int MIN_FPS = 1;
    public static final int MAX_FPS = 30;

    public static final int INITIAL_FRAME_WIDTH = 550;
    public static final int INITIAL_FRAME_HEIGHT = 250;

    public RemoteDesktopScreen() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(INITIAL_FRAME_WIDTH, INITIAL_FRAME_HEIGHT));
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        framesPerSecond = new JSlider(JSlider.HORIZONTAL, MIN_FPS, MAX_FPS, DEFAULT_FPS);

        framesPerSecond.setUI(new MetalSliderUI() {
            protected void scrollDueToClickInTrack(int direction) {
                int value = slider.getValue();

                if (slider.getOrientation() == JSlider.HORIZONTAL) {
                    value = this.valueForXPosition(slider.getMousePosition().x);
                } else if (slider.getOrientation() == JSlider.VERTICAL) {
                    value = this.valueForYPosition(slider.getMousePosition().y);
                }
                slider.setValue(value);
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

        framesPerSecond.setMajorTickSpacing(1);
        framesPerSecond.setMinorTickSpacing(1);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);

        framesPerSecond.addChangeListener((e) -> {
            MasterMain.getExecutor().submit(() -> {
                notifyCharacteristicsChanged();
            });
        });

        mainPanel.add(framesPerSecond, BorderLayout.SOUTH);
        mainPanel.add(imagePanel, BorderLayout.CENTER);
    }

    public void packetReceived(ScreenshotDataPacket imgPacket) {
        byte[] screenshotBytes = imgPacket.getScreenshot();
        ByteArrayInputStream inb = new ByteArrayInputStream(screenshotBytes);
        Graphics gb = imagePanel.getGraphics();

        try {
            BufferedImage buffimg = ImageIO.read(inb);
            gb.drawImage(buffimg, 0, 0, null);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void register(RemoteDesktopScreenObserver observer) {
        if (!observerList.contains(Objects.requireNonNull(observer))) {
            observerList.add(Objects.requireNonNull(observer));
        }
    }

    public void unregister(RemoteDesktopScreenObserver observer) {
        observerList.remove(observer);
    }

    private void notifyCharacteristicsChanged() {
        List<RemoteDesktopScreenObserver> tmp = new ArrayList<>(observerList);
        for (RemoteDesktopScreenObserver observer : tmp) {
            observer.remoteDesktopGUIUpdated(framesPerSecond.getValue(), imagePanel.getWidth(), imagePanel.getHeight());
        }
    }
}