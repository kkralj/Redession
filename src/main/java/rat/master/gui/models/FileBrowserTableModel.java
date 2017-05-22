package rat.master.gui.models;

import rat.packets.DirectoryListingPacket;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.regex.Pattern;

public class FileBrowserTableModel extends AbstractTableModel implements IFileDirectoryObserver {

    private DirectoryListingPacket directoryList;

    private int folderCount, fileCount;

    private static final String[] columnNames = new String[]{
            "Name", "Type", "Size", "Permissions", "Creation date",
            "Last access", "Last modified", "Bytes", "Symbolic link",
            "Visibility status"
    };

    @Override
    public int getRowCount() {
        return folderCount + fileCount + 1;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int i) {
        return columnNames[i];
    }

    @Override
    public Object getValueAt(int i, int i1) {
        if (i == 0) {
            switch (i1) {
                case 0:
                    return "..";
                default:
                    return "";
            }
        }
        i--; // ignore first row

        DirectoryListingPacket.DirectoryFileEntry fileEntry;
        if (i < folderCount) {
            fileEntry = directoryList.getFolders().get(i);
        } else {
            fileEntry = directoryList.getFiles().get(i - folderCount);
        }

        switch (i1) {
            case 0:
                return fileEntry.getName();
            case 1:
                return fileEntry.getType();
            case 2: {
                if (fileEntry.getSize() < 0) {
                    return "";
                }

                double fileSize = fileEntry.getSize();
                String extension = "bytes";

                if (fileSize >= 1024.0) {
                    fileSize /= 1024.0;
                    extension = "KB";
                }

                if (fileSize >= 1024.0) {
                    fileSize /= 1024.0;
                    extension = "MB";
                }

                if (fileSize >= 1024.0) {
                    fileSize /= 1024.0;
                    extension = "GB";
                }

                return String.format("%.2f %s", fileSize, extension);
            }
            case 3:
                return fileEntry.getPermissions();
            case 4:
                return fileEntry.getCreationTime();
            case 5:
                return fileEntry.getLastAccessTime();
            case 6:
                return fileEntry.getLastModifiedTime();
            case 7:
                return fileEntry.getSize() >= 0 ? String.valueOf(fileEntry.getSize()) : "";
            case 8:
                return fileEntry.isSymbolicLink() ? "Yes" : "No";
            case 9:
                return fileEntry.isHidden() ? "Hidden" : "Visible";
            default:
                return "";
        }

    }

    public String mergePaths(String root, String extra) {
        String slash = directoryList.getSlash();

        if (!root.endsWith(slash)) {
            root += slash;
        }

        root += extra;

        if (!root.endsWith(slash)) {
            root += slash;
        }

        return root;
    }

    public String getParentPath() {
        String dirPath = directoryList.getDirectoryPath();
        String slash = directoryList.getSlash();

        int slashCount = dirPath.length() - dirPath.replaceAll(Pattern.quote(slash), "").length();
        if (slashCount <= 1) {
            return dirPath; // root path, can't go up
        }

        if (dirPath.endsWith(slash)) {
            dirPath = dirPath.substring(0, dirPath.length() - 1);
        }

        int lastSlash = dirPath.lastIndexOf(slash);
        return dirPath.substring(0, lastSlash + 1);
    }

    public String getFolderPath() {
        return directoryList.getDirectoryPath();
    }

    public String getPath(int row) {
        row--;

        String name = (row >= folderCount) ? directoryList.getFiles().get(row - folderCount).getName() :
                directoryList.getFolders().get(row).getName();

        String dirPath = directoryList.getDirectoryPath();
        String slash = directoryList.getSlash();

        if (!dirPath.endsWith(slash)) {
            dirPath += slash;
        }

        return dirPath + name + slash;
    }

    @Override
    public void directoryChanged(DirectoryListingPacket packet) {
        this.directoryList = packet;
        folderCount = packet.getFolders().size();
        fileCount = packet.getFiles().size();

        Collections.sort(directoryList.getFolders());
        Collections.sort(directoryList.getFiles());

        SwingUtilities.invokeLater(() -> {
            fireTableDataChanged();
        });
    }
}
