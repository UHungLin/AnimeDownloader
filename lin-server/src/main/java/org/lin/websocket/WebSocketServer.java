package org.lin.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/13
 */
public class WebSocketServer extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

	private static Channel serverChannel;

	private String ip;
	private Integer port;

	public WebSocketServer(String ip, Integer port) {
		this.ip = ip;
		this.port = port;
	}

	@Override
	public void run() {
		EventLoopGroup boosGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(boosGroup, workGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(new WebSocketChannelInitializer(ip,port));
			serverChannel = bootstrap.bind(new InetSocketAddress(ip, port)).sync().channel();
			serverChannel.closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			boosGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}

	public void closeServer() {
		if (serverChannel != null) {
			LOGGER.info("close webSocketServer");
			serverChannel.close();
			serverChannel = null;
		}
	}

}
