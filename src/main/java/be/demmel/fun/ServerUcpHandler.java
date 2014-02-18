package be.demmel.fun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ServerUcpHandler extends SimpleChannelInboundHandler<ByteBuf> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerUcpHandler.class);
	private int amountOfPDUsSentBack;
	private String clientId;
	private int currentMessage;
	private int amountOfPDUsReceived;
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
    	if(evt instanceof IdleStateEvent) {
    		LOGGER.error("Idle connection ({}). PDUs received: {}. PDUs sent back: {}", ctx.channel().remoteAddress(), this.amountOfPDUsReceived, this.amountOfPDUsSentBack);
    	}
    	super.userEventTriggered(ctx, evt);
    }
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
		LOGGER.error("Error:", cause);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf ucpPacket) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
		this.amountOfPDUsReceived++;
		
		ctx.writeAndFlush(Unpooled.copiedBuffer(new byte[]{0x02, 0x31, 0x32, 0x2f, 0x30, 0x30, 0x30, 0x34, 0x30, 0x2f, 0x52, 0x2f, 0x36, 0x30, 0x2f, 0x41, 0x2f, 0x73, 0x6d, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x39, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x2f, 0x33, 0x34, 0x03}));
		this.amountOfPDUsSentBack++;
	}
}
