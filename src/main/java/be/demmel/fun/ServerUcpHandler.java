package be.demmel.fun;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import be.demmel.protocol.ucp.O_6x_AbstractDataType;
import be.demmel.protocol.ucp.PositiveResponse;
import be.demmel.protocol.ucp.UCPOperationHeader;
import be.demmel.protocol.ucp.UCPOperationType;
import be.demmel.protocol.ucp.UCPPacket;

public class ServerUcpHandler extends SimpleChannelInboundHandler<UCPPacket> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerUcpHandler.class);
	private int amountOfPDUsSentBack;
	private String clientId;
	private int currentMessage;
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
    	if(evt instanceof IdleStateEvent) {
    		LOGGER.error("Idle connection ({}). PDUs sent back: {}", ctx.channel().remoteAddress(), this.amountOfPDUsSentBack);
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
	protected void channelRead0(ChannelHandlerContext ctx, UCPPacket ucpPacket) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
		
		O_6x_AbstractDataType p60 = (O_6x_AbstractDataType)ucpPacket.getData();
		
		if(clientId == null) {
			this.clientId = p60.getPwd();
		}
		
		if(!p60.getPwd().equals(this.clientId)) {
			LOGGER.error("PWD is {} but needs to be {}", p60.getPwd(), this.clientId);
		}
		
		if(!p60.getNpwd().equals(this.currentMessage++ + "")) {
			LOGGER.error("NPWD is {} but needs to be {}", p60.getNpwd(), this.currentMessage - 1);
		}
				
		UCPOperationHeader header = new UCPOperationHeader(123, UCPOperationHeader.Type.RESULT, UCPOperationType.SESSION_MANAGEMENT);
		PositiveResponse data = new PositiveResponse("sm0123456789987654321");
		UCPPacket ucpResultPacket = new UCPPacket(header, data);
		
		ctx.writeAndFlush(ucpResultPacket);
		this.amountOfPDUsSentBack++;
	}
}
