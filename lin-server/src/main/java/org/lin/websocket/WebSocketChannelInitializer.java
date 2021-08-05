package org.lin.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/13
 */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

	private String ip;

	private Integer port;


	public WebSocketChannelInitializer(String ip,Integer port){
		this.ip=ip;
		this.port=port;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast(new HttpServerCodec());

		pipeline.addLast(new HttpObjectAggregator(63356));

		pipeline.addLast(new WebSocketServerCompressionHandler());

//		pipeline.addLast(new ChunkedWriteHandler());

//		pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

		pipeline.addLast(new WebSocketServerHandler(ip,port));
	}
}
