package org.lin.downloader;

import org.apache.commons.lang3.StringUtils;
import org.lin.exception.DownloaderException;
import org.lin.pojo.entity.TaskInfo;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/29
 */
public class AcFunM3U8HttpDownloader extends M3U8HttpDownloader {

	private AcFunM3U8HttpDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		super(headers, taskInfo);
		isSupportRange = false;
		isSingleConnection = false;
	}

	public static M3U8HttpDownloader newInstance(Map<String, String> headers, TaskInfo taskInfo) {
		return new AcFunM3U8HttpDownloader(headers, taskInfo);
	}

	@Override
	protected void buildChunkInfoList(long totalLength, int connectionCount) throws DownloaderException {
		super.buildChunkInfoList(totalLength, connectionCount);
		String host = taskInfo.getUrl().split("hls/")[0] + "hls/";
		tsUrlList = tsUrlList.stream().filter(StringUtils::isNotBlank).map(host::concat).collect(Collectors.toList());
	}

}
