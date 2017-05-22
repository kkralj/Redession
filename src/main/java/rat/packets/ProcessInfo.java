package rat.packets;

import java.io.Serializable;
import java.util.Objects;

public class ProcessInfo implements Serializable, Comparable<ProcessInfo> {

    private static final long serialVersionUID = 241680236916542860L;

    private int PID;

    private String name;

    private String location;

    private long memory;

    public ProcessInfo(int PID, String name, String location, long memory) {
        this.PID = PID;
        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
        this.memory = memory;
    }

    public long getMemory() {
        return memory;
    }

    public int getPID() {
        return PID;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public int compareTo(ProcessInfo processInfo) {
        if (processInfo == null) {
            return -1;
        }
        return name.compareTo(processInfo.getName());
    }
}
