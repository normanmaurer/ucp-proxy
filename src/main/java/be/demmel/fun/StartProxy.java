package be.demmel.fun;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StartProxy {
	private static final Logger LOGGER = LoggerFactory.getLogger(StartProxy.class);
	private static final SocketAddress localAddress = new InetSocketAddress("127.0.0.1", 7201);
	private static final EventExecutorGroup handlersExecutor = new DefaultEventExecutorGroup(10); // packet handling should happen outside of the pool that handles IO

	public static void main(String... args) {
		Thread.currentThread().setName("Startup");
		try {
            final InetSocketAddress outboundAddress = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 7900);
			
			EventLoopGroup bossGroup = new NioEventLoopGroup();
			EventLoopGroup workerGroup = new NioEventLoopGroup();

			try {
				ServerBootstrap b = new ServerBootstrap();
				b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new CommonUcpChannelInitializer(CommonStaticConfig.PACKET_SERIALIZER,
						CommonStaticConfig.PACKET_DESERIALIZER, 60000 /* ms */) {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {						
						//TODO: connect timeout when connecting to backend
						super.initChannel(ch);
						ChannelPipeline pipeline = ch.pipeline();
						// make sure that only 1 UCPPacket gets decoded the for the first read()
						DelimiterBasedFrameDecoder frameDecoder = (DelimiterBasedFrameDecoder) pipeline.get("frameDecoder");
						frameDecoder.setSingleDecode(true);// will be set back to false once the client is authenticated and the connection to the remote server has been established
						pipeline.addLast(handlersExecutor, "authenticationHandler", new FrontendHandler(outboundAddress));
						//FIXME: using a separate executorservice makes the proxy "lose" some packets!
					}
				}).childOption(ChannelOption.AUTO_READ, false);

				ChannelFuture f = b.bind(localAddress).sync(); // bind and start to accept incoming connections
				LOGGER.info("The \"UCP Proxy\" started");
				f.channel().closeFuture().sync();// wait until the server socket is closed.
			} catch (InterruptedException ie) {
				LOGGER.error("Server interrupted: ", ie);
			} finally {
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		} catch (Throwable throwable) {
			LOGGER.error("Initializing the \"UCP Proxy\" failed because: ", throwable);
		}
	}
}
