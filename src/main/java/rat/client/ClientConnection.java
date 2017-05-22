package rat.client;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ClientConnection {

    private static volatile boolean active = true;
    private static ClientBootstrap bootstrap;
    private static ChannelFuture channelFuture;

    static {
        initBootstrap();
    }

    private static void initBootstrap() {
        // Configure the client.
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setOption("child.keepAlive", true);

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                ChannelPipeline p = Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),
                        new ConnectionHandler()
                );
                return p;
            }
        });
    }

    public static void connect(String hostName, int connectionPort) {
        while (active) {
            try {
                System.out.println("Connecting...");

                // Start the connection attempt.
                InetSocketAddress socketAddress = new InetSocketAddress(hostName, connectionPort);
                ClientConnection.channelFuture = bootstrap.connect(socketAddress);

                // Blocking while connected.
                channelFuture.sync().getChannel().getCloseFuture().sync();

            } catch (Exception ex) {
            } finally {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
            }
        }

        bootstrap.releaseExternalResources();
    }

    public static void deactivate() {
        active = false;
        channelFuture.getChannel().close();

    }
}
