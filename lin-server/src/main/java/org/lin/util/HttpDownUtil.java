package org.lin.util;

import org.apache.http.HttpStatus;
import org.lin.constant.Global;
import org.lin.downloader.DefaultHttpDownloader;
import org.lin.downloader.IHttpDownloader;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/26
 */
public class HttpDownUtil {

	private static final long ONE_KB = 1024;

	private static final long ONE_MB = 1024 * ONE_KB;

	private static final long ONE_CONNECTION_UPPER_LIMIT = 500 * ONE_MB; // 500M

	private static final long TWO_CONNECTION_UPPER_LIMIT = 1024 * ONE_MB; // 1G

	private static final int DEFAULT_MAX_CONNECTION_LIMIT = 3;

	public static String getTaskFilePath(DefaultHttpDownloader httpDownloader) {
		return httpDownloader.getTaskInfo().getFilePath() + File.separator
				+ httpDownloader.getTaskInfo().getName() + "." + httpDownloader.getTaskInfo().getFileType().value();
	}

	public static String getTaskFilePathWithoutSuffix(IHttpDownloader httpDownloader) {
		return httpDownloader.getTaskInfo().getFilePath() + File.separator
				+ httpDownloader.getTaskInfo().getName();
	}

	public static String getTaskFilePathWithMp4Suffix(IHttpDownloader httpDownloader) {
		return httpDownloader.getTaskInfo().getFilePath() + File.separator
				+ httpDownloader.getTaskInfo().getName() + Global.DEFAULT_VIDEO_SUFFIX;
	}

	public static int calcConnectionCount(long totalLength) {
		if (totalLength < ONE_CONNECTION_UPPER_LIMIT) {
			return 1;
		}
		if (totalLength < TWO_CONNECTION_UPPER_LIMIT) {
			return 2;
		}
		return DEFAULT_MAX_CONNECTION_LIMIT;
	}

	public static HttpURLConnection connect(String url, Map<String, String> headers) throws IOException {
		URL url1 = new URL(url);
		HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
		urlConnection.setConnectTimeout(10000);
		urlConnection.setReadTimeout(10000);
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		return urlConnection;
	}

	public static Map<String, String> addRangeForHeader(Map<String, String> headers, long startOffset, long endOffset) {
		// deep copy
		HashMap<String, String> newHeaders = new HashMap<>();
		newHeaders.putAll(headers);
		if (endOffset == -1 || endOffset == 0) {
			newHeaders.put("Range", String.format("bytes=%d-", startOffset));
		} else {
			newHeaders.put("Range", String.format("bytes=%d-%d", startOffset, endOffset));
		}
		return newHeaders;
	}

	public static Map<String, String> addRangeForHeader(Map<String, String> headers, long startOffset) {
		// deep copy
		HashMap<String, String> newHeaders = new HashMap<>();
		newHeaders.putAll(headers);
		newHeaders.put("Range", String.format("bytes=%d-", startOffset));
		return newHeaders;
	}

	public static boolean isAcceptRange(int statusCode, Map<String, String> headers) {
		if (statusCode == HttpStatus.SC_PARTIAL_CONTENT) return true;
		for (Map.Entry<String, String> header : headers.entrySet()) {
			if ("Accept-Ranges".equals(header.getKey())) {
				return "bytes".equals(header.getValue());
			}
		}
		return false;
	}

}
