package org.lin.http.bilibili.common;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.lin.http.bilibili.common.RequestMessage;
import org.lin.http.bilibili.common.ResponseMessage;
import org.lin.util.HttpUtil;

import java.io.IOException;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/25
 */
public class DefaultServiceClient {

//	public ResponseMessage sendRequestCore(RequestMessage request) {
//		HttpRequestBase httpRequest = createHttpRequest(request);
//		HttpResponse httpResponse = HttpUtil.doGet(httpRequest);
//		ResponseMessage response =  buildResponse(httpResponse);
//		return response;
//	}

	public ResponseMessage sendRequestCore(RequestMessage request) {
		HttpRequestBase httpRequest = createHttpRequest(request);
		int timeout = 3;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
		} catch (IOException e) {
			httpRequest.abort();
			throw new RuntimeException(e.getMessage(), e);
		}
		ResponseMessage response = buildResponse(httpResponse);
		return response;
	}

	protected HttpRequestBase createHttpRequest(RequestMessage request) {
		HttpGet getMethod = new HttpGet(request.getUri());
		if (request.getHeaders() != null && request.getHeaders().size() > 0) {
			for (String key : request.getHeaders().keySet()) {
				getMethod.addHeader(key, request.getHeaders().get(key));
			}
		}
		return getMethod;
	}

	protected ResponseMessage buildResponse(HttpResponse httpResponse) {
		if (httpResponse == null)
			return null;

		ResponseMessage response = new ResponseMessage();

		if (httpResponse.getStatusLine() != null) {
			response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
		}

		if (httpResponse.getEntity() != null && HttpStatus.SC_OK == response.getStatusCode()) {
			HttpEntity entity = httpResponse.getEntity();
			String content;
			try {
				content = EntityUtils.toString(entity, Consts.UTF_8);
				EntityUtils.consumeQuietly(entity);
				response.setBody(content);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		return response;
	}

}
