package rat.master.gui.frames;

import rat.master.MasterMain;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ChatGUI extends JFrame {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int MAX_MESSAGE_LENGTH = 4096;

    private List<ChatGUIObserver> observerList = new ArrayList<>();
    private JTextArea messageBox;
    private JButton sendMessage;
    private JTextArea chatBox;
    private JButton btnNudge;
    private JButton btnExport;
    private JLabel lblRemainingChars;

    public ChatGUI() {
        setMinimumSize(new Dimension(400, 300));
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        Border border = BorderFactory.createLineBorder(Color.BLACK);

        messageBox = new JTextArea(5, 10);
        messageBox.requestFocusInWindow();
        messageBox.setLineWrap(true);
        ((PlainDocument) messageBox.getDocument()).setDocumentFilter(new SizeFilter(MAX_MESSAGE_LENGTH));
        messageBox.getDocument().addDocumentListener(messageBoxDocumentListener);
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


        JPanel controlButtonsPanel = new JPanel(new GridLayout(1, 0));

        sendMessage = new JButton("Send");
        sendMessage.addActionListener((e) -> sendMessage());
        JPanel btnSendPanel = new JPanel(new BorderLayout());
        btnSendPanel.setBorder(new EmptyBorder(2, 0, 2, 2));
        btnSendPanel.add(sendMessage, BorderLayout.CENTER);
        controlButtonsPanel.add(btnSendPanel);

        btnNudge = new JButton("Nudge");
        btnNudge.addActionListener((e) -> sendNudge());
        JPanel btnNudgePanel = new JPanel(new BorderLayout());
        btnNudgePanel.setBorder(new EmptyBorder(2, 0, 2, 2));
        btnNudgePanel.add(btnNudge, BorderLayout.CENTER);
        controlButtonsPanel.add(btnNudgePanel);

        btnExport = new JButton("Export");
        btnExport.addActionListener(exportTextAction);
        JPanel btnExportPanel = new JPanel(new BorderLayout());
        btnExportPanel.setBorder(new EmptyBorder(2, 0, 2, 4));
        btnExportPanel.add(btnExport, BorderLayout.CENTER);
        controlButtonsPanel.add(btnExportPanel);

        lblRemainingChars = new JLabel();
        refreshRemainingChars();

        JPanel southButtonInfoPanel = new JPanel(new BorderLayout());
        southButtonInfoPanel.add(controlButtonsPanel, BorderLayout.CENTER);
        southButtonInfoPanel.add(lblRemainingChars, BorderLayout.EAST);

        JPanel chatBoxPanel = new JPanel(new BorderLayout());
        chatBoxPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        chatBox = new JTextArea();
        chatBox.setBorder(new EmptyBorder(5, 5, 5, 5));
        chatBox.setEditable(false);
        chatBox.setLineWrap(true);
        chatBoxPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);
        mainPanel.add(chatBoxPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
        southPanel.add(messageBoxBorder, BorderLayout.CENTER);
        southPanel.add(southButtonInfoPanel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);
    }


    public void register(ChatGUIObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(ChatGUIObserver observer) {
        observerList.remove(observer);
    }

    private void notifyObserververs(String message) {
        MasterMain.getExecutor().submit(() -> {
            List<ChatGUIObserver> tmp = new ArrayList<>(observerList);
            for (ChatGUIObserver observer : tmp) {
                observer.sendChatMessage(message);
            }
        });
    }

    private void sendNudge() {
        MasterMain.getExecutor().submit(() -> {
            List<ChatGUIObserver> tmp = new ArrayList<>(observerList);
            for (ChatGUIObserver observer : tmp) {
                observer.sendNudge();
            }
        });
    }

    private void sendMessage() {
        if (messageBox.getText().trim().length() >= 1) {
            notifyObserververs(messageBox.getText());
            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            String chatMessage = String.format("[%s]  <You>: %s", timeStamp, messageBox.getText());
            if (!chatMessage.endsWith("\n")) {
                chatMessage += "\n";
            }
            chatBox.append(chatMessage);
            chatBox.setCaretPosition(chatBox.getDocument().getLength());
        }
        messageBox.setText("");
        messageBox.requestFocusInWindow();
    }

    public void messageReceived(String message) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        String chatMessage = String.format("[%s]  <Him>: %s", timeStamp, message);
        if (!chatMessage.endsWith("\n")) {
            chatMessage += "\n";
        }
        chatBox.append(chatMessage);
        messageBox.requestFocusInWindow();
    }

    private void refreshRemainingChars() {
        lblRemainingChars.setText("Characters remaining: " + (MAX_MESSAGE_LENGTH - messageBox.getText().length()));
    }

    private DocumentListener messageBoxDocumentListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            refreshRemainingChars();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            refreshRemainingChars();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            refreshRemainingChars();
        }
    };

    private ActionListener exportTextAction = (e) -> {
        String text = chatBox.getText();
        text = text.replaceAll("\n", LINE_SEPARATOR);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (fileToSave.exists()) {
                int dialogResult = JOptionPane.showConfirmDialog(
                        null,
                        "File already existing. Do you want to override?",
                        "Warning",
                        JOptionPane.YES_NO_OPTION
                );
                if (dialogResult != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            try (BufferedOutputStream writer = new BufferedOutputStream(
                    new FileOutputStream(fileToSave.getAbsolutePath()))) {
                writer.write(text.getBytes());
                JOptionPane.showMessageDialog(
                        null,
                        "File sucessfully saved.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    };

    private class SizeFilter extends DocumentFilter {

        private int maxCharacters;

        public SizeFilter(int maxChars) {
            maxCharacters = maxChars;
        }

        public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
                throws BadLocationException {

            if ((fb.getDocument().getLength() + str.length()) <= maxCharacters)
                super.insertString(fb, offs, str, a);
            else
                Toolkit.getDefaultToolkit().beep();
        }

        public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a)
                throws BadLocationException {

            if ((fb.getDocument().getLength() + str.length()
                    - length) <= maxCharacters)
                super.replace(fb, offs, length, str, a);
            else
                Toolkit.getDefaultToolkit().beep();
        }
    }

}
