package be.demmel.fun;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import be.demmel.protocol.ucp.O_60_SessionManagement;
import be.demmel.protocol.ucp.UCPOperationHeader;
import be.demmel.protocol.ucp.UCPOperationType;
import be.demmel.protocol.ucp.UCPPacket;

public class ClientUcpHandler extends SimpleChannelInboundHandler<UCPPacket> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientUcpHandler.class);
	private static final AtomicInteger numberOfResultsReceived = new AtomicInteger(); 
	private int amountOfMessages;
	private int amountOfResponsesReceived;
	private final int clientId;
	
	public ClientUcpHandler(int amountOfMessages, int clientId) {
		this.amountOfMessages = amountOfMessages;
		this.clientId = clientId;
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
        	UCPOperationHeader header = new UCPOperationHeader(123, UCPOperationHeader.Type.OPERATION, UCPOperationType.SESSION_MANAGEMENT);
    		O_60_SessionManagement data = new O_60_SessionManagement("toto", '5', '6', '1', this.clientId+"", i+"", "0100", "opid12345678900123456789");
    		UCPPacket ucpPacket = new UCPPacket(header, data);
    		
    		ctx.writeAndFlush(ucpPacket);
        	
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
	protected void channelRead0(ChannelHandlerContext ctx, UCPPacket ucpPacket) throws Exception {
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
