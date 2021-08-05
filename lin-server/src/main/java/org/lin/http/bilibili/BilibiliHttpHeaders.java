package org.lin.http.bilibili;

import java.util.HashMap;
import java.util.Map;

public class BilibiliHttpHeaders {

	final static String UA_PC_Chrome = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

	/**
	 * 该Header配置用于FLV视频下载
	 */
	public static Map<String, String> getBiliWwwFLVHeaders(String bvId) {
		HashMap<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Origin", "https://www.bilibili.com");
		headerMap.put("Referer", "https://www.bilibili.com/video/" + bvId);// need addbvId
		headerMap.put("User-Agent", UA_PC_Chrome);
		return headerMap;
	}

	/**
	 * 该Header配置用于M4s视频下载
	 */
	public static Map<String, String> getBilibiliM4sHeaders(String bvId) {
		HashMap<String, String> headerMap = new HashMap<String, String>();
		headerMap.remove("X-Requested-With");
		headerMap.put("Origin", "https://www.bilibili.com");
		headerMap.put("Referer", "https://www.bilibili.com/video/" + bvId);// need addbvId
		headerMap.put("User-Agent", UA_PC_Chrome);
		return headerMap;
	}

	/**
	 * 该Header配置用于M4s视频下载
	 * 指定Range
	 */
	public static Map<String, String> getBilibiliM4sHeaders(String bvId, long range) {
		HashMap<String, String> headerMap = new HashMap<String, String>();
		headerMap.remove("X-Requested-With");
		headerMap.put("Referer", "https://www.bilibili.com/video/" + bvId);// need addbvId
		headerMap.put("User-Agent", UA_PC_Chrome);
		headerMap.put("Range", "bytes=" + range + "-");
		return headerMap;
	}

	/**
	 * 该Header配置用于M4s视频下载
	 * 指定Range范围
	 */
	public static Map<String, String> getBilibiliM4sHeaders(String bvId, long start, long end) {
		HashMap<String, String> headerMap = new HashMap<String, String>();
		headerMap.remove("X-Requested-With");
		headerMap.put("Referer", "https://www.bilibili.com/video/" + bvId);// need addbvId
		headerMap.put("User-Agent", UA_PC_Chrome);
		headerMap.put("Range", String.format("bytes=%d-%d", start, end));
		return headerMap;
	}

	/**
	 * 该Header配置用于通用PC端页面访问
	 */
	public static Map<String, String> getCommonHeaders() {
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("Cache-Control", "max-age=0");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("User-Agent", UA_PC_Chrome);
		return headerMap;
	}

}
