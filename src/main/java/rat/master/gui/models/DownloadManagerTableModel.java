package rat.master.gui.models;

import rat.master.FileReceiverObserver;
import rat.packets.FileDataPacket;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class DownloadManagerTableModel extends AbstractTableModel implements FileReceiverObserver {

    private List<DownloadDataEntry> downloads = new ArrayList<>();

    private static final String[] columnNames = new String[]{
            "File", "Progress", "Size", "Speed", "Status"
    };

    @Override
    public int getRowCount() {
        return downloads.size();
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
        DownloadDataEntry dataEntry = downloads.get(i);

        switch (i1) {
            case 0:
                return dataEntry.getRelativePath();
            case 1:
                return String.valueOf(dataEntry.getTransferred() * 100 / dataEntry.getTotalSize()) + "%";
            case 2: {
                double size = dataEntry.getTotalSize() / 1024.0;
                String extension = "KB";

                if (size >= 1024.0) {
                    size /= 1024;
                    extension = "MB";
                }

                return String.format("%.2f %s", size, extension);
            }
            case 3:
                return String.format("%.2f KB/s", dataEntry.getDownloadSpeed());
            case 4:
                return dataEntry.isTransferCompleted() ? "Completed" : "Downloading";
            default:
                return "-";
        }

    }

    @Override
    public void updateProgress(FileDataPacket dataPacket, long count, double downloadSpeed) {
        DownloadDataEntry dataEntry = null;
        for (DownloadDataEntry entry : downloads) {
            if (entry.getRelativePath().equals(dataPacket.getRelativePath())) {
                dataEntry = entry;
                break;
            }
        }

        if (dataEntry != null) {
            dataEntry.updateTransferred(count, downloadSpeed);
        } else {
            dataEntry = new DownloadDataEntry(dataPacket.getRelativePath(), count, dataPacket.getTotalSize(), downloadSpeed);
            downloads.add(dataEntry);
        }

        fireTableDataChanged();
    }

    private static class DownloadDataEntry {

        private String relativePath;

        private long transferred;
        private long totalSize;
        private double downloadSpeed;

        private boolean transferCompleted;

        public DownloadDataEntry(String relativePath, long transferred, long totalSize, double downloadSpeed) {
            this.relativePath = relativePath;
            this.transferred = transferred;
            this.totalSize = totalSize;
            this.downloadSpeed = downloadSpeed;
            this.transferCompleted = (transferred == totalSize ? true : false);
        }

        public void updateTransferred(long bytes, double downloadSpeed) {
            this.transferred = bytes;
            this.downloadSpeed = downloadSpeed;
            this.transferCompleted = (transferred == totalSize ? true : false);
        }

        public double getDownloadSpeed() {
            return downloadSpeed;
        }

        public boolean isTransferCompleted() {
            return transferCompleted;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public long getTransferred() {
            return transferred;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DownloadDataEntry that = (DownloadDataEntry) o;

            return relativePath != null ? relativePath.equals(that.relativePath) : that.relativePath == null;
        }

        @Override
        public int hashCode() {
            return relativePath != null ? relativePath.hashCode() : 0;
        }
    }
}
