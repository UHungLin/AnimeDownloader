package org.lin.downloader;

import org.lin.pojo.entity.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/25
 */
public class MP4HttpDownloader extends DefaultHttpDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MP4HttpDownloader.class);

	private MP4HttpDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		super(headers, taskInfo);
	}

	public static MP4HttpDownloader newInstance(Map<String, String> headers, TaskInfo taskInfo) {
		return new MP4HttpDownloader(headers, taskInfo);
	}

}

