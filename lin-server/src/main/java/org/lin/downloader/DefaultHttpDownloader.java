package org.lin.downloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.lin.constant.FileFormatType;
import org.lin.constant.HttpDownStatus;
import org.lin.constant.MessageType;
import org.lin.core.MessageCore;
import org.lin.exception.DownloaderException;
import org.lin.pojo.HttpHeaderWrapper;
import org.lin.pojo.entity.ChunkInfo;
import org.lin.pojo.entity.ConnectInfo;
import org.lin.pojo.entity.TaskInfo;
import org.lin.util.FileUtils;
import org.lin.util.HttpDownUtil;
import org.lin.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/25
 */
public abstract class DefaultHttpDownloader implements IHttpDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpDownloader.class);

	protected Map<String, String> headers;
	protected TaskInfo taskInfo;
	/** 是否是恢复下载 */
	protected volatile boolean isResume;
	/** 是否支持 http 分断下载 */
	protected boolean isSupportRange;
	protected boolean isSingleConnection;
	private DownloadRunnable singleDownloadRunnable;
	protected ArrayList<DownloadRunnable> downloadRunnables;
	protected ExecutorService executorService;
	protected DownloadSpeedMonitor speedMonitor;
	private int retryTimes;
	/** 下载的文件格式(eg. mp4 m4s m3u8...) */
	protected FileFormatType fileType;
	/** 给客户端的提示消息 */
	protected String msg;

	protected long callbackMinIntervalBytes;

	/** 回调最小间隔: 1s */
	protected static final int CALLBACK_MIN_INTERVAL_MILLIS = 1000; // ms
	/** 回调最小字节数: 1kb */
	protected static final int CALLBACK_MIN_INTERVAL_BYTES = 1024; // byte
	protected static final int NO_ANY_PROGRESS_CALLBACK = -1;
	protected static final int CALLBACK_DEFAULT_PROGRESS_MAX_COUNT = 100;

	private volatile long lastCallbackTimestamp = 0;
	private volatile boolean isFirstCallbackProgressToUser = true;
	private final AtomicLong callbackIncreaseBuffer = new AtomicLong();
	private final AtomicBoolean needCallbackProgressToUser = new AtomicBoolean(false);


	public DefaultHttpDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		this.headers = headers;
		this.taskInfo = taskInfo;

		downloadRunnables = new ArrayList<>();
		executorService = DownloaderManager.getInstance().getExecutorService();
		isResume = false;
		isSupportRange = false;
		retryTimes = 3;
		speedMonitor = new DownloadSpeedMonitor();
	}

	@Override
	public String getId() {
		return taskInfo.getId();
	}

	@Override
	public void run() {

		if (taskInfo.pause() || taskInfo.downloading())
			return;

		updateStatus(HttpDownStatus.DOWNLOADING);
//		speedMonitor.start(taskInfo.getCurrentOffset());

		try {
			trialConnect();
		} catch (DownloaderException e) {
			onError(e.getMessage());
			return;
		}

		calcCallbackMinIntervalBytes();

		String filePath = HttpDownUtil.getTaskFilePath(this);
		if (FileUtils.existsFile(filePath)) {
			// if use the DB to store task's downloading message, rewrite steps
			if (!isResume) {
				// rename file
				String newFileName = FileUtils.renameFile(filePath);
				taskInfo.setName(newFileName);
				filePath = HttpDownUtil.getTaskFilePath(this);
			}
			if (isResume && !isSupportRange) {
				FileUtils.deleteFile(filePath); // delete task and re-download
			}
		} else {
			try {
				FileUtils.createFile(filePath);
			} catch (IOException e) {
				onError(String.format("create file - [%s] fail", taskInfo.getName()));
				return;
			}
		}

		if (taskInfo.pause()) {
			return;
		}

		if (!isResume) {
			try {
				// pre allocate
				if (taskInfo.getTotalSize() > 0)
					FileUtils.setLength(filePath, taskInfo.getTotalSize());
			} catch (IOException | DownloaderException e) {
				onError("not enough disk space");
				return;
			}
		}

		final int connectionCount  = calcConnectionCount(taskInfo.getTotalSize());
		taskInfo.setConnectionCount(connectionCount);

		if (taskInfo.pause()) {
			return;
		}

		LOGGER.info("taskInfo = {}", taskInfo);

		isSingleConnection = (connectionCount == 1);
		try {
			if (!isResume) {
				buildChunkInfoList(taskInfo.getTotalSize(), connectionCount);
			}
			if (isSingleConnection) {
				handleWithSingleConnection();
			} else {
				// else {
					// get chunkInfoList from DB
				// }
				handleWithMultipleConnection(taskInfo.getChunkInfoList());
			}
		} catch (DownloaderException e) {
			LOGGER.error("handle connection fail. {}", e.getMessage());
			onError("download fail");
		}

	}

	protected void trialConnect() throws DownloaderException {

		if (isResume) return;

		Map<String, String> newHeaders = HttpDownUtil.addRangeForHeader(headers, 0, 1);
		HttpHeaderWrapper wrapper = null;
		do {
			try {
				// 有些网站不支持 head 请求
//				wrapper = HttpUtil.doHead(taskInfo.getUrl(), newHeaders);
				wrapper = HttpUtil.doGetForHeaders(taskInfo.getUrl(), newHeaders);
				if (wrapper.getCode() != HttpStatus.SC_OK && wrapper.getCode() != HttpStatus.SC_PARTIAL_CONTENT) {
					LOGGER.error("trialConnect fail with status code {}", wrapper.getCode());
					throw new DownloaderException("connect fail with status code " + wrapper.getCode());
				} else {
					isSupportRange = HttpDownUtil.isAcceptRange(wrapper.getCode(), wrapper.getHeaders());
					String length = StringUtils.isBlank(wrapper.getHeaders().get("Content-Range")) ? null :
									wrapper.getHeaders().get("Content-Range").split("/")[1];
					long totalLength = StringUtils.isBlank(length) ? -1 : Long.parseLong(length);
					taskInfo.setTotalSize(totalLength);
					LOGGER.debug("totalLength = {}", totalLength);
					break;
				}
			} catch (IOException e) {
				if (isRetry(e)) {
					try {
						LOGGER.warn("request timeout, retry again...");
						onRetry("请求超时，尝试重新连接...");
						TimeUnit.SECONDS.sleep(3);
					} catch (InterruptedException interruptedException) {
						LOGGER.warn(interruptedException.getMessage());
						throw new DownloaderException(interruptedException.getMessage());
					}
				} else {
					LOGGER.error("trialConnect fail. {}", e.getMessage());
					throw new DownloaderException("connect fail");
				}
			}
		} while (true);
	}

	@Override
	public void updateStatus(HttpDownStatus status) {
		taskInfo.setStatus(status.getStatus());
		if (status != HttpDownStatus.DOWNLOADING) {
			speedMonitor.reset();
			taskInfo.setSpeed(speedMonitor.getSpeed());
		}
		if (status == HttpDownStatus.COMPLETE) {
			DownloaderManager.getInstance().complete(taskInfo.getId());
		}
		MessageCore.send(status, msg, taskInfo);
		DownloaderManager.getInstance().refresh();
	}

	@Override
	public void start() {
		if (downloading() || complete()) return;
		updateStatus(HttpDownStatus.WAITING);
	}

	public void onError(String msg) {
		this.msg = msg;
		System.out.println(msg);
		updateStatus(HttpDownStatus.FAIL);
//		callback.onError(this, msg);
		if (downloadRunnables.isEmpty()) return;
		@SuppressWarnings("unchecked")
		ArrayList<DownloadRunnable> pauseList = (ArrayList<DownloadRunnable>) downloadRunnables.clone();
		for (DownloadRunnable runnable : pauseList) {
			if (runnable != null) { // if runnable is null, it must be completed and removed.
				runnable.pause();
			}
		}
	}

	public void onProcess(long increaseBytes) {
		taskInfo.increaseSize(increaseBytes);
		speedMonitor.update(taskInfo.getCurrentOffset());
		taskInfo.setSpeed(speedMonitor.getSpeed());

		callbackIncreaseBuffer.addAndGet(increaseBytes);
		final long now = System.currentTimeMillis();
		isNeedCallbackToUser(now);
		handleProcess();
	}

	public boolean isRetry(Exception e) {
		if ((e instanceof SocketTimeoutException
				|| e instanceof NoRouteToHostException
				|| e instanceof SocketException)
				&& retryTimes > 0) {
			retryTimes--;
			return true;
		}
		return false;
	}

	public void onRetry(String msg) {
		LOGGER.debug("request retry. msg = {}", msg);
		this.msg = msg;
		updateStatus(HttpDownStatus.RETRY);
	}

	public void onComplete(String msg) {
		this.msg = msg;
		updateStatus(HttpDownStatus.COMPLETE);
	}

	public void onComplete(DownloadRunnable runnable) {
		if (isSingleConnection) {
			if (taskInfo.getCurrentOffset() < taskInfo.getTotalSize()) {
				LOGGER.error("task[{} range=[{}, {})]", taskInfo.getName(),
						runnable.getChunkInfo().getCurrentOffset(),
						runnable.getChunkInfo().getEndOffset());
//				onRetry("complete check fail");
				// 这里不自动重新恢复下载，让客户端用户点击【恢复下载】按钮
				onError("下载出现错误，请单击【resume】按钮恢复下载");
			} else {
				updateStatus(HttpDownStatus.COMPLETE);
			}
		} else {
			LOGGER.debug("download complete...");
			long currentOffset = runnable.getChunkInfo().getCurrentOffset();
			long endOffset = runnable.getChunkInfo().getEndOffset();
			if (currentOffset < endOffset) {
				LOGGER.error("task[{}]'s chunkInfo[index={} range=[{}, {})]", taskInfo.getName(),
						runnable.getChunkInfo().getIndex(), runnable.getChunkInfo().getCurrentOffset(),
						runnable.getChunkInfo().getEndOffset());
//				onRetry("complete check fail");
				onError("下载出现错误，请单击【resume】按钮恢复下载");
			} else {
				downloadRunnables.remove(runnable);
				if (downloadRunnables.isEmpty()) {
					updateStatus(HttpDownStatus.COMPLETE);
				}
			}
		}
	}

	@Override
	public void pause() {
		if (taskInfo.pause() || taskInfo.complete() || taskInfo.error()) return;
		if (singleDownloadRunnable != null) singleDownloadRunnable.pause();
		@SuppressWarnings("unchecked")
		ArrayList<DownloadRunnable> pauseList = (ArrayList<DownloadRunnable>) downloadRunnables.clone();
		for (DownloadRunnable runnable : pauseList) {
			if (runnable != null) {
				runnable.pause();
			}
		}
		updateStatus(HttpDownStatus.PAUSE);
	}

	@Override
	public void resume() {
		if (taskInfo.downloading() || taskInfo.waiting() || taskInfo.complete()) return;
		isResume = true;
		updateStatus(HttpDownStatus.WAITING);
	}

	@Override
	public void cancel() {
		pause();
	}

	@Override
	public boolean downloading() {
		return taskInfo.getStatus() == HttpDownStatus.DOWNLOADING.getStatus();
	}

	@Override
	public boolean retrying() {
		return taskInfo.getStatus() == HttpDownStatus.RETRY.getStatus();
	}

	@Override
	public boolean waiting() {
		return taskInfo.getStatus() == HttpDownStatus.WAITING.getStatus();
	}

	@Override
	public boolean complete() {
		return taskInfo.getStatus() == HttpDownStatus.COMPLETE.getStatus();
	}

	@Override
	public TaskInfo getTaskInfo() {
		return taskInfo;
	}

	public FileFormatType getFileType() {
		return fileType;
	}

	protected void calcCallbackMinIntervalBytes() {
		callbackMinIntervalBytes = calculateCallbackMinIntervalBytes(taskInfo.getTotalSize(), CALLBACK_DEFAULT_PROGRESS_MAX_COUNT);
	}

	protected void handleWithSingleConnection() throws DownloaderException {
		LOGGER.debug("download with single thread...");

		Map<String, String> newHeaders = headers;
		if (isSupportRange) {
			newHeaders = HttpDownUtil.addRangeForHeader(headers, taskInfo.getCurrentOffset(), taskInfo.getTotalSize());
		} else {
			taskInfo.setCurrentOffset(0); // download from start
			taskInfo.getChunkInfoList().get(0).setCurrentOffset(0);
		}
		speedMonitor.start(taskInfo.getCurrentOffset());
		final ConnectInfo connectInfo = new ConnectInfo.Builder()
				.url(taskInfo.getUrl())
				.headers(newHeaders)
				.supportRange(isSupportRange)
				.build();

		LOGGER.debug("connectInfo - {}", connectInfo);
		singleDownloadRunnable = new DownloadRunnable.Builder()
				.setPath(HttpDownUtil.getTaskFilePath(this))
				.setChunkInfo(taskInfo.getChunkInfoList().get(0))
				.setConnectInfo(connectInfo)
				.setHttpDownloader(this)
				.build();

		if (taskInfo.pause()) {
		} else {
			singleDownloadRunnable.run();
		}
	}

	protected void handleWithMultipleConnection(final List<ChunkInfo> chunkInfoList) throws DownloaderException {
		LOGGER.debug("download with multi thread");

		final ArrayList<DownloadRunnable> tempDownloadRunnables = new ArrayList<>();
		long realCurrentLength = 0;
		for (ChunkInfo chunkInfo : chunkInfoList) {
			LOGGER.debug("chunkInfo - {}", chunkInfo);

			realCurrentLength += (chunkInfo.getCurrentOffset() - chunkInfo.getStartOffset());

			if (chunkInfo.getStatus() == HttpDownStatus.COMPLETE.getStatus()) {
				continue;
			}
			final long contentLength;
			if (chunkInfo.getEndOffset() == -1) {
				// is the last one
				contentLength = taskInfo.getTotalSize() - chunkInfo.getCurrentOffset();
			} else {
				contentLength = chunkInfo.getEndOffset() - chunkInfo.getCurrentOffset() + 1;
			}
			if (contentLength <= 0) {
				chunkInfo.setStatus(HttpDownStatus.COMPLETE.getStatus());
				continue;
			}

			Map<String, String> newHeaders = HttpDownUtil.addRangeForHeader(headers,
												chunkInfo.getCurrentOffset(), chunkInfo.getEndOffset());
			ConnectInfo connectInfo = new ConnectInfo.Builder()
											.url(taskInfo.getUrl())
											.headers(newHeaders)
											.supportRange(isSupportRange)
											.build();
			DownloadRunnable runnable = new DownloadRunnable.Builder()
					.setPath(HttpDownUtil.getTaskFilePath(this))
					.setChunkInfo(chunkInfo)
					.setConnectInfo(connectInfo)
					.setHttpDownloader(this)
					.build();
			tempDownloadRunnables.add(runnable);
		}
		taskInfo.setCurrentOffset(realCurrentLength);
		speedMonitor.start(taskInfo.getCurrentOffset());

		if (taskInfo.pause()) {
		} else {
			downloadRunnables = tempDownloadRunnables;
			if (downloadRunnables.isEmpty()) {
				updateStatus(HttpDownStatus.COMPLETE);
			} else {
				// downloadRunnables.forEach(runnable -> System.out.println(runnable.getChunkInfo()));
				for (DownloadRunnable runnable : downloadRunnables) {
					executorService.submit(runnable);
				}
			}
		}
	}

	protected void buildChunkInfoList(long totalLength, int connectionCount) throws DownloaderException {
		final List<ChunkInfo> chunkInfoList = new ArrayList<>();
		if (isSingleConnection) {
			ChunkInfo chunkInfo = new ChunkInfo();
			chunkInfo.setIndex(0);
			chunkInfo.setStartOffset(0);
			chunkInfo.setCurrentOffset(0);
			chunkInfo.setEndOffset(totalLength);
			chunkInfoList.add(chunkInfo);
		} else {
			long startOffset = 0;
			final long eachRegion = totalLength / connectionCount;

			for (int i = 0; i < connectionCount; i++) {
				final long endOffset;
				if (i == connectionCount - 1) {
					endOffset = -1;
				} else {
					endOffset = startOffset + eachRegion - 1;
				}
				ChunkInfo chunkInfo = new ChunkInfo();
				chunkInfo.setIndex(i);
				chunkInfo.setStartOffset(startOffset);
				chunkInfo.setEndOffset(endOffset);
				chunkInfo.setCurrentOffset(startOffset);
				chunkInfoList.add(chunkInfo);

				startOffset += eachRegion;
			}
		}
		taskInfo.setChunkInfoList(chunkInfoList);
	}

	protected int calcConnectionCount(long totalLength) {
		if (isSupportRange) {
			if (isResume) {
				return taskInfo.getConnectionCount();
			} else {
				return HttpDownUtil.calcConnectionCount(totalLength);
			}
		}
		return 1;
	}

	protected long calculateCallbackMinIntervalBytes(long totalSize, int callbackDefaultProgressMaxCount) {
		if (totalSize <= 0) {
			return NO_ANY_PROGRESS_CALLBACK; // 不知道 totalSize， 不展示下载进度
//			return CALLBACK_MIN_INTERVAL_BYTES;
		} else {
			long callbackMinIntervalBytes = totalSize / callbackDefaultProgressMaxCount;
//			return callbackMinIntervalBytes <= 0L ? CALLBACK_MIN_INTERVAL_BYTES : callbackMinIntervalBytes;
			return callbackMinIntervalBytes <= CALLBACK_MIN_INTERVAL_BYTES ? CALLBACK_MIN_INTERVAL_BYTES : callbackMinIntervalBytes;
		}
	}

	private void isNeedCallbackToUser(long now) {
		boolean needCallBack = false;
//		if (callbackMinIntervalBytes == NO_ANY_PROGRESS_CALLBACK)
//			return;
		if (isFirstCallbackProgressToUser) {
			isFirstCallbackProgressToUser = false;
			needCallBack = true;
		} else {
			if ((now - lastCallbackTimestamp) >= CALLBACK_MIN_INTERVAL_MILLIS
					|| ((callbackMinIntervalBytes != NO_ANY_PROGRESS_CALLBACK)
					&& callbackIncreaseBuffer.get() >= callbackMinIntervalBytes)) {
				needCallBack = true;
			}
		}
		if (needCallBack && needCallbackProgressToUser.compareAndSet(false, true)) {
			lastCallbackTimestamp = now;
			callbackIncreaseBuffer.set(0L);
		}
	}

	private void handleProcess() {
		if (needCallbackProgressToUser.compareAndSet(true, false)) {
			MessageCore.send(MessageType.NORMAL, HttpDownStatus.DOWNLOADING, "", taskInfo);
		}
	}

}

