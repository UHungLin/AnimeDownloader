package org.lin.pojo;

import java.util.Map;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/30
 */
public class HttpHeaderWrapper {

	private int code;
	private Map<String, String> headers;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
}
