package org.lin.downloader;

import org.apache.http.HttpStatus;
import org.lin.constant.Global;
import org.lin.constant.HttpDownStatus;
import org.lin.core.DelayHandleCore;
import org.lin.pojo.DelayHandleTask;
import org.lin.pojo.entity.ChunkInfo;
import org.lin.pojo.entity.ConnectInfo;
import org.lin.pojo.HttpHeaderWrapper;
import org.lin.pojo.entity.TaskInfo;
import org.lin.exception.DownloaderException;
import org.lin.util.FFmpegUtil;
import org.lin.util.FileUtils;
import org.lin.util.HttpDownUtil;
import org.lin.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/10
 */
public class M4SHttpDownloader extends DefaultHttpDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(M4SHttpDownloader.class);

	private String videoUrl;
	private String audioUrl;
	private String filePath;
	private String videoFilePath;
	private String audioFilePath;
	private long videoTotalSize;
	private long audioTotalSize;

	private M4SHttpDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		super(headers, taskInfo);
		init();
		// bilibili 默认支持分断下载
		isSupportRange = true;
	}

	public static M4SHttpDownloader newInstance(Map<String, String> headers, TaskInfo taskInfo) {
		return new M4SHttpDownloader(headers, taskInfo);
	}

	private void init() {
		String url = taskInfo.getUrl();
		String[] urls = url.split(Global.URL_SEPARATOR);
		videoUrl = urls[0];
		audioUrl = urls[1];
	}

	@Override
	public void run() {

		if (taskInfo.pause() || taskInfo.downloading())
			return;

		updateStatus(HttpDownStatus.DOWNLOADING);
		msg = "";
//		speedMonitor.start(taskInfo.getCurrentOffset());
		calcCallbackMinIntervalBytes();

		try {
			trialConnect();
		} catch (DownloaderException e) {
			onError(e.getMessage());
			return;
		}

		filePath = HttpDownUtil.getTaskFilePathWithMp4Suffix(this);
		if (FileUtils.existsFile(filePath)) {
			// if use the DB to store task's downloading message, rewrite this
			if (!isResume) {
				// rename file
				String newFileName = FileUtils.renameFile(filePath);
				taskInfo.setName(newFileName);
				filePath = HttpDownUtil.getTaskFilePathWithMp4Suffix(this);
			}
			if (isResume && !isSupportRange) {
				FileUtils.deleteFile(filePath); // delete task and re-download
			}
		}
		try {
			if (!isResume) {
				videoFilePath = HttpDownUtil.getTaskFilePathWithoutSuffix(this) + Global.M4S_VIDEO_SUFFIX;
				audioFilePath = HttpDownUtil.getTaskFilePathWithoutSuffix(this) + Global.M4S_AUDIO_SUFFIX;
				FileUtils.deleteFile(videoFilePath);
				FileUtils.deleteFile(audioFilePath);
				FileUtils.createFile(videoFilePath);
				FileUtils.createFile(audioFilePath);
			}
		} catch (IOException e) {
			onError(String.format("create file - [%s] fail", taskInfo.getName()));
			return;
		}

		if (taskInfo.pause()) {
			return;
		}

		try {
			if (!isResume) {
				// pre allocate
				FileUtils.setLength(videoFilePath, videoTotalSize);
				FileUtils.setLength(audioFilePath, audioTotalSize);
			}
		} catch (IOException | DownloaderException e) {
			onError("not enough disk space");
			return;
		}

		final int connectionCount  = calcConnectionCount(taskInfo.getTotalSize());
		taskInfo.setConnectionCount(connectionCount);

		if (taskInfo.pause()) {
			return;
		}

		System.out.println("taskInfo: " + taskInfo);

		isSingleConnection = (connectionCount == 1);
		try {
			if (!isResume) {
				buildChunkInfoList(taskInfo.getTotalSize(), connectionCount);
			}
//			if (isSingleConnection) {
//				handleWithSingleConnection();
//			} else {
//				// else {
//				// get chunkInfoList from DB
//				// }
//				handleWithMultipleConnection(taskInfo.getChunkInfoList());
//			}
			// 默认使用多线程下载 m4s 格式的视频
			handleWithMultipleConnection(taskInfo.getChunkInfoList());
		} catch (DownloaderException e) {
			LOGGER.error("handle connection fail. {}", e.getMessage());
			onError("download fail");
		}

	}

	@Override
	protected void trialConnect() throws DownloaderException {
		if (isResume) return;

		Map<String, String> newHeaders = HttpDownUtil.addRangeForHeader(headers, 0);
		HttpHeaderWrapper wrapper = null;
		do {
			try {
				// don't support head request
				wrapper = HttpUtil.doGetForHeaders(videoUrl, newHeaders);
				if (wrapper.getCode() != HttpStatus.SC_OK && wrapper.getCode() != HttpStatus.SC_PARTIAL_CONTENT) {
					LOGGER.error("trialConnect fail with status code {}", wrapper.getCode());
					String errMsg = "";
					if (wrapper.getCode() == HttpStatus.SC_FORBIDDEN) {
//						errMsg = "url invalid, please delete the task and try again";
						errMsg = "链接失效，请删除该下载任务并重新下载";
					} else {
//						errMsg = "connect fail with status code " + wrapper.getCode();
						errMsg = "网络连接失败，状态码：" + wrapper.getCode();
					}
					throw new DownloaderException(errMsg);
				} else {
					String range = wrapper.getHeaders().get("Content-Range");
					videoTotalSize = Long.parseLong(range.split("/")[1]) - 1;
					// System.out.println("---" + videoTotalSize);
					taskInfo.setTotalSize(videoTotalSize);
					wrapper = HttpUtil.doGetForHeaders(audioUrl, newHeaders);
					if (wrapper.getCode() != HttpStatus.SC_OK && wrapper.getCode() != HttpStatus.SC_PARTIAL_CONTENT) {
						LOGGER.error("trialConnect fail with status code {}", wrapper.getCode());
						throw new DownloaderException("网络连接失败，状态码：" + wrapper.getCode());
					}
					range = wrapper.getHeaders().get("Content-Range");
					audioTotalSize = Long.parseLong(range.split("/")[1]) - 1;
					// System.out.println("---" + audioTotalSize);
					taskInfo.addTotalSize(audioTotalSize);
					break;
				}
			} catch (IOException e) {
				if (isRetry(e)) {
					try {
						onRetry("请求超时进行重试...");
						TimeUnit.SECONDS.sleep(3);
					} catch (InterruptedException interruptedException) {
						LOGGER.error(interruptedException.getMessage());
						throw new DownloaderException(interruptedException.getMessage());
					}
				} else {
					LOGGER.error("trialConnect fail. {}", e.getMessage());
					throw new DownloaderException("网络连接失败");
				}
			}
		} while (true);
	}

	@Override
	protected int calcConnectionCount(long totalLength) {
		return 2; // 一个线程下载 video, 一个线程下载 audio
	}

	@Override
	protected void buildChunkInfoList(long totalLength, int connectionCount) {
		final List<ChunkInfo> chunkInfoList = new ArrayList<>();
		ChunkInfo videoChunkInfo = new ChunkInfo();
		videoChunkInfo.setIndex(0);
		videoChunkInfo.setStartOffset(0);
		videoChunkInfo.setCurrentOffset(0);
		videoChunkInfo.setEndOffset(videoTotalSize);
		chunkInfoList.add(videoChunkInfo);

		ChunkInfo audioChunkInfo = new ChunkInfo();
		audioChunkInfo.setIndex(1);
		audioChunkInfo.setStartOffset(0);
		audioChunkInfo.setCurrentOffset(0);
		audioChunkInfo.setEndOffset(audioTotalSize);
		chunkInfoList.add(audioChunkInfo);

		taskInfo.setChunkInfoList(chunkInfoList);
	}

	@Override
	protected void handleWithMultipleConnection(List<ChunkInfo> chunkInfoList) throws DownloaderException {
		long realCurrentLength = 0;
		final ArrayList<DownloadRunnable> tempDownloadRunnables = new ArrayList<>();

		for (ChunkInfo chunkInfo : chunkInfoList) {
			LOGGER.debug("chunkInfo - {}", chunkInfo);
			realCurrentLength += (chunkInfo.getCurrentOffset() - chunkInfo.getStartOffset());
			if (chunkInfo.getStatus() == HttpDownStatus.COMPLETE.getStatus()) {
				continue;
			}
			final long contentLength = chunkInfo.getEndOffset() - chunkInfo.getCurrentOffset();
			if (contentLength <= 0) {
				chunkInfo.setStatus(HttpDownStatus.COMPLETE.getStatus());
				continue;
			}
		}
		ChunkInfo videoChunkInfo = chunkInfoList.get(0);
		LOGGER.debug("videoChunkInfo = {}", videoChunkInfo);
		if (videoChunkInfo.getStatus() != HttpDownStatus.COMPLETE.getStatus()) {
			Map<String, String> newHeaders = HttpDownUtil.addRangeForHeader(headers,
					videoChunkInfo.getCurrentOffset(), videoChunkInfo.getEndOffset());
			ConnectInfo connectInfo = new ConnectInfo.Builder()
					.url(videoUrl)
					.headers(newHeaders)
					.supportRange(isSupportRange)
					.build();
			DownloadRunnable runnable = new DownloadRunnable.Builder()
					.setPath(videoFilePath)
					.setChunkInfo(videoChunkInfo)
					.setConnectInfo(connectInfo)
					.setHttpDownloader(this)
					.build();
			tempDownloadRunnables.add(runnable);
		}
		ChunkInfo audioChunkInfo = chunkInfoList.get(1);
		LOGGER.debug("audioChunkInfo = {}", audioChunkInfo);
		if (audioChunkInfo.getStatus() != HttpDownStatus.COMPLETE.getStatus()) {
			Map<String, String> newHeaders = HttpDownUtil.addRangeForHeader(headers,
					audioChunkInfo.getCurrentOffset(), audioChunkInfo.getEndOffset());
			ConnectInfo connectInfo = new ConnectInfo.Builder()
					.url(audioUrl)
					.headers(newHeaders)
					.supportRange(isSupportRange)
					.build();
			DownloadRunnable runnable = new DownloadRunnable.Builder()
					.setPath(audioFilePath)
					.setChunkInfo(audioChunkInfo)
					.setConnectInfo(connectInfo)
					.setHttpDownloader(this)
					.build();
			tempDownloadRunnables.add(runnable);
		}
		downloadRunnables = tempDownloadRunnables;
		taskInfo.setCurrentOffset(realCurrentLength);
		speedMonitor.start(taskInfo.getCurrentOffset());

		if (taskInfo.pause()) {
		} else {
			if (downloadRunnables.isEmpty()) {
				updateStatus(HttpDownStatus.COMPLETE);
			} else {
				for (DownloadRunnable runnable : downloadRunnables) {
					executorService.submit(runnable);
				}
			}
		}
	}

	@Override
	public void onComplete(DownloadRunnable runnable) {
		synchronized (this) {
			super.onComplete(runnable);
			if (this.complete()) {
				LOGGER.debug("M4SHttpDownloader onComplete");
				onComplete("下载完成，合并文件中，请等待...");
				boolean result = FFmpegUtil.convert(filePath, videoFilePath, audioFilePath);
				if (result) {
					onComplete("合并成功");
					DelayHandleTask task_1 = DelayHandleTask.delayTask(DelayHandleTask.Type.DELETE_FILE, videoFilePath);
					DelayHandleTask task_2 = DelayHandleTask.delayTask(DelayHandleTask.Type.DELETE_FILE, audioFilePath);
					DelayHandleCore.getInstance().addTask(task_1);
					DelayHandleCore.getInstance().addTask(task_2);
				} else {
					LOGGER.error("m4s video merge fail");
					onError("视频合并失败，请检查当前系统是否有 ffmpeg 环境");
				}
			}
		}
	}
}

