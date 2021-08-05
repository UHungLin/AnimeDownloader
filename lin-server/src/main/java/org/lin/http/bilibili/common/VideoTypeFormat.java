package org.lin.http.bilibili.common;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/25
 */
public enum VideoTypeFormat {

	M4S(16), FLV(0), MP4(1);

	private int value;

	public int getValue() {
		return value;
	}

	private VideoTypeFormat(int value) {
		this.value = value;
	}

}
