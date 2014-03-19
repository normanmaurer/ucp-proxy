package be.demmel.fun;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateEvent;

import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Sharable
public class FrontendHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(FrontendHandler.class);
	private final SocketAddress remoteAddress;
	private boolean authenticated;
	private Channel outboundChannel;
	private int amountOfPDUsForwarded;
	private int amountOfPDUsForwardedSuccess;

	public FrontendHandler(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		MDC.put("channel", String.format("[id: 0x%08x]", ctx.channel().hashCode()));
		if (evt instanceof IdleStateEvent) {
			LOGGER.error("Idle connection ({}), closing it. PDUs forwarded: {}. Success: {}", ctx.channel().remoteAddress(), this.amountOfPDUsForwarded,
					this.amountOfPDUsForwardedSuccess);
			//ctx.close();//TODO: re-enable
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]", ctx.channel().hashCode()));
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]", ctx.channel().hashCode()));
		LOGGER.error("Error:", cause);
		closeOnFlush(ctx.channel());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]", ctx.channel().hashCode()));
		ctx.channel().read();// read the first message to trigger "channelRead0(...)"
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object ucpPacket) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]", ctx.channel().hashCode()));

		if (!this.authenticated) {// authenticate the client
			this.authenticate(ctx, ucpPacket);
		} else {// forward the packet
			this.forwardPacket(ctx, ucpPacket);
		}
		this.amountOfPDUsForwarded++;
	}

	private void forwardPacket(final ChannelHandlerContext ctx, Object ucpPacket) {
		if (outboundChannel.isActive()) {
			outboundChannel.writeAndFlush(ucpPacket).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {// forwarding the packet succeeded so read the next one
						amountOfPDUsForwardedSuccess++;
						ctx.channel().read();
					} else {
						future.channel().close();
					}
				}
			});
		} else {// this should never ever happen
			assert false : "OutboundChannel inactive, cannot forward PDU";
		}
	}

	private void authenticate(final ChannelHandlerContext ctx, final Object ucpPacket) {
		this.authenticated = true;// authentication against a database (= blocking) happens here

		// connect to the gateway
		final Channel inboundChannel = ctx.channel();
		Bootstrap b = new Bootstrap();
		b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
		// TODO: remove timeout as it's only done to count and print the amount of responses received
				.handler(new CommonUcpChannelInitializer(30000 /* ms */) {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// no need for an inactivity check on this side of the proxy
						super.initChannel(ch);
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast("backendHandler", new BackendHandler(inboundChannel));
					}
				}).option(ChannelOption.AUTO_READ, false);
		ChannelFuture f = b.connect(remoteAddress);
		outboundChannel = f.channel();
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {// forward the packet
					// from now on any amount of UCPPackets may be decoded per read()
					ChannelPipeline cp = inboundChannel.pipeline();
					DelimiterBasedFrameDecoder frameDecoder = (DelimiterBasedFrameDecoder) cp.get("frameDecoder");
					frameDecoder.setSingleDecode(false);
					forwardPacket(ctx, ucpPacket);

					cp.fireChannelRead(Unpooled.EMPTY_BUFFER);
				} else {// close the inbound channel
					LOGGER.error("Forwarding the packet failed, closing it", future.cause());
					inboundChannel.close();
				}
			}
		});
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]", ctx.channel().hashCode()));
		if (outboundChannel != null) {
			closeOnFlush(outboundChannel);
		}
	}

	static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {// flush the pending packets then close the channel
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}
}
