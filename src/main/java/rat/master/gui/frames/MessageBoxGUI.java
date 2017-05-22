package rat.master.gui.frames;

import rat.master.MasterMain;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MessageBoxGUI extends JFrame {

    private List<MessageBoxGUIObserver> observerList = new ArrayList<>();

    private JPanel mainPanel;

    private JTextArea txtTitle, txtMessage;

    private Map<Integer, JRadioButton> messageBoxTypes = new HashMap<>();

    public MessageBoxGUI() {
        setMinimumSize(new Dimension(300, 250));
        // setResizable(false);
        initGUI();
    }

    private void initGUI() {
        mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        initIconTypesGroup();

        initCenterPanel();
    }

    private void initCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        Border border = BorderFactory.createLineBorder(Color.BLACK);

        JPanel panelTitle = new JPanel(new BorderLayout());
        panelTitle.setBorder(new EmptyBorder(5, 0, 0, 5));
        txtTitle = new JTextArea("Title");
        txtTitle.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        panelTitle.add(txtTitle, BorderLayout.CENTER);
        centerPanel.add(panelTitle, BorderLayout.NORTH);

        JPanel panelMessage = new JPanel(new BorderLayout());
        panelMessage.setBorder(new EmptyBorder(5, 0, 0, 5));
        txtMessage = new JTextArea("Message");
        txtMessage.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        txtMessage.setLineWrap(true);
        panelMessage.add(txtMessage, BorderLayout.CENTER);
        centerPanel.add(panelMessage, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new BorderLayout());
        panelButtons.setBorder(new EmptyBorder(5, 0, 5, 5));
        JButton btnSend = new JButton("Send");
        panelButtons.add(btnSend, BorderLayout.CENTER);
        centerPanel.add(panelButtons, BorderLayout.SOUTH);

        btnSend.addActionListener((e) -> {
            notifyObserververs(txtTitle.getText(), txtMessage.getText(), getMessageBoxType());
        });
    }

    private int getMessageBoxType() {
        for (Map.Entry<Integer, JRadioButton> radioButtonEntry : messageBoxTypes.entrySet()) {
            if (radioButtonEntry.getValue().isSelected()) {
                return radioButtonEntry.getKey();
            }
        }
        return JOptionPane.PLAIN_MESSAGE;
    }

    private void initIconTypesGroup() {
        JRadioButton btnMessageInformation = new JRadioButton("Information");
        JRadioButton btnMessageError = new JRadioButton("Error");
        JRadioButton btnMessageWarning = new JRadioButton("Warning");
        JRadioButton btnMessageQuestion = new JRadioButton("Question");
        JRadioButton btnMessagePlain = new JRadioButton("No icon");
        btnMessageInformation.setSelected(true);

        messageBoxTypes.put(JOptionPane.INFORMATION_MESSAGE, btnMessageInformation);
        messageBoxTypes.put(JOptionPane.ERROR_MESSAGE, btnMessageError);
        messageBoxTypes.put(JOptionPane.WARNING_MESSAGE, btnMessageWarning);
        messageBoxTypes.put(JOptionPane.QUESTION_MESSAGE, btnMessageQuestion);
        messageBoxTypes.put(JOptionPane.PLAIN_MESSAGE, btnMessagePlain);

        ButtonGroup messageTypesGroup = new ButtonGroup();
        messageTypesGroup.add(btnMessageInformation);
        messageTypesGroup.add(btnMessageError);
        messageTypesGroup.add(btnMessageWarning);
        messageTypesGroup.add(btnMessageQuestion);
        messageTypesGroup.add(btnMessagePlain);

        JLabel lblInformationRadioButton = new JLabel();
        lblInformationRadioButton.setIcon(UIManager.getIcon("OptionPane.informationIcon"));

        JLabel lblErrorRadioButton = new JLabel();
        lblErrorRadioButton.setIcon(UIManager.getIcon("OptionPane.errorIcon"));

        JLabel lblWarningRadioButton = new JLabel();
        lblWarningRadioButton.setIcon(UIManager.getIcon("OptionPane.warningIcon"));

        JLabel lblQuestionRadioButton = new JLabel();
        lblQuestionRadioButton.setIcon(UIManager.getIcon("OptionPane.questionIcon"));

        JPanel pnlInformationCategory = new JPanel(new BorderLayout());
        pnlInformationCategory.add(lblInformationRadioButton, BorderLayout.WEST);
        pnlInformationCategory.add(btnMessageInformation, BorderLayout.CENTER);

        JPanel pnlErrorCategory = new JPanel(new BorderLayout());
        pnlErrorCategory.add(lblErrorRadioButton, BorderLayout.WEST);
        pnlErrorCategory.add(btnMessageError, BorderLayout.CENTER);

        JPanel pnlWarningCategory = new JPanel(new BorderLayout());
        pnlWarningCategory.add(lblWarningRadioButton, BorderLayout.WEST);
        pnlWarningCategory.add(btnMessageWarning, BorderLayout.CENTER);

        JPanel pnlPlainCategory = new JPanel(new BorderLayout());
        pnlPlainCategory.add(btnMessagePlain, BorderLayout.CENTER);

        JPanel pnlQuestionCategory = new JPanel(new BorderLayout());
        pnlQuestionCategory.add(lblQuestionRadioButton, BorderLayout.WEST);
        pnlQuestionCategory.add(btnMessageQuestion, BorderLayout.CENTER);

        JPanel messageTypesPanel = new JPanel(new GridLayout(0, 1));
        messageTypesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        messageTypesPanel.add(pnlInformationCategory);
        messageTypesPanel.add(pnlErrorCategory);
        messageTypesPanel.add(pnlWarningCategory);
        messageTypesPanel.add(pnlQuestionCategory);
        messageTypesPanel.add(pnlPlainCategory);

        mainPanel.add(messageTypesPanel, BorderLayout.WEST);
    }

    public void register(MessageBoxGUIObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(MessageBoxGUIObserver observer) {
        observerList.remove(observer);
    }

    private void notifyObserververs(String title, String message, int messageBoxType) {
        MasterMain.getExecutor().submit(() -> {
            List<MessageBoxGUIObserver> tmp = new ArrayList<>(observerList);
            for (MessageBoxGUIObserver observer : tmp) {
                observer.sendMessage(title, message, messageBoxType);
            }
        });
    }

}
