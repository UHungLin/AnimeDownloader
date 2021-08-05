package org.lin.http.bilibili;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.lin.config.ApiConfig;
import org.lin.http.bilibili.common.DefaultServiceClient;
import org.lin.http.bilibili.common.VideoTypeFormat;
import org.lin.http.bilibili.response.*;
import org.lin.http.bilibili.common.RequestMessage;
import org.lin.http.bilibili.common.ResponseMessage;

import java.util.Map;

/**
 * @author Lin =￣ω￣=
 * @date 2020/7/30
 */
public class BilibiliApiClient {

	private DefaultServiceClient serviceClient;

	private static Gson gson = new Gson().newBuilder().create();

	public BilibiliApiClient() {
		this.serviceClient = new DefaultServiceClient();
	}

	/**
	 * get AV/BV video info
	 *
	 * @param bvId
	 * @return
	 */
	public BilibiliVideoResponse getVideoInfo(String bvId, Map<String, String> headers) throws BilibiliException {
		String url = String.format(ApiConfig.BILIBILI_VIEW, bvId);
		BilibiliVideoResponse response = doRequest(url, headers, BilibiliVideoResponse.class);
		return response;
	}

	public BilibiliFlvPlayUrlResponse getFlvPlayUrl(String bvId, int cId, Map<String, String> headers) throws BilibiliException {
		String url = String.format(ApiConfig.BILIBILI_PLAYURL, bvId, cId, VideoTypeFormat.FLV.getValue(), "");
		BilibiliFlvPlayUrlResponse response = doRequest(url, headers, BilibiliFlvPlayUrlResponse.class);
		return response;
	}

	/**
	 * 获取M4S格式视频播放链接
	 *
	 * @param bvId
	 * @param cid
	 * @return
	 */
	public BilibiliM4SPlayUrlResponse getM4SFormatVideoPlayUrl(String bvId, Integer cid, Map<String, String> headers) throws BilibiliException {
		String url = String.format(ApiConfig.BILIBILI_PLAYURL, bvId, cid, VideoTypeFormat.M4S.getValue(), "");
		BilibiliM4SPlayUrlResponse response = doRequest(url, headers, BilibiliM4SPlayUrlResponse.class);
		return response;
	}

	public BilibiliSeasonResponse getSeason(String seasonID, Map<String, String> headers) throws BilibiliException {
		String url = String.format(ApiConfig.BILIBILI_SEASON, seasonID);
		BilibiliSeasonResponse response = doRequest(url, headers, BilibiliSeasonResponse.class);
		return response;
	}

	public BilibiliMediaResponse getMedia(String mediaID, Map<String, String> headers) throws BilibiliException {
		String url = String.format(ApiConfig.BILIBILI_MEDIA, mediaID);
		BilibiliMediaResponse response = doRequest(url, headers, BilibiliMediaResponse.class);
		return response;
	}

	protected <T extends GenericResponse> T doRequest(String url, Map<String, String> headers, Class<T> clazz) throws BilibiliException {
		RequestMessage request = buildRequest(url, headers);
		ResponseMessage responseMessage = sendRequest(request);
		T genericResponse = buildResponse(responseMessage, clazz);
		handleResponse(url, genericResponse);
		return genericResponse;
	}

	protected RequestMessage buildRequest(String url, Map<String, String> headers) {
		RequestMessage request = new RequestMessage();
		request.setUri(url);
		request.setHeaders(headers);
		return request;
	}

	protected ResponseMessage sendRequest(RequestMessage request) {
		ResponseMessage responseMessage = serviceClient.sendRequestCore(request);
		return responseMessage;
	}

	protected <T> T buildResponse(ResponseMessage responseMessage, Class<T> clazz) {
		if (responseMessage != null && StringUtils.isNoneBlank(responseMessage.getBody())) {
			System.out.println("====" + responseMessage.getBody());
			return gson.fromJson(responseMessage.getBody(), clazz);
		}
		return null;
	}

	protected void handleResponse(String url, GenericResponse genericResponse) throws BilibiliException {
		if (genericResponse.getCode() != ResponseCode.SUCCESS) {
			throw new BilibiliException(genericResponse.getCode(), genericResponse.getMessage());
		}
	}

}
