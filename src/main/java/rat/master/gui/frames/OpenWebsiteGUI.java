package rat.master.gui.frames;

import rat.master.MasterMain;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenWebsiteGUI extends JFrame {

    private List<OpenWebsiteGUIObserver> observerList = new ArrayList<>();

    private JButton confirmButton;

    private JTextArea urlInput;

    private JLabel urlStatus;

    public OpenWebsiteGUI() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(200, 200));
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        mainPanel.add(northPanel, BorderLayout.NORTH);
        JLabel lblEnterWebsite = new JLabel("Enter website address");
        northPanel.add(lblEnterWebsite, BorderLayout.CENTER);

        urlInput = new JTextArea("http://www.google.com");
        urlInput.setLineWrap(true);
        urlInput.setBorder(new LineBorder(Color.BLACK));
        mainPanel.add(new JScrollPane(urlInput), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        confirmButton = new JButton("OK");
        southPanel.add(confirmButton, BorderLayout.CENTER);

        confirmButton.addActionListener((e) -> {
            MasterMain.getExecutor().submit(() -> {
                notifyListeners(urlInput.getText());
            });
        });
    }

    public void register(OpenWebsiteGUIObserver observer) {
        if (!observerList.contains(Objects.requireNonNull(observer))) {
            observerList.add(observer);
        }
    }

    public void unregister(OpenWebsiteGUIObserver observer) {
        observerList.remove(observer);
    }

    private void notifyListeners(String url) {
        List<OpenWebsiteGUIObserver> tmp = new ArrayList<>(observerList);
        for (OpenWebsiteGUIObserver observer : tmp) {
            observer.websiteEntered(url);
        }
    }
}
