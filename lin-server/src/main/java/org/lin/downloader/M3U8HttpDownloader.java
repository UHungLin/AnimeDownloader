package org.lin.downloader;

import org.apache.commons.lang3.StringUtils;
import org.lin.constant.Global;
import org.lin.constant.HttpDownStatus;
import org.lin.core.DelayHandleCore;
import org.lin.pojo.DelayHandleTask;
import org.lin.pojo.entity.ChunkInfo;
import org.lin.pojo.entity.ConnectInfo;
import org.lin.pojo.entity.TaskInfo;
import org.lin.exception.DownloaderException;
import org.lin.util.FileUtils;
import org.lin.util.HttpDownUtil;
import org.lin.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/22
 */
public class M3U8HttpDownloader extends DefaultHttpDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(M3U8HttpDownloader.class);

	private String dirPath;
	protected List<String> tsUrlList = new ArrayList<>();


	protected M3U8HttpDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		super(headers, taskInfo);
		isSupportRange = false;
		isSingleConnection = false;
	}

	public static M3U8HttpDownloader newInstance(Map<String, String> headers, TaskInfo taskInfo) {
		return new M3U8HttpDownloader(headers, taskInfo);
	}

	@Override
	public void run() {
		if (taskInfo.pause() || taskInfo.downloading())
			return;

		updateStatus(HttpDownStatus.DOWNLOADING);
		msg = "";

		calcCallbackMinIntervalBytes();

		dirPath = HttpDownUtil.getTaskFilePathWithoutSuffix(this);
		if (FileUtils.existsDirectory(dirPath)) {
			if (!isResume) {
				String newFileName = FileUtils.renameDir(dirPath);
				taskInfo.setName(newFileName);
				dirPath = HttpDownUtil.getTaskFilePathWithoutSuffix(this);
			}
		}
		if (!isResume) {
			try {
				FileUtils.createDirectory(dirPath);
			} catch (IOException e) {
				onError(String.format("create directory - [%s] fail", taskInfo.getName()));
				return;
			}
		}
		if (taskInfo.pause()) {
			return;
		}
		final int connectionCount = calcConnectionCount(taskInfo.getTotalSize());
		taskInfo.setConnectionCount(connectionCount);

		LOGGER.debug("taskInfo: {}", taskInfo);
		try {
			if (!isResume) {
				buildChunkInfoList(taskInfo.getTotalSize(), connectionCount);
			}
			handleWithMultipleConnection(taskInfo.getChunkInfoList());
		} catch (DownloaderException e) {
			LOGGER.error("handle connection fail. {}", e.getMessage());
			onError(e.getMessage());
		}
	}

	@Override
	protected void trialConnect() throws DownloaderException {

	}

	@Override
	protected int calcConnectionCount(long totalLength) {
		return -1; // unKnown
	}

	// TODO 优化
	@Override
	protected void buildChunkInfoList(long totalLength, int connectionCount) throws DownloaderException {
		String m3u8Content = HttpUtil.doGet(taskInfo.getUrl(), headers);
		if (!m3u8Content.contains("#EXTM3U"))
			throw new DownloaderException("不是m3u8链接");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
				m3u8Content.getBytes(StandardCharsets.UTF_8)),
				StandardCharsets.UTF_8));
		String line;
		try {
			while (StringUtils.isNotBlank(line = reader.readLine())) {
				if (line.contains("EXT-X-KEY")) {
					// TODO 好困，下次再写
					throw new DownloaderException("加密的m3u8文件，当前版本暂不支持下载~");
				}
				if (line.contains(".m3u8")) { // 需要从第二个m3u8链接获取
					System.out.println(line);
					if (!line.contains("http")) {
						String beforeUrl = taskInfo.getUrl();
						URL url = new URL(beforeUrl);
						line = new URL(new URL(beforeUrl.split(url.getPath())[0]), line).toString();
						System.out.println("line = " + line);
					}
					taskInfo.setUrl(line);
					buildChunkInfoList(0, 0);
					continue;
				}
				LOGGER.debug("line = {}", line);
				if (line.startsWith("#")) {
					continue;
				}
				tsUrlList.add(line);
			}
			reader.close();
		} catch (IOException e) {
		}
		final List<ChunkInfo> chunkInfoList = new ArrayList<>(tsUrlList.size());
		for (int i = 0; i < tsUrlList.size(); i++) {
			ChunkInfo chunkInfo = new ChunkInfo();
			chunkInfo.setIndex(i);
			chunkInfo.setStartOffset(0);
			chunkInfo.setCurrentOffset(0);
			chunkInfo.setEndOffset(0);
			chunkInfoList.add(chunkInfo);
		}
		taskInfo.setChunkInfoList(chunkInfoList);
	}

	@Override
	protected void handleWithMultipleConnection(List<ChunkInfo> chunkInfoList) throws DownloaderException {
		if (!isResume) {
			final ArrayList<DownloadRunnable> tempDownloadRunnables = new ArrayList<>();
			for (int i = 0; i < chunkInfoList.size(); i++) {
				ConnectInfo connectInfo = new ConnectInfo.Builder()
						.url(tsUrlList.get(i))
						.headers(headers)
						.supportRange(isSupportRange)
						.build();
				LOGGER.debug("chunkInfo - {}", chunkInfoList.get(i));
				DownloadRunnable runnable = new DownloadRunnable.Builder()
						.setPath(dirPath + File.separator + i + Global.M3U8_VIDEO_SUFFIX)
						.setChunkInfo(chunkInfoList.get(i))
						.setConnectInfo(connectInfo)
						.setHttpDownloader(this)
						.build();
				tempDownloadRunnables.add(runnable);
			}
			downloadRunnables = tempDownloadRunnables;
			LOGGER.debug("downloadRunnables = {}", downloadRunnables);
		} else {
			for (DownloadRunnable runnable : downloadRunnables) {
				runnable.resume();
				ChunkInfo chunkInfo = runnable.getChunkInfo();
				chunkInfo.setCurrentOffset(0L);
				String path = runnable.getPath();
				FileUtils.deleteFile(path);
			}
		}
		speedMonitor.start(taskInfo.getCurrentOffset());
		if (taskInfo.pause()) {
		} else {
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

	@Override
	public void onComplete(DownloadRunnable runnable) {
		synchronized (this) {
			LOGGER.info("M3U8HttpDownloader onComplete");
			super.onComplete(runnable);
			if (this.complete()) {
				String dirPath = HttpDownUtil.getTaskFilePathWithoutSuffix(this);
				String filePath = dirPath + Global.DEFAULT_VIDEO_SUFFIX;
				FileChannel toChannel = null;
				FileChannel fromChannel = null;
				try {
					if (FileUtils.existsFile(filePath)) {
						FileUtils.deleteFile(filePath);
					}
					FileUtils.createFile(filePath);
					FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
					toChannel = fileOutputStream.getChannel();
					File[] files = new File(dirPath).listFiles();
					Collections.sort(Arrays.asList(files), new Comparator<File>() {
						@Override
						public int compare(File f1, File f2) {
							return Integer.compare(getName(f1), getName(f2));
						}
					});
					for (File file : files) {
						System.out.println(file.getAbsolutePath());
						fromChannel = new FileInputStream(file).getChannel();
						fromChannel.transferTo(0, fromChannel.size(), toChannel);
						fromChannel.close();
					}
					LOGGER.debug("merge ts file complete, delete temp file");
//					FileUtils.deleteDir(new File(dirPath));
					DelayHandleTask task = DelayHandleTask.delayTask(DelayHandleTask.Type.DELETE_DIR, dirPath);
					DelayHandleCore.getInstance().addTask(task);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (toChannel != null) {
						try {
							toChannel.close();
						} catch (IOException e) {
						}
					}
					if (fromChannel != null) {
						try {
							fromChannel.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
	}

	private Integer getName(File file) {
		return Integer.parseInt(file.getName().split(".ts")[0]);
	}

}
