package lambda.netty.loadbalancer.core;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslHandler;
import lambda.netty.loadbalancer.core.SysService.SysServiceHostResolveHandler;
import lambda.netty.loadbalancer.core.launch.Launcher;
import lambda.netty.loadbalancer.core.proxy.ProxyFrontendHandler;
import lambda.netty.loadbalancer.core.sslconfigs.SSLHandlerProvider;


public class ServerHandlersInit extends ChannelInitializer<SocketChannel> {

    private EventLoopGroup remoteHostEventLoopGroup;

    public ServerHandlersInit(EventLoopGroup remoteHostEventLoopGroup) {

        this.remoteHostEventLoopGroup = remoteHostEventLoopGroup;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline channelPipeline = socketChannel.pipeline();


        if (Server.ENABLE_SSL) {
            SslHandler sslHandler = SSLHandlerProvider.getSSLHandler();
            channelPipeline.addFirst(sslHandler);
        }
        channelPipeline.addLast(
                new HttpRequestDecoder(),
                new HttpObjectAggregator(Launcher.getIntValue(ConfigConstants.CONFIG_TRANSPORT_SERVER_HTTPOBJECTAGGREGATOR)),
                new SysServiceHostResolveHandler(remoteHostEventLoopGroup),
                new ProxyFrontendHandler());


    }

}
