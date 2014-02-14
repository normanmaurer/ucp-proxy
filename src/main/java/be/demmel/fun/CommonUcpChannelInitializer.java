package be.demmel.fun;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import be.demmel.protocol.ucp.serialization.UCPPacketDeserializerImpl;
import be.demmel.protocol.ucp.serialization.UCPPacketSerializerImpl;

public class CommonUcpChannelInitializer extends ChannelInitializer<SocketChannel> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUcpChannelInitializer.class);
	private final UCPPacketDeserializerImpl ucpPacketDeserializer;
	private final UCPPacketSerializerImpl ucpPacketSerializer;
	private final int idleMillesecondsBeforeChannelClose;
	
	public CommonUcpChannelInitializer(UCPPacketSerializerImpl ucpPacketSerializer, UCPPacketDeserializerImpl ucpPacketDeserializer) {
		this(ucpPacketSerializer, ucpPacketDeserializer, 0);
	}
	
	public CommonUcpChannelInitializer(UCPPacketSerializerImpl ucpPacketSerializer, UCPPacketDeserializerImpl ucpPacketDeserializer, int idleMillesecondsBeforeChannelClose) {
		this.ucpPacketDeserializer = ucpPacketDeserializer;
		this.ucpPacketSerializer = ucpPacketSerializer;
		this.idleMillesecondsBeforeChannelClose = idleMillesecondsBeforeChannelClose;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		MDC.put("channel", String.format("[id: 0x%08x]",ch.hashCode()));
		LOGGER.debug("New channel: {}", ch);
		ChannelPipeline pipeline = ch.pipeline();
		if(this.idleMillesecondsBeforeChannelClose != 0) {
			pipeline.addLast(new IdleStateHandler(0, 0, idleMillesecondsBeforeChannelClose, TimeUnit.MILLISECONDS));
		}
		
		pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(2048, false, true /* to avoid reading an infinite amount of bytes (when no delimiter was sent)*/, Unpooled.wrappedBuffer(new byte[]{0x03})));
		pipeline.addLast("ucpPduDecoder", new UcpDecoder(ucpPacketDeserializer));
		pipeline.addLast("packetEncoder", new UcpEncoder(ucpPacketSerializer));
	}
}
