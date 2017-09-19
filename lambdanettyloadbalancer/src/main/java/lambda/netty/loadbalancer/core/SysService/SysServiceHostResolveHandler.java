package lambda.netty.loadbalancer.core.SysService;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lambda.netty.loadbalancer.core.ConfigConstants;
import lambda.netty.loadbalancer.core.etcd.EtcdClientException;
import lambda.netty.loadbalancer.core.etcd.EtcdUtil;
import lambda.netty.loadbalancer.core.launch.Launcher;
import lambda.netty.loadbalancer.core.loadbalance.LoadBalanceUtil;
import lambda.netty.loadbalancer.core.loadbalance.StateImplJsonHelp;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.InstanceStates;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.State;
import lambda.netty.loadbalancer.core.proxy.DecoderException;
import lambda.netty.loadbalancer.core.proxy.ProxyEvent;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

public class SysServiceHostResolveHandler extends ChannelInboundHandlerAdapter {
    final static Logger logger = Logger.getLogger(SysServiceHostResolveHandler.class);
    private final static String HOST = "Host";
    private static final String SYS_HOST = Launcher.getStringValues(ConfigConstants.SYS_SERVICE_CONNECTIONS_CONNECTION_HOST).get(0);
    private static final int SYS_PORT = Launcher.getIntValues(ConfigConstants.SYS_SERVICE_CONNECTIONS_CONNECTION_PORT).get(0);
    private static final String SYS_PATH=Launcher.getStringValue(ConfigConstants.SYS_SERVICE_CONNECTIONS_PATH);
    private static final String SYS_PROTOCOL = Launcher.getStringValue(ConfigConstants.SYS_SERVICE_CONNECTIONS_PROTOCOL);
    Channel remoteHostChannel = null;
    EventLoopGroup remoteHostEventLoopGroup;

    public SysServiceHostResolveHandler(EventLoopGroup remoteHostEventLoopGroup) {
        this.remoteHostEventLoopGroup = remoteHostEventLoopGroup;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel mainChannel = ctx.channel();
        Bootstrap b = new Bootstrap();
        b.group(remoteHostEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new SysServiceHandlersInit(ctx));

        b.connect(SYS_HOST, SYS_PORT).addListeners(new CustomListener(mainChannel));
        super.channelActive(ctx);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {


    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String instanceID= request.headers().get("domain");

            EtcdUtil.getValue(instanceID).thenAccept(x -> {

                String val = String.valueOf(x.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8));
                State stateImpl = StateImplJsonHelp.getObject(val);

                if (stateImpl.getState() == InstanceStates.DOWN) {
                    logger.info("No instance is up ! informing Sys-service ");
                    requestIp();
                } else if (stateImpl.getState() == InstanceStates.RUNNING) {
                    logger.info("These instances are up and running");
                   String remoteIp= LoadBalanceUtil.getRemoteHost(stateImpl);
                    try {
                        EtcdUtil.putValue("localhost", StateImplJsonHelp.toString(stateImpl));
                    } catch (EtcdClientException e) {
                        logger.error("Cannot connect to ETCD !", e);
                    }
                    // redirect the request
                    ProxyEvent proxyEvent = new ProxyEvent(remoteIp);
                    ctx.fireUserEventTriggered(proxyEvent);
                }
            });
        } else {
            logger.error("Decoder doesn't work. Not a FullHttpRequest Object !");
            throw new DecoderException();
        }
        ctx.fireChannelRead(msg);
    }

    private void requestIp() {
        // Prepare the HTTP request.
        HttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, getURI());
        request.headers().set(HttpHeaderNames.HOST, SYS_HOST);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        // Send the HTTP request.
        remoteHostChannel.writeAndFlush(request);
        // Wait for the server to close the connection.
        try {
            remoteHostChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Couldn't close the connection with the Sys-service !", e);
        }
        logger.info("Request sent to the System Service");
    }
    private String getURI(){

        String url=null;
        try {
            URI uri=new URI(SYS_PROTOCOL,null,SYS_HOST,SYS_PORT,SYS_PATH,null,null);
           url=uri.toURL().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    private final class CustomListener implements ChannelFutureListener {
        private Channel mainChannel;

        CustomListener(Channel mainChannel) {
            this.mainChannel = mainChannel;
        }

        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                logger.info("connected to the System service: " +SYS_HOST+":"+SYS_PORT+SYS_PATH);
                remoteHostChannel = channelFuture.channel();
                //Reading the main channel after Sys service is connected
                mainChannel.read();
            } else {
                logger.error("Cannot connect to the System Service !");
            }
        }
    }
}
