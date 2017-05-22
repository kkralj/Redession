package rat.client;

import org.jboss.netty.channel.*;
import rat.packets.IPacket;

public class ConnectionHandler extends SimpleChannelUpstreamHandler {

    private ClientMain clientMain = new ClientMain();

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        System.out.println("I'm connected.");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        IPacket packet = (IPacket) e.getMessage();
        clientMain.packetReceived(e.getChannel(), packet);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("I'm closed.");
        clientMain.stopWork();
        super.channelClosed(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("I'm disconnected.");
        clientMain.stopWork();
        super.channelDisconnected(ctx, e);
    }

}
