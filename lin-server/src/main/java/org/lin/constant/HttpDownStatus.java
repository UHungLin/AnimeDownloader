package org.lin.constant;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/26
 */
public enum HttpDownStatus {

	WAITING(0), DOWNLOADING(1), PAUSE(2), FAIL(3), COMPLETE(4), RETRY(5);

	public int getStatus() {
		return status;
	}

	private final int status;

	HttpDownStatus(int status) {
		this.status = status;
	}

	public static HttpDownStatus getStatus(int status) {
		for (HttpDownStatus value : HttpDownStatus.values()) {
			if (value.getStatus() == status) {
				return value;
			}
		}
		return null;
	}

}
