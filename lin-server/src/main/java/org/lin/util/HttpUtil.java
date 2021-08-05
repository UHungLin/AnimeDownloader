package org.lin.util;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.lin.pojo.HttpHeaderWrapper;

import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/25
 */
public class HttpUtil {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";

	// timeout setting
	private static final RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(5000)
			.setConnectionRequestTimeout(5000)
			.setSocketTimeout(10000)
			.build();

	private static final ConnectionConfig connectionConfig = ConnectionConfig.custom()
			.setMalformedInputAction(CodingErrorAction.IGNORE)
			.setUnmappableInputAction(CodingErrorAction.IGNORE)
			.setCharset(Consts.UTF_8)
			.build();

	private static HttpClientBuilder getBuilder() {
		List<Header> headers = new ArrayList<>();
		Header header = new BasicHeader("User-Agent", USER_AGENT);
		headers.add(header);
		return HttpClients.custom().setDefaultConnectionConfig(connectionConfig)
				.setDefaultHeaders(headers).setDefaultRequestConfig(requestConfig);
	}

	public static String doGet(String url) {
		String result;
		HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpClient httpclient = getBuilder().build();
		     CloseableHttpResponse response = httpclient.execute(httpGet)) {
			HttpEntity httpEntity = response.getEntity();
			result = EntityUtils.toString(httpEntity);
		} catch (IOException e) {
			httpGet.abort();
			throw new RuntimeException(e.getMessage(), e);
		}
		return result;
	}

	public static String doGet(String url, List<Header> headers) {
		return doGet(url, headers, StandardCharsets.UTF_8.name());
	}

	public static String doGet(String url, List<Header> headers, String charset) {
		String result;
		HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpClient httpclient = getBuilder(headers).build();
		     CloseableHttpResponse response = httpclient.execute(httpGet)) {
			HttpEntity httpEntity = response.getEntity();
			result = EntityUtils.toString(httpEntity, charset);
		} catch (IOException e) {
			httpGet.abort();
			throw new RuntimeException(e.getMessage(), e);
		}
		return result;
	}

	public static String doGet(String url, Map<String, String> headers) {
		return doGet(url, headers, StandardCharsets.UTF_8.name());
	}

	public static String doGet(String url, Map<String, String> headers, String charset) {
		List<Header> headerList = new ArrayList<>();
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				Header header = new BasicHeader(entry.getKey(), entry.getValue());
				headerList.add(header);
			}
		}
		return doGet(url, headerList, charset);
	}

	public static HttpHeaderWrapper doGetForHeaders(String url, Map<String, String> headers) throws IOException {
		HttpHeaderWrapper wrapper = new HttpHeaderWrapper();
//		HttpHost proxy = new HttpHost("127.0.0.1", 8866);
		HttpGet httpGet = new HttpGet(url);

		List<Header> headerList = new ArrayList<>();
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				Header header = new BasicHeader(entry.getKey(), entry.getValue());
				headerList.add(header);
			}
		}
		try (CloseableHttpClient httpclient = getBuilder(headerList).build();
		     CloseableHttpResponse response = httpclient.execute(httpGet)) {
			Header[] allHeaders = response.getAllHeaders();
			int statusCode = response.getStatusLine().getStatusCode();
			Map<String, String> headerMap = new HashMap<>();
			for (Header header : allHeaders) {
				headerMap.put(header.getName(), header.getValue());
			}
			wrapper.setCode(statusCode);
			wrapper.setHeaders(headerMap);
		} catch (IOException e) {
			httpGet.abort();
			throw e;
		}
		return wrapper;
	}

	public static HttpResponse doGet(HttpRequestBase httpRequest) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
		} catch (IOException e) {
			httpRequest.abort();
			throw new RuntimeException(e.getMessage(), e);
		}
		return httpResponse;
	}

	public static HttpHeaderWrapper doHead(String url, Map<String, String> headers) throws IOException {
		HttpHeaderWrapper wrapper = new HttpHeaderWrapper();
		HttpHead httpHead = new HttpHead(url);
		List<Header> headerList = new ArrayList<>();
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				Header header = new BasicHeader(entry.getKey(), entry.getValue());
				headerList.add(header);
			}
		}
		try (CloseableHttpClient httpclient = getBuilder(headerList).build();
		     CloseableHttpResponse response = httpclient.execute(httpHead)) {
			int statusCode = response.getStatusLine().getStatusCode();
			Map<String, String> headerMap = new HashMap<>();
			for (Header header : response.getAllHeaders()) {
				headerMap.put(header.getName(), header.getValue());
			}
			wrapper.setCode(statusCode);
			wrapper.setHeaders(headerMap);
		}
		return wrapper;
	}

	private static HttpClientBuilder getBuilder(List<Header> headers) {
		return HttpClients.custom().setDefaultConnectionConfig(connectionConfig)
				.setDefaultHeaders(headers).setDefaultRequestConfig(requestConfig);
	}

}

