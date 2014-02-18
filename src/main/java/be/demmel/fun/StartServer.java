package be.demmel.fun;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(StartServer.class);
	private static final SocketAddress localAddress = new InetSocketAddress("127.0.0.1", 7900);
	private static final EventExecutorGroup handlersExecutor = new DefaultEventExecutorGroup(5); // packet handling should happen outside of the pool that handles IOs

	public static void main(String... args) {
		Thread.currentThread().setName("Startup");
		try {
			LOGGER.info("The \"UCP Server\" is starting");
			
			EventLoopGroup bossGroup = new NioEventLoopGroup();
			EventLoopGroup workerGroup = new NioEventLoopGroup();

			try {
				ServerBootstrap b = new ServerBootstrap();
				b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new CommonUcpChannelInitializer(30000 /* ms */) {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						super.initChannel(ch);
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(handlersExecutor, "ucpPduHandler", new ServerUcpHandler());
					}
				});

				ChannelFuture f = b.bind(localAddress).sync(); // Bind and start to accept incoming connections

				LOGGER.info("The \"UCP Server\" started");
				// Wait until the server socket is closed.
				f.channel().closeFuture().sync();
			} catch (InterruptedException ie) {
				LOGGER.error("Server interrupted: ", ie);
			} finally {
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		} catch (Throwable throwable) {
			LOGGER.error("Initializing the \"UCP Server\" failed because: ", throwable);
		}

	}
}
