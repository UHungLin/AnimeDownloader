package org.lin.downloader;

import org.lin.pojo.entity.TaskInfo;

import java.util.Map;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/3
 */
public class FlvHttpDownloader extends DefaultHttpDownloader {


	private FlvHttpDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		super(headers, taskInfo);
	}

	public static FlvHttpDownloader newInstance(Map<String, String> headers, TaskInfo taskInfo) {
		return new FlvHttpDownloader(headers, taskInfo);
	}

	@Override
	public void onComplete(DownloadRunnable runnable) {
		System.out.println("FlvHttpDownloader onComplete");
		super.onComplete(runnable);
		if (this.complete()) {
		}
	}
}

