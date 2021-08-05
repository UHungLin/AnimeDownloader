package org.lin.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/8
 */
public class HttpServer extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

	private int port;

	private List<Object> controllerList;

	private static Channel serverChannel;

	public HttpServer(int port) {
		this.port = port;
		this.controllerList = new ArrayList<>();
	}

	@Override
	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(3);
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			// pool directBuffer
			b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			b.childOption(ChannelOption.TCP_NODELAY, true);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					ch.pipeline().addLast("httpCodec", new HttpServerCodec());
					ch.pipeline().addLast(new HttpObjectAggregator(4194304));
					ch.pipeline().addLast("serverHandle", new HttpServerHandler(controllerList));
				}
			});
			serverChannel = b.bind(port).sync().channel();
			serverChannel.closeFuture().sync();
		} catch (Exception e) {
			LOGGER.error("{}", e.getMessage());
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public HttpServer addController(Object obj) {
		this.controllerList.add(obj);
		return this;
	}

	public void stopServer() {
		if (serverChannel != null) {
			LOGGER.info("close httpServer");
			serverChannel.close();
			serverChannel = null;
		}
	}

}

