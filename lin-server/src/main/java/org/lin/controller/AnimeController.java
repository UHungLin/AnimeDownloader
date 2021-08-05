package org.lin.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.lin.Application;
import org.lin.annotation.RequestMapping;
import org.lin.constant.HttpDownStatus;
import org.lin.constant.MessageType;
import org.lin.core.MessageCore;
import org.lin.downloader.DownloaderManager;
import org.lin.http.HttpServer;
import org.lin.pojo.entity.TaskInfo;
import org.lin.pojo.entity.VideoInfo;
import org.lin.http.common.Result;
import org.lin.http.view.VideoView;
import org.lin.parser.ParserManager;
import org.lin.util.HttpHandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/4
 */
@RequestMapping("/lin/anime")
public class AnimeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnimeController.class);

	@RequestMapping("/parse")
	public FullHttpResponse parse(Channel channel, FullHttpRequest request) throws IOException {
		Map<String, String> map = getJSONParams(request);
		LOGGER.debug("request params = {}", map);
		try {
			HashMap<String, String> headers = getJSONParams(map.get("headers"), new TypeReference<HashMap<String, String>>() {});
			VideoView videoInfo = ParserManager.getInstance().parse((String) map.get("search"), headers);
			if (videoInfo == null) {
				return HttpHandlerUtil.buildJson(Result.errorResult("解析失败"));
			} else {
				return HttpHandlerUtil.buildJson(Result.successResult(videoInfo));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return HttpHandlerUtil.buildJson(Result.errorResult("解析失败"));
		}
	}

	@RequestMapping("/download")
	public FullHttpResponse download(Channel channel, FullHttpRequest request) throws IOException {
		Map<String, String> map = getJSONParams(request);
		HashMap<String, String> headers = getJSONParams(map.get("headers"), new TypeReference<HashMap<String, String>>() {});
		List<VideoInfo> list = getJSONParams(map.get("videoInfoList"), new TypeReference<List<VideoInfo>>() {});

		LOGGER.debug("request params = {}", map);

		List<TaskInfo> taskInfoList = new ArrayList<>();
		for (VideoInfo videoInfo : list) {
			TaskInfo taskInfo = null;
			try {
				taskInfo = ParserManager.getInstance().buildTaskInfo(headers, videoInfo);
				taskInfoList.add(taskInfo);
			} catch (Exception e) {
				if (taskInfo == null) {
					taskInfo = new TaskInfo();
					taskInfo.setId(videoInfo.getId());
				}
				MessageCore.send(MessageType.CALL_BACK, HttpDownStatus.FAIL, e.getMessage(), taskInfo);
			}
		}
		for (TaskInfo taskInfo : taskInfoList) {
			try {
				DownloaderManager.getInstance().start(taskInfo, headers);
			} catch (Exception e) {
				MessageCore.send(MessageType.CALL_BACK, HttpDownStatus.FAIL, e.getMessage(), taskInfo);
			}
		}
		return HttpHandlerUtil.buildJson(Result.successResult());
	}

	@RequestMapping("/cancel")
	public FullHttpResponse cancel(Channel channel, FullHttpRequest request) {
		try {
			Map<String, String> map = getJSONParams(request);
			String taskId = map.get("id");
			DownloaderManager.getInstance().cancel(taskId);
		} catch (IOException e) {
		}
		return HttpHandlerUtil.buildJson(Result.successResult());
	}

	@RequestMapping("/pause")
	public FullHttpResponse pause(Channel channel, FullHttpRequest request) {
		try {
			Map<String, String> map = getJSONParams(request);
			String taskId = map.get("id");
			DownloaderManager.getInstance().pause(taskId);
		} catch (IOException e) {
		}
		return HttpHandlerUtil.buildJson(Result.successResult());
	}

	@RequestMapping("/resume")
	public FullHttpResponse resume(Channel channel, FullHttpRequest request) {
		try {
			Map<String, String> map = getJSONParams(request);
			String taskId = map.get("id");
			DownloaderManager.getInstance().resume(taskId);
		} catch (IOException e) {
		}
		return HttpHandlerUtil.buildJson(Result.successResult());
	}

	@RequestMapping("/shutdown")
	public FullHttpResponse shutdown(Channel channel, FullHttpRequest request) {
		try {
			System.out.println("shutdown...");
			Application.application().stopServer();
		} catch (Exception e) {
		}
		return HttpHandlerUtil.buildJson(Result.successResult());
	}

	private Map<String, String> getJSONParams(FullHttpRequest request) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(request.content().toString(Charset.forName("UTF-8")), Map.class);
	}

	private <T> T getJSONParams(String jsonStr, Class<T> clazz) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(jsonStr, clazz);
	}

	private <T> T getJSONParams(Object obj, TypeReference<T> clazzList) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(objectMapper.writeValueAsString(obj), clazzList);
	}

	private Map<String, Object> parseParams(FullHttpRequest request) throws IOException {
		HttpMethod method = request.method();

		Map<String, Object> parmMap = new HashMap<>();

		if (HttpMethod.GET == method) {
			QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
			decoder.parameters().entrySet().forEach( entry -> {
				parmMap.put(entry.getKey(), entry.getValue().get(0));
			});
		} else if (HttpMethod.POST == method) {
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
			decoder.offer(request);

			List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

			for (InterfaceHttpData parm : parmList) {
				Attribute data = (Attribute) parm;
				parmMap.put(data.getName(), data.getValue());
			}
		} else {
			throw new IOException("only accept method get/post");
		}
		return parmMap;
	}

}
