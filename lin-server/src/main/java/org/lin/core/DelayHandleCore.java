package org.lin.core;

import org.apache.commons.lang3.StringUtils;
import org.lin.pojo.DelayHandleTask;
import org.lin.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/31
 */
public class DelayHandleCore {

	private static final Logger LOGGER = LoggerFactory.getLogger(DelayHandleCore.class);

	private static List<DelayHandleTask> tasks = new CopyOnWriteArrayList<>();

	private ScheduledFuture<?> delayHandleTaskTimer;

	private static final DelayHandleCore INSTANCE = new DelayHandleCore();

	private DelayHandleCore() {}

	public static DelayHandleCore getInstance() {
		return INSTANCE;
	}

	public void init() {
		loadTaskTimer();
	}

	public void addTask(DelayHandleTask task) {
		tasks.add(task);
	}

	public void stop() {
		ThreadContext.shutdown(this.delayHandleTaskTimer);
	}

	private void loadTaskTimer() {
		System.out.println("初始化 DelayHandleCore");
		this.delayHandleTaskTimer = ThreadContext.timer(5, 2, TimeUnit.SECONDS, new DelayHandleTaskTimer());
	}


	private class DelayHandleTaskTimer implements Runnable {
		@Override
		public void run() {
			if (tasks.isEmpty()) {
				try {
					TimeUnit.SECONDS.sleep(1);
					return;
				} catch (InterruptedException e) {
				}
			}
			for (DelayHandleTask task : tasks) {
				System.out.println("task - " + task);
				switch (task.getType()) {
					case DELETE_FILE:
						if (deleteFile(task.getData())) {
							LOGGER.debug("删除文件 - " + task.getData() + " 成功");
							tasks.remove(task);
						} else {
							LOGGER.debug("删除文件 - " + task.getData() + " 失败");
						}
						break;
					case DELETE_DIR:
						if (deleteDir(task.getData())) {
							LOGGER.debug("删除文件夹 - " + task.getData() + " 成功");
							tasks.remove(task);
						} else {
							LOGGER.debug("删除文件夹 - " + task.getData() + " 失败");
						}
						break;
					default:
						LOGGER.warn("DownloaderDelayTask Type is not adapted - {}", task);
						break;
				}
			}
		}
	}

	private boolean deleteFile(String path) {
		if (StringUtils.isBlank(path) || !FileUtils.existsFile(path))
			return true;
		return FileUtils.deleteFile(path);
	}

	private boolean deleteDir(String path) {
		if (StringUtils.isBlank(path) || !FileUtils.existsDirectory(path))
			return true;
		return FileUtils.deleteDir(path);
	}

}
