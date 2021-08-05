package org.lin.downloader;

import org.lin.constant.HttpDownStatus;
import org.lin.pojo.entity.TaskInfo;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/28
 */
public interface IHttpDownloader extends Runnable {

	String getId();

	TaskInfo getTaskInfo();

	void updateStatus(HttpDownStatus status);

	void start();

	void pause();

	void resume();

	void cancel();

	boolean downloading();

	boolean retrying();

	boolean waiting();

	boolean complete();

}
