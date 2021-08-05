package org.lin.pojo.entity;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/27
 */

import org.apache.commons.lang3.StringUtils;
import org.lin.exception.DownloaderException;

import java.util.Map;


public class ConnectInfo {

	private boolean supportRange;
	private Map<String, String> headers;
	private String url;

	public ConnectInfo(boolean supportRange, Map<String, String> headers, String url) {
		this.supportRange = supportRange;
		this.headers = headers;
		this.url = url;
	}

	public static class Builder {
		private boolean supportRange;
		private Map<String, String> headers;
		private String url;

		public Builder supportRange(boolean supportRange) {
			this.supportRange = supportRange;
			return this;
		}

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder headers(Map<String, String> headers) {
			this.headers = headers;
			return this;
		}

		public ConnectInfo build() throws DownloaderException {
			if (StringUtils.isBlank(url) || headers == null)
				throw new DownloaderException("build connectionInfo fail.");
			return new ConnectInfo(supportRange, headers, url);
		}

	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "ConnectInfo{" +
				"supportRange=" + supportRange +
				", headers=" + headers +
				", url='" + url + '\'' +
				'}';
	}
}
