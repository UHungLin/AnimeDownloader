package org.lin.http.bilibili.common;

import java.util.HashMap;
import java.util.Map;

public class RequestMessage {

	private String uri;

	private Map<String, String> headers = new HashMap<String, String>();

	public RequestMessage() {
	}

	public RequestMessage(String uri, Map<String, String> headers) {
		super();
		this.uri = uri;
		this.headers = headers;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

}
