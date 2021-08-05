package org.lin.websocket;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.lin.pojo.Message;
import org.lin.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Lin =￣ω￣=
 * @date 2021/7/13
 */
public final class ClientCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientCallback.class);

	public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	public static void push(Message message) {
		if (group.isEmpty()) {
			LOGGER.warn("no websocket client connect...");
			return;
		}
		String json = JsonUtil.objToJson(message);
		group.writeAndFlush(new TextWebSocketFrame(json));
	}

}
