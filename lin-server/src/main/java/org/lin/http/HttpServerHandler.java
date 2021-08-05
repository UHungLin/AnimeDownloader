package org.lin.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.lin.annotation.RequestMapping;
import org.lin.http.common.Result;
import org.lin.util.HttpHandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/8
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerHandler.class);

	private List<Object> controllerList;

	public HttpServerHandler(List<Object> controllerList) {
		this.controllerList = controllerList;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		URI uri = new URI(request.uri());
		FullHttpResponse httpResponse = invoke(uri.getPath(), ctx.channel(), request);
		if (httpResponse != null) {
			httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
			ctx.channel().writeAndFlush(httpResponse);
		}
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) {
		ctx.channel().close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause = cause.getCause() == null ? cause : cause.getCause();
		LOGGER.error("request error " + cause);
		FullHttpResponse httpResponse = HttpHandlerUtil.buildJson(Result.errorResult("未知错误"));
		httpResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		ctx.channel().writeAndFlush(httpResponse);
	}

	public FullHttpResponse invoke(String uri, Channel channel, FullHttpRequest request)
			throws Exception {
		if (controllerList != null) {
			for (Object obj : controllerList) {
				Class<?> clazz = obj.getClass();
				RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
				if (mapping != null) {
					String mappingUri = fixUri(mapping.value()[0]);
					for (Method actionMethod : clazz.getMethods()) {
						RequestMapping subMapping = actionMethod.getAnnotation(RequestMapping.class);
						if (subMapping != null) {
							String subMappingUri = fixUri(subMapping.value()[0]);
							if (uri.equalsIgnoreCase(mappingUri + subMappingUri)) {
								return (FullHttpResponse) actionMethod.invoke(obj, channel, request);
							}
						}
					}
				}
			}
		}
		// return 404
		return HttpHandlerUtil.buildJson(Result.errorResult(404, "not found"));
	}

	private String fixUri(String uri) {
		StringBuilder builder = new StringBuilder(uri);
		if (builder.indexOf("/") != 0) {
			builder.insert(0, "/");
		}
		if (builder.lastIndexOf("/") == builder.length() - 1) {
			builder.delete(builder.length() - 1, builder.length());
		}
		return builder.toString();
	}

}

