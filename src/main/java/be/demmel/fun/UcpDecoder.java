package be.demmel.fun;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import be.demmel.protocol.ucp.UCPPacket;
import be.demmel.protocol.ucp.serialization.UCPPacketDeserializerImpl;

public class UcpDecoder extends ByteToMessageDecoder {
	private static final Logger LOGGER = LoggerFactory.getLogger(UcpDecoder.class);
	private final UCPPacketDeserializerImpl ucpPacketDeserializer;
	private int amountOfPDUsReceived;

	public UcpDecoder(UCPPacketDeserializerImpl ucpPacketDeserializer) {
		this.ucpPacketDeserializer = ucpPacketDeserializer;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf ucpPacketPdu, List<Object> out) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
		UCPPacket ucpPacket = this.ucpPacketDeserializer.deserialize(ucpPacketPdu);
		LOGGER.debug("{}", ucpPacket);
		this.amountOfPDUsReceived++;
		out.add(ucpPacket);
	}
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
    	if(evt instanceof IdleStateEvent) {
    		LOGGER.error("Idle connection ({}). PDUs received: {}", ctx.channel().remoteAddress(), this.amountOfPDUsReceived);
    	}
    	super.userEventTriggered(ctx, evt);
    }
	
	@Override
	protected void decodeLast(ChannelHandlerContext ctx, ByteBuf ucpPacketPdu, List<Object> out) throws Exception {
		// channel goes inactive, do nothing
	}
	
	@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
		LOGGER.debug("Channel went inactive");
		super.channelInactive(ctx);
	}

	public UCPPacketDeserializerImpl getUcpPacketDeserializer() {
		return ucpPacketDeserializer;
	}
}
