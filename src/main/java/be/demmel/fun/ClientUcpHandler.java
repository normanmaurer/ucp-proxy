package be.demmel.fun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ClientUcpHandler extends SimpleChannelInboundHandler<ByteBuf> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientUcpHandler.class);
	private static final AtomicInteger numberOfResultsReceived = new AtomicInteger(); 
	private int amountOfMessages;
	private int amountOfResponsesReceived;
	
	public ClientUcpHandler(int amountOfMessages) {
		this.amountOfMessages = amountOfMessages;
	}
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
    	if(evt instanceof IdleStateEvent) {
    		LOGGER.error("Idle connection ({}). PDUs received: {}", ctx.channel().remoteAddress(), this.amountOfResponsesReceived);
    	}
    	super.userEventTriggered(ctx, evt);
    }
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    	MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
    	    	
    	for(int i = 0 ; i < amountOfMessages ; i++) {
    		LOGGER.debug("Sending UCP 60 message");
    		ctx.writeAndFlush(Unpooled.copiedBuffer(new byte[]{0x02, 0x31, 0x32, 0x2f, 0x30, 0x30, 0x30, 0x36, 0x37, 0x2f, 0x4f, 0x2f, 0x36, 0x30, 0x2f, 0x74, 0x6f, 0x74, 0x6f, 0x2f, 0x35, 0x2f, 0x36, 0x2f, 0x31, 0x2f, 0x33, 0x31, 0x2f, 0x33, 0x30, 0x2f, 0x30, 0x31, 0x30, 0x30, 0x2f, 0x2f, 0x2f, 0x2f, 0x6f, 0x70, 0x69, 0x64, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x2f, 0x2f, 0x42, 0x35, 0x03}));
        	
        	LOGGER.debug("UCP message sent");
    	}
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
		
		this.amountOfResponsesReceived++;
		
		if(this.amountOfResponsesReceived == this.amountOfMessages) {// each packet has had its response
			LOGGER.info("Each packet sent has had its response, closing channel");
			numberOfResultsReceived.incrementAndGet();
			ctx.close();
		}
	}
	
	public static int getNumberOfResultsReceived() {
		return numberOfResultsReceived.get();
	}
}
