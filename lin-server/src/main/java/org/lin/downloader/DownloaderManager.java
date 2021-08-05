package org.lin.downloader;

import org.lin.constant.Global;
import org.lin.constant.HttpDownStatus;
import org.lin.constant.MessageType;
import org.lin.core.DelayHandleCore;
import org.lin.core.MessageCore;
import org.lin.core.ThreadContext;
import org.lin.pojo.DelayHandleTask;
import org.lin.pojo.entity.TaskInfo;
import org.lin.exception.DownloaderBuildException;
import org.lin.util.HttpDownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/28
 */
public class DownloaderManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderManager.class);

	/** 同时下载任务数 */
	private static final Integer DOWNLOADING_TASK_COUNT_LIMIT = 3;

	private final ExecutorService executorService;

	private final Map<String, IHttpDownloader> downloadTaskMap;

	private DownloaderManager() {
		this.executorService = ThreadContext.newExecutor(8, 20,
				1000, 60, ThreadContext.LIN_GET_THREAD_DOWNLOADER);
		this.downloadTaskMap = new ConcurrentHashMap<>();

	}

	public void init() {
		// load task from DB
	}

	private static final DownloaderManager INSTANCE = new DownloaderManager();

	public static DownloaderManager getInstance() {
		return INSTANCE;
	}

	public void pause(String taskId) {
		System.out.println("taskId - " + taskId);
		IHttpDownloader iHttpDownloader = downloadTaskMap.get(taskId);
		if (iHttpDownloader == null) return;
		pause(iHttpDownloader);
	}

	private void pause(IHttpDownloader httpDownloader) {
		httpDownloader.pause();
	}

	public void resume(String taskId) {
		System.out.println("taskId - " + taskId);
		IHttpDownloader iHttpDownloader = downloadTaskMap.get(taskId);
		if (iHttpDownloader == null) return;
		resume(iHttpDownloader);
	}

	private void resume(IHttpDownloader httpDownloader) {
		httpDownloader.resume();
	}

	public void cancel(String taskId) {
		LOGGER.debug("cancel task taskId - {}", taskId);
		MessageCore.deleteMessageByTaskId(taskId);
		IHttpDownloader iHttpDownloader = downloadTaskMap.get(taskId);
		if (iHttpDownloader == null) return;
		cancel(iHttpDownloader);
	}

	/**
	 * 删除下载任务
	 * 如果文件下载完成，只删除下载记录
	 * 如果文件还没下载完成，删除临时文件和下载记录
	 *
	 * @param downloader
	 */
	private void cancel(IHttpDownloader downloader) {
		LOGGER.info("cancel task - {}", downloader.getTaskInfo().getName());
		downloader.cancel();
		downloadTaskMap.remove(downloader.getId());
		if (!downloader.complete()) {
			handleDownloadTempFile(downloader);
		}
	}

	private void handleDownloadTempFile(IHttpDownloader downloader) {
		if (downloader.getTaskInfo().isBilibiliType()
				&& downloader.getTaskInfo().isM4SFileType()) {
			String videoFilePath = HttpDownUtil.getTaskFilePathWithoutSuffix(downloader) + Global.M4S_VIDEO_SUFFIX;
			String audioFilePath = HttpDownUtil.getTaskFilePathWithoutSuffix(downloader) + Global.M4S_AUDIO_SUFFIX;
			DelayHandleTask task_1 = DelayHandleTask.delayTask(DelayHandleTask.Type.DELETE_FILE, videoFilePath);
			DelayHandleTask task_2 = DelayHandleTask.delayTask(DelayHandleTask.Type.DELETE_FILE, audioFilePath);
			DelayHandleCore.getInstance().addTask(task_1);
			DelayHandleCore.getInstance().addTask(task_2);
		} else if (downloader.getTaskInfo().isM3U8FileType()) {
			String dirPath = HttpDownUtil.getTaskFilePathWithoutSuffix(downloader);
			DelayHandleTask task = DelayHandleTask.delayTask(DelayHandleTask.Type.DELETE_DIR, dirPath);
			DelayHandleCore.getInstance().addTask(task);
		} else if (downloader.getTaskInfo().isMP4FileType()) {
			String filePath = HttpDownUtil.getTaskFilePathWithMp4Suffix(downloader);
			DelayHandleTask task = DelayHandleTask.delayTask(DelayHandleTask.Type.DELETE_FILE, filePath);
			DelayHandleCore.getInstance().addTask(task);
		}
	}

	public void start(TaskInfo taskInfo, Map<String, String> headers) throws DownloaderBuildException {
		if (taskInfo == null) return;
		synchronized (this.downloadTaskMap) {
			DefaultHttpDownloader httpDownloader = buildDownloader(taskInfo, headers);
			if (httpDownloader == null)
				throw new DownloaderBuildException("创建下载器失败");
			submit(httpDownloader).start();
		}
	}

	private IHttpDownloader submit(DefaultHttpDownloader httpDownloader) {
		IHttpDownloader existDownloader = downloadTaskMap.get(httpDownloader.getId());
		if (existDownloader != null) {
			return existDownloader;
		}
		downloadTaskMap.put(httpDownloader.getId(), httpDownloader);
		MessageCore.send(MessageType.NORMAL, HttpDownStatus.WAITING, "", httpDownloader.taskInfo);
		return httpDownloader;
	}

	private DefaultHttpDownloader buildDownloader(TaskInfo taskInfo,
	                                              Map<String, String> headers){
		DefaultHttpDownloader httpDownloader = taskInfo.buildDownloader(headers);
		return httpDownloader;
	}

	public void complete(String taskId) {
		LOGGER.debug("task complete, delete taskId - {}", taskId);
		downloadTaskMap.remove(taskId);
	}

	public void refresh() {
		synchronized (downloadTaskMap) {
			final Collection<IHttpDownloader> downloaders = downloadTaskMap.values();
			final long count = downloaders.stream()
					.filter(downloader ->
						downloader.downloading() || downloader.retrying()
					).count();
			if (count == DOWNLOADING_TASK_COUNT_LIMIT) {
				// pass
			} else if (count > DOWNLOADING_TASK_COUNT_LIMIT) {
				downloaders.stream()
						.filter(IHttpDownloader::downloading)
						.skip(DOWNLOADING_TASK_COUNT_LIMIT)
						.forEach(IHttpDownloader::pause);
			} else {
				downloaders.stream()
						.filter(IHttpDownloader::waiting)
						.limit(DOWNLOADING_TASK_COUNT_LIMIT - count)
						.forEach(executorService::submit);
			}
		}
	}

	public void stop() {
		this.downloadTaskMap.values().stream()
				.forEach(IHttpDownloader::pause);
		ThreadContext.shutdown(this.executorService);
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public Map<String, IHttpDownloader> getDownloadTaskMap() {
		return downloadTaskMap;
	}

}

