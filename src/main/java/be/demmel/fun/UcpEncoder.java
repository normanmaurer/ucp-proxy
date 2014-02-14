package be.demmel.fun;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import be.demmel.protocol.ucp.UCPPacket;
import be.demmel.protocol.ucp.serialization.UCPPacketSerializerImpl;

@Sharable
public class UcpEncoder extends MessageToByteEncoder<UCPPacket> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UcpEncoder.class);
	private final UCPPacketSerializerImpl ucpPacketSerializer;
	
	public UcpEncoder(UCPPacketSerializerImpl ucpPacketSerializer) {
		this.ucpPacketSerializer = ucpPacketSerializer;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, UCPPacket ucpPacket, ByteBuf out) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
		LOGGER.debug("{}", ucpPacket);
		out.writeBytes(ucpPacketSerializer.serialize(ucpPacket));
	}
}
