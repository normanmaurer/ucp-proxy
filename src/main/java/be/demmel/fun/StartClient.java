package be.demmel.fun;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class StartClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(StartClient.class);
	private static final SocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 7201);// proxy
	//private static final SocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 7900);// server
	private static final EventExecutorGroup handlersExecutor = new DefaultEventExecutorGroup(10); // packet handling should happen outside of the pool that
																									// handles IOs
	private static final int AMOUNT_OF_CLIENTS = 3500, AMOUNT_OF_MESSAGES_PER_CLIENT = 500;

	public static void main(String... args) {
		Thread.currentThread().setName("Startup");
		long startTime = System.currentTimeMillis();
		try {
			LOGGER.info("The \"UCP Client\" is starting");

			final CountDownLatch remainingClientsCount = new CountDownLatch(AMOUNT_OF_CLIENTS);

			EventLoopGroup group = null;
			try {
				group = new NioEventLoopGroup();
				Bootstrap b = new Bootstrap().group(group).channel(NioSocketChannel.class);
				b.handler(new CommonUcpChannelInitializer(30000 /* ms */) {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						super.initChannel(ch);
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(handlersExecutor, "ucpPduHandler", new ClientUcpHandler(AMOUNT_OF_MESSAGES_PER_CLIENT));
					}
				});

				for (int i = 0; i < AMOUNT_OF_CLIENTS; i++) {

					b.connect(serverAddress).addListener(new ChannelFutureListener() {

						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							MDC.put("channel", String.format("[id: 0x%08x]",future.channel().hashCode()));
							LOGGER.info("UCP client connected");
							future.channel().closeFuture().addListener(new ChannelFutureListener() {

								@Override
								public void operationComplete(ChannelFuture future) throws Exception {
									MDC.put("channel", String.format("[id: 0x%08x]",future.channel().hashCode()));
									LOGGER.info("UCP client disconnected");
									remainingClientsCount.countDown();
								}
							});
						}
					});
				}

				LOGGER.debug("Waiting for all clients to finish");
				remainingClientsCount.await();

				LOGGER.warn("Number of clients that finished successfully: {}", ClientUcpHandler.getNumberOfResultsReceived());
			} finally {
				LOGGER.debug("Shutting down thread pool");
				if (group != null) {
					group.shutdownGracefully();
				}
				handlersExecutor.shutdownGracefully();
			}
			LOGGER.warn("Processing took {}ms", System.currentTimeMillis() - startTime);
		} catch (Throwable throwable) {
			LOGGER.error("Initializing the \"UCP Client\" failed because: ", throwable);
		}
	}
}
