package rat.master;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MasterMain {

    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static void main(String[] args) throws Exception {

        Server server = new Server();
        server.getMasterConnectionHandler().startRATGUI();

        // TODO: clipboard
        // TODO: file browser download with progress bar
        // TODO: multiple ports bind

//        SwingUtilities.invokeLater(() -> {
//            new ChatGUI().setVisible(true);
//        });

    }

}
