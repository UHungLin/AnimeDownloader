package org.lin.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.lin.core.MessageCore;

import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/13
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

	private static final String WEBSOCKET_PATH = "/websocket";

	private String ip;

	private Integer port;

	private WebSocketServerHandshaker handshaker;

	public WebSocketServerHandler(String ip,Integer port){
		this.ip=ip;
		this.port=port;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, ((FullHttpRequest) msg));
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("客户端与服务端连接开启....");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ClientCallback.group.remove(ctx.channel());
		System.out.println("客户端与服务器链接关闭！");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {

		if (!req.decoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		if (!ClientCallback.group.isEmpty()) {
			// 确保只有一个客户端与服务端连接
			for (Channel channel : ClientCallback.group) {
				channel.close();
			}
			ClientCallback.group.clear();
		}
		ClientCallback.group.add(ctx.channel());
		System.out.println("ClientCallback.group = " + ClientCallback.group.size());

		WebSocketServerHandshakerFactory wsFactory
				= new WebSocketServerHandshakerFactory("ws://" + ip + ":" + port + WEBSOCKET_PATH, null, true);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
		if(msg instanceof TextWebSocketFrame){
			String request = ((TextWebSocketFrame) msg).text();
			System.out.println("request = " + request);
			if (Objects.equals(request, "2333")) { // 心跳
				ClientCallback.group.writeAndFlush(new TextWebSocketFrame(request));
				return;
			}
			MessageCore.deleteMessage(request);
		}
		else if (msg instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(msg.content().retain()));
		}
		else if (msg instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg.retain());
		}
	}

	private void sendHttpResponse(ChannelHandlerContext ctx,
	                              FullHttpRequest req, DefaultFullHttpResponse res) {
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
					CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
		}

		ChannelFuture future = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.status().code() != 200) {
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}

	private static boolean isKeepAlive(FullHttpRequest req) {
		return false;
	}

}

