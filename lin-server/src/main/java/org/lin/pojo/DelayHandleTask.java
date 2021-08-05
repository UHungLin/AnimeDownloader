package org.lin.pojo;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/31
 */
public class DelayHandleTask {

	public enum Type {
		DELETE_FILE,
		DELETE_DIR
	}

	private Type type;
	private String data;

	private DelayHandleTask(Type type, String data) {
		this.type = type;
		this.data = data;
	}

	public static DelayHandleTask delayTask(Type type, String data) {
		return new DelayHandleTask(type, data);
	}

	public Type getType() {
		return type;
	}

	public String getData() {
		return data;
	}

	@Override
	public String toString() {
		return "DownloaderDelayTask{" +
				"type=" + type +
				", data='" + data + '\'' +
				'}';
	}
}
