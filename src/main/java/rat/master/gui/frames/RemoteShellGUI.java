package rat.master.gui.frames;

import rat.master.MasterMain;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteShellGUI extends JFrame {

    private List<RemoteShellGUIObserver> observerList = new ArrayList<>();

    private JTextArea messageBox;
    private JTextArea chatBox;

    public RemoteShellGUI() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 400));
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        Border border = BorderFactory.createLineBorder(Color.BLACK);

        messageBox = new JTextArea(5, 10);
        messageBox.requestFocusInWindow();
        messageBox.setLineWrap(true);
        messageBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        messageBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                messageBox.setBorder(BorderFactory.createCompoundBorder(border,
                        BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                messageBox.setBorder(null);
            }
        });
        JPanel messageBoxBorder = new JPanel(new BorderLayout());
        messageBoxBorder.add(new JScrollPane(messageBox), BorderLayout.CENTER);
        messageBoxBorder.setBorder(new EmptyBorder(2, 0, 2, 0));
        mainPanel.add(messageBoxBorder, BorderLayout.SOUTH);


        JPanel chatBoxPanel = new JPanel(new BorderLayout());
        chatBoxPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        chatBox = new JTextArea();
        chatBox.setBorder(new EmptyBorder(5, 5, 5, 5));
        chatBox.setEditable(false);
        chatBox.setLineWrap(true);
        chatBoxPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);
        mainPanel.add(chatBoxPanel, BorderLayout.CENTER);
    }

    public void register(RemoteShellGUIObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(RemoteShellGUIObserver observer) {
        observerList.remove(observer);
    }

    private void notifyObserververs(String command) {
        System.out.println("Sending command: " + command);
        MasterMain.getExecutor().submit(() -> {
            List<RemoteShellGUIObserver> tmp = new ArrayList<>(observerList);
            for (RemoteShellGUIObserver observer : tmp) {
                observer.shellCommandEntered(command);
            }
        });
    }

    public void responseReceived(String response) {
        if (!response.endsWith("\n")) {
            response += "\n";
        }
        chatBox.append(response);
        messageBox.requestFocusInWindow();
    }

    private void sendMessage() {
        if (messageBox.getText().trim().length() >= 1) {
            notifyObserververs(messageBox.getText());
        }
        messageBox.setText("");
        messageBox.requestFocusInWindow();
    }
}
