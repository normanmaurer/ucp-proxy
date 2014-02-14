package be.demmel.fun;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class BackendHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(BackendHandler.class);

    private final Channel inboundChannel;
    private int amountOfPDUsForwarded;
	private int amountOfPDUsForwardedSuccess;

    public BackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
    	if(evt instanceof IdleStateEvent) {
    		LOGGER.error("Idle connection ({}). PDUs forwarded: {}. Success: {}", ctx.channel().remoteAddress(), this.amountOfPDUsForwarded, this.amountOfPDUsForwardedSuccess);
    	}
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    	MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
    	
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                	ctx.channel().read();//FIXME: same as frontend: multiple PDUs could be read in a single read.
                    amountOfPDUsForwardedSuccess++;
                } else {
                    future.channel().close();
                }
            }
        });
        this.amountOfPDUsForwarded++;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
    	
        FrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	MDC.put("channel", String.format("[id: 0x%08x]",ctx.channel().hashCode()));
        cause.printStackTrace();
        FrontendHandler.closeOnFlush(ctx.channel());
    }
}
