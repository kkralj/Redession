package rat.master.gui.models;

import rat.master.ClientController;
import rat.packets.BasicInformationPacket;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ClientTableModel extends AbstractTableModel implements IClientObserver {

    private List<ClientController> clients = Collections.synchronizedList(new ArrayList<>());

    private static final String[] columnNames = new String[]{
            "Computer name", "Username", "OS", "Cores", "Architecture", "RAM", "External IP"
    };

    @Override
    public int getRowCount() {
        return clients.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int i) {
        return columnNames[i];
    }

    @Override
    public Object getValueAt(int row, int column) {
        ClientController client = clients.get(row);
        BasicInformationPacket info = client.getModel().getInformationPacket();

        switch (column) {
            case 0:
                return info.getComputerName();
            case 1:
                return info.getUserName();
            case 2:
                return info.getOS();
            case 3:
                return info.getCores();
            case 4:
                return info.getOSArchitecture();
            case 5:
                return String.format("%.2f GB", ((1.0 * info.getRAM() / 1024 / 1024) / 1024));
            case 6:
                return info.getExternalIPAddress();
            default:
                return "-";
        }
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @Override
    public void clientConnected(ClientController clientController) {
        clients.add(Objects.requireNonNull(clientController));

        clientController.getModel().register(this);

        SwingUtilities.invokeLater(() -> {
            fireTableDataChanged();
        });
    }

    @Override
    public void clientDataChanged(ClientController clientController) {
        SwingUtilities.invokeLater(() -> {
            fireTableDataChanged();
        });
    }

    @Override
    public void clientDisconnected(ClientController clientController) {
        clients.remove(Objects.requireNonNull(clientController));

        clientController.getModel().unregister(this);

        SwingUtilities.invokeLater(() -> {
            fireTableDataChanged();
        });
    }

    public ClientController getClient(int index) {
        return clients.get(index);
    }
}
