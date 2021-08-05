package org.lin.downloader;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/6
 */
public class DownloadSpeedMonitor {

	private long startDownloadSize;
	private long lastDownloadSize;
	private long lastRefreshTime;
	private long startTime;

	private int speed;

	// kb/s
	private long minIntervalUpdateSpeed = 1000;

	public int getSpeed() {
		return speed;
	}

	public void start(long downloadSize) {
		startDownloadSize = downloadSize;
		startTime = System.currentTimeMillis();
	}

	public void update(long downloadSize) {
		if (minIntervalUpdateSpeed <= 0) {
			return;
		}

		boolean isUpdateDate = false;
		if (lastRefreshTime == 0L) {
			isUpdateDate = true;
		} else {
			long interval = System.currentTimeMillis() - lastRefreshTime;
			if (interval >= minIntervalUpdateSpeed || (speed == 0 && interval > 0)) {
				isUpdateDate = true;
				speed = (int) ((downloadSize - lastDownloadSize) / interval * 1000); // byte/s
				speed = Math.max(0, speed);
			}
		}
		if (isUpdateDate) {
			lastDownloadSize = downloadSize;
			lastRefreshTime = System.currentTimeMillis();
		}
	}

	public void reset() {
		speed = 0;
		lastRefreshTime = 0L;
	}

	public void end(long downloadSize) {
		if (startTime <= 0)
			return;

		downloadSize = downloadSize - startDownloadSize;
		lastRefreshTime = 0L;
		long interval = System.currentTimeMillis() - startTime;
		if (interval <= 0) {
			speed = (int) downloadSize;
		} else {
			speed = (int) (downloadSize / interval);
		}
	}


}
