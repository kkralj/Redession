package rat.master.gui.frames;

import rat.master.MasterMain;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DownloadExecuteGUI extends JFrame {

    private List<DownloadExecuteGUIObserver> observerList = new ArrayList<>();

    private JTextArea txtAddress;

    public DownloadExecuteGUI() {
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(300, 200));
        initGUI();
        pack();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JLabel lblAddressInfo = new JLabel("Enter direct address to jar file");
        northPanel.add(lblAddressInfo, BorderLayout.CENTER);
        mainPanel.add(northPanel, BorderLayout.BEFORE_FIRST_LINE);

        txtAddress = new JTextArea(3, 20);
        txtAddress.setText("http://www.example.com/application.jar");
        txtAddress.setBorder(new LineBorder(Color.BLACK));
        txtAddress.setLineWrap(true);
        mainPanel.add(new JScrollPane(txtAddress), BorderLayout.CENTER);

        GridLayout buttonsLayout = new GridLayout(1, 0);
        buttonsLayout.setHgap(2);
        JPanel buttonsPanel = new JPanel(buttonsLayout);
        buttonsPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        JButton btnOK = new JButton("OK");
        btnOK.addActionListener((e) -> {
            String URL = txtAddress.getText().trim();
            notifyObserververs(URL);
            dispose();
        });
        buttonsPanel.add(btnOK);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener((e) -> {
            dispose();
        });
        buttonsPanel.add(btnCancel);
    }

    public void register(DownloadExecuteGUIObserver observer) {
        observerList.add(Objects.requireNonNull(observer));
    }

    public void unregister(DownloadExecuteGUIObserver observer) {
        observerList.remove(observer);
    }

    private void notifyObserververs(String URL) {
        System.out.println("URL: " + URL);
        MasterMain.getExecutor().submit(() -> {
            List<DownloadExecuteGUIObserver> tmp = new ArrayList<>(observerList);
            for (DownloadExecuteGUIObserver observer : tmp) {
                observer.downloadExecuteRequest(URL);
            }
        });
    }
}
