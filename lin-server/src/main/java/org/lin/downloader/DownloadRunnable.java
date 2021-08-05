package org.lin.downloader;

import org.apache.commons.lang3.StringUtils;
import org.lin.constant.HttpDownStatus;
import org.lin.pojo.entity.ChunkInfo;
import org.lin.pojo.entity.ConnectInfo;
import org.lin.exception.DownloaderException;
import org.lin.util.HttpDownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/27
 */
public class DownloadRunnable implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadRunnable.class);

	private static final int BUFFER_SIZE = 1024 * 4;

	private final String path;
	private final ChunkInfo chunkInfo;
	private final ConnectInfo connectInfo;
	private final DefaultHttpDownloader downloader;
	private InputStream inputStream;
	private int retryTimes;
	private volatile boolean paused;

	public DownloadRunnable(String path, ChunkInfo chunkInfo, ConnectInfo connectInfo,
	                        DefaultHttpDownloader downloader) {
		this.path = path;
		this.chunkInfo = chunkInfo;
		this.connectInfo = connectInfo;
		this.paused = false;
		this.downloader = downloader;
		retryTimes = 3;
	}

	@Override
	public void run() {
		if (paused) return;

		LOGGER.debug("start download...");

		RandomAccessFile raf = null;
		FileDescriptor fileDescriptor = null;
		BufferedOutputStream out = null;
		do {
			try {
				raf = new RandomAccessFile(path, "rw");
				raf.seek(chunkInfo.getCurrentOffset());
				fileDescriptor = raf.getFD();
				out = new BufferedOutputStream(new FileOutputStream(fileDescriptor));

				connect();

				if (paused) return;

				byte[] buffer = new byte[BUFFER_SIZE];
				int readLen = 0;

				while (downloader.downloading()) {
					readLen = inputStream.read(buffer);
					if (readLen <= -1) {
						downloader.onComplete(this);
						break;
					}
					out.write(buffer, 0, readLen);
					chunkInfo.increaseSize(readLen);
					downloader.onProcess(readLen);
				}
				break;
			} catch (IOException | DownloaderException e) {
				System.out.println(e);
				if (isRetry(e)) {
					try {
						LOGGER.info("request timeout. chunkInfo index's {}", chunkInfo.getIndex());
						downloader.onRetry("请求超时，尝试重新连接...");
						TimeUnit.SECONDS.sleep(3);
					} catch (InterruptedException interruptedException) {
						LOGGER.error(e.getMessage());
						downloader.onError("下载失败");
						break;
					}
				} else {
					LOGGER.error(e.getMessage());
					downloader.onError("下载失败");
					break;
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				downloader.onError("下载失败");
				break;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
				if (out != null) {
					try {
						out.flush();
						fileDescriptor.sync();
					} catch (IOException e) {
						LOGGER.error(e.getMessage());
					} finally {
						try {
							raf.close();
							out.close();
						} catch (IOException e) {
						}
					}
				}
			}
		} while (true);
	}

	public void pause() {
		paused = true;
	}

	public void resume() {
		paused = false;
	}

	public ChunkInfo getChunkInfo() {
		return chunkInfo;
	}

	public String getPath() {
		return path;
	}

	private void connect() throws DownloaderException, IOException {
		try {
//			Map<String, String> headers = connectInfo.getHeaders();
//			if (connectInfo.isSupportRange()) {
//				// if retry connect, need to update the currentOffset
//				headers = HttpDownUtil.addRangeForHeader(connectInfo.getHeaders(),
//						chunkInfo.getCurrentOffset(), chunkInfo.getEndOffset());
//			}
			HttpURLConnection connect = HttpDownUtil.connect(connectInfo.getUrl(), connectInfo.getHeaders());
			connect.connect();
			int responseCode = connect.getResponseCode();

			LOGGER.info("the url[{}], is connected with code[{}] ", connectInfo.getUrl(), responseCode);

			if (responseCode != HttpURLConnection.HTTP_OK
					&& responseCode != HttpURLConnection.HTTP_PARTIAL) {
//					downloader.onError(String.format("request failed. status code %s", responseCode));
				throw new DownloaderException(String.format("request failed. status code %s", responseCode));
			}
			inputStream = connect.getInputStream();
			if (downloader.retrying()) {
				downloader.updateStatus(HttpDownStatus.DOWNLOADING);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw e;
		}
	}

	private boolean isRetry(Exception e) {
		if ((e instanceof SocketTimeoutException
				|| e instanceof SocketException)
				&& retryTimes > 0) {
			retryTimes--;
			return true;
		}
		return false;
	}

	public static class Builder {
		private String path;
		private ChunkInfo chunkInfo;
		private ConnectInfo connectInfo;
		private DefaultHttpDownloader hostRunner;

		public Builder setPath(String path) {
			this.path = path;
			return this;
		}

		public Builder setChunkInfo(ChunkInfo chunkInfo) {
			this.chunkInfo = chunkInfo;
			return this;
		}

		public Builder setConnectInfo(ConnectInfo connectInfo) {
			this.connectInfo = connectInfo;
			return this;
		}

		public Builder setHttpDownloader(DefaultHttpDownloader hostRunner) {
			this.hostRunner = hostRunner;
			return this;
		}

		public DownloadRunnable build() throws DownloaderException {
			if (StringUtils.isBlank(path) || hostRunner == null)
				throw new DownloaderException("missing parameters");
			return new DownloadRunnable(path, chunkInfo, connectInfo, hostRunner);
		}

	}

}
