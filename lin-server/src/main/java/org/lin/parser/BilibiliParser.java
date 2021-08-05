package org.lin.parser;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lin.annotation.Parser;
import org.lin.constant.BilibiliType;
import org.lin.constant.FileFormatType;
import org.lin.constant.Global;
import org.lin.core.BilibiliClientCore;
import org.lin.downloader.DefaultHttpDownloader;
import org.lin.downloader.FlvHttpDownloader;
import org.lin.downloader.M4SHttpDownloader;
import org.lin.exception.ParseException;
import org.lin.http.bilibili.BilibiliException;
import org.lin.http.bilibili.BilibiliHttpHeaders;
import org.lin.http.bilibili.model.*;
import org.lin.http.bilibili.response.*;
import org.lin.http.view.SubVideoView;
import org.lin.http.view.VideoView;
import org.lin.pojo.entity.TaskInfo;
import org.lin.pojo.entity.VideoInfo;
import org.lin.util.CommonUtil;
import org.lin.util.ConvertUtil;
import org.lin.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/24
 */
@Parser(name = "bilibili")
public final class BilibiliParser extends AbstractParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(BilibiliParser.class);

	private static final Pattern AVPATTERN = Pattern.compile("av([0-9]+)");
	private static final Pattern BVPATTERN = Pattern.compile("BV([0-9A-Za-z]+)");
	private static final Pattern SSPATTERN = Pattern.compile("ss([0-9]+)");
	private static final Pattern MDPATTERN = Pattern.compile("md([0-9]+)");
	private static final Pattern EPPATTERN = Pattern.compile("ep([0-9]+)");

	public BilibiliParser() {
		super(Type.BILIBILI);
	}

	@Override
	public VideoView parse(String url, Map<String, String> headers) throws ParseException {
		LOGGER.info("BilibiliParser parsing...");
		Matcher matcher = null;
		try {
			if ((matcher = AVPATTERN.matcher(url)).find()) {
				String avId = matcher.group(1);
				return getVideoInfo(avId, BilibiliType.AV, headers);
			} else if ((matcher = BVPATTERN.matcher(url)).find()) {
				String bvId = matcher.group();
				return getVideoInfo(bvId, BilibiliType.BV, headers);
			} else if ((matcher = SSPATTERN.matcher(url)).find()) {
				String ssId = matcher.group(1);
				return getVideoInfo(ssId, BilibiliType.SS, headers);
			} else if ((matcher = MDPATTERN.matcher(url)).find()) {
				String mdId = matcher.group(1);
				return getVideoInfo(mdId, BilibiliType.MD, headers);
			} else if ((matcher = EPPATTERN.matcher(url)).find()) {
				String epId = matcher.group(1);
				return getVideoInfo(epId, BilibiliType.EP, headers);
			}
		} catch (BilibiliException e) {
			throw new ParseException(e);
		}
		return null;
	}

	@Override
	public TaskInfo buildTaskInfo(Map<String, String> headers, VideoInfo videoInfo) throws BilibiliException {
		TaskInfo taskInfo = new TaskInfo();
		if (StringUtils.isNotBlank(videoInfo.getUrl())) {
			taskInfo.setUrl(videoInfo.getUrl());
		} else {
			Map<String, String> data = getM4SVideoDetail(videoInfo.getbId(), videoInfo.getcId(), videoInfo.getQuality(), headers);
			taskInfo.setUrl(data.get("url"));
		}
		taskInfo.setId(videoInfo.getId());
		taskInfo.setCoverImg(videoInfo.getCoverImg());
		taskInfo.setName(CommonUtil.clearInvalidChars(videoInfo.getTitle()));
		taskInfo.setType(videoInfo.getType());
		taskInfo.setFileType(videoInfo.getFileType());
		taskInfo.setFilePath(videoInfo.getSavePath());
		return taskInfo;
	}

	@Override
	public DefaultHttpDownloader buildDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		DefaultHttpDownloader httpDownloader = null;
		switch (taskInfo.getFileType()) {
			case FLV:
				httpDownloader = FlvHttpDownloader.newInstance(headers, taskInfo);
				break;
			case M4S:
				httpDownloader = M4SHttpDownloader.newInstance(headers, taskInfo);
				break;
			default:
				LOGGER.warn("buildDownloader fail... taskInfo = {}", taskInfo);
				break;
		}
		return httpDownloader;
	}

	private Map<String, String> getM4SVideoDetail(String bvId, int cId, int quality, Map<String, String> headers) throws BilibiliException {
		BilibiliM4SPlayUrlResponse m4SPlayUrlResponse = BilibiliClientCore.getBilibiliClient().getM4SFormatVideoPlayUrl(bvId, cId, headers);
		Map<String, String> result = new HashMap<>();
		PlayUrlM4SData data = m4SPlayUrlResponse.getData();
		List<Video> videoList = data.getDash().getVideo();
		List<Audio> audioList = data.getDash().getAudio();
		String videoUrl = "";
		for (Video video : videoList) {
			if (video.getId() == quality) {
				videoUrl = video.getBaseUrl();
				break;
			}
		}
		String audioUrl = audioList.get(0).getBaseUrl();
		result.put("url", String.join(Global.URL_SEPARATOR, videoUrl, audioUrl));
		return result;
	}

	private VideoView getVideoInfo(String id, BilibiliType type, Map<String, String> headers) throws BilibiliException {
		VideoView videoInfo = null;
		switch (type) {
			case AV:
				String bvId = ConvertUtil.Av2Bv(id);
				videoInfo = getBVVideoInfoWithM4S(bvId, headers);
				break;
			case BV:
				videoInfo = getBVVideoInfoWithM4S(id, headers);
				break;
			case SS:
				videoInfo = getSSVideoInfoWithMS4(id, headers);
				break;
			case MD:
				videoInfo = getMDVideoInfoWithMS4(id, headers);
				break;
			case EP:
				videoInfo = getEPVideoInfoWithMS4(id, headers);
				break;
		}
		return videoInfo;
	}

	private VideoView getBVVideoInfoWithM4S(String bvId, Map<String, String> headers) throws BilibiliException {
		if (headers == null) {
			headers = BilibiliHttpHeaders.getBilibiliM4sHeaders(bvId);
		} else {
			Map<String, String> biliWwwM4SHeaders = BilibiliHttpHeaders.getBilibiliM4sHeaders(bvId);
			for (Map.Entry<String, String> entry : biliWwwM4SHeaders.entrySet()) {
				headers.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		BilibiliVideoResponse videoInfoResponse = BilibiliClientCore.getBilibiliClient().getVideoInfo(bvId, headers);
		Data data = videoInfoResponse.getData();
		VideoView videoInfo = toBaseVideoInfo(data);

		videoInfo.type = this.type();
		videoInfo.fileType = FileFormatType.M4S;
		videoInfo.headers = headers;

		BilibiliM4SPlayUrlResponse m4SPlayUrlResponse = BilibiliClientCore.getBilibiliClient().getM4SFormatVideoPlayUrl(bvId, videoInfo.cId, headers);
		toM4sDetailVideoInfo(videoInfo, m4SPlayUrlResponse.getData());
		return videoInfo;
	}


	@Deprecated
	private VideoView getBVVideoInfoWithFlv(String bvId, Map<String, String> headers) throws BilibiliException {
		if (headers == null) {
			headers = BilibiliHttpHeaders.getBiliWwwFLVHeaders(bvId);
		} else {
			Map<String, String> biliWwwFLVHeaders = BilibiliHttpHeaders.getBiliWwwFLVHeaders(bvId);
			for (Map.Entry<String, String> entry : biliWwwFLVHeaders.entrySet()) {
				headers.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		BilibiliVideoResponse videoInfoResponse;
		BilibiliFlvPlayUrlResponse playUrlInfoResponse;
		videoInfoResponse = BilibiliClientCore.getBilibiliClient().getVideoInfo(bvId, headers);
		Data data = videoInfoResponse.getData();
		VideoView videoInfo = toBaseVideoInfo(data);

		videoInfo.type = this.type();
		videoInfo.fileType = FileFormatType.FLV;
		videoInfo.headers = headers;

		playUrlInfoResponse = BilibiliClientCore.getBilibiliClient().getFlvPlayUrl(bvId, videoInfo.cId, headers);
		toFlvDetailVideoInfo(videoInfo, playUrlInfoResponse.getPlayUrlFlvData());
		return videoInfo;
	}

	private VideoView toM4sDetailVideoInfo(VideoView videoInfo, PlayUrlM4SData playUrlM4SData) {
		List<String> acceptDescriptions = playUrlM4SData.getAcceptDescription();
		List<Integer> acceptQualitys = playUrlM4SData.getAcceptQuality();
		Collections.reverse(acceptDescriptions); // quality asc
		Collections.reverse(acceptQualitys);
		for (int i = 0; i < acceptDescriptions.size(); i++) {
			videoInfo.acceptDescription.put(acceptDescriptions.get(i), acceptQualitys.get(i));
		}
		Map<Integer, String> videoUrlMap = new HashMap<>();
		List<Video> videoList = playUrlM4SData.getDash().getVideo();
		List<Audio> audioList = playUrlM4SData.getDash().getAudio();
		for (Video video : videoList) {
			videoUrlMap.putIfAbsent(video.getId(), video.getBaseUrl());
		}
		videoInfo.dashVideoMap = videoUrlMap;
		videoInfo.audioUrl = audioList.get(0).getBaseUrl();

		return videoInfo;
	}

	private void toFlvDetailVideoInfo(VideoView videoInfo, PlayUrlFlvData playUrlFlvData) {
		List<String> acceptDescriptions = playUrlFlvData.getAcceptDescription();
		List<Integer> acceptQualitys = playUrlFlvData.getAcceptQuality();
		for (int i = 0; i < acceptDescriptions.size(); i++) {
			videoInfo.acceptDescription.put(acceptDescriptions.get(i), acceptQualitys.get(i));
		}
		List<SubVideoView> subVideoInfos = videoInfo.subVideoInfos;
		for (int i = 0; i < playUrlFlvData.getDurl().size(); i++) {
			subVideoInfos.get(i).url = playUrlFlvData.getDurl().get(i).getUrl();
		}
	}

	private VideoView toBaseVideoInfo(Data data) {
		VideoView videoInfo = new VideoView();
		videoInfo.id = data.getBvid();
		videoInfo.cId = data.getCid();
		videoInfo.title = data.getTitle();
		videoInfo.description = data.getDesc();
		videoInfo.preViewUrl = data.getPic();
		videoInfo.author = data.getOwner().getName();
		List<SubVideoView> list = new ArrayList<>();
		for (Page page : data.getPages()) {
			SubVideoView subVideoInfo = new SubVideoView();
			subVideoInfo.bvId = data.getBvid();
			subVideoInfo.cid = page.getCid();
			subVideoInfo.name = page.getPart();
			list.add(subVideoInfo);
		}
		videoInfo.subVideoInfos = list;
		return videoInfo;
	}

	private VideoView getSSVideoInfoWithMS4(String ssId, Map<String, String> headers) throws BilibiliException {
		headers = handleHeaders(headers);
		BilibiliSeasonResponse seasonResponse = BilibiliClientCore.getBilibiliClient().getSeason(ssId, headers);
		List<Episode> episodes = seasonResponse.getResult().getMainSection().getEpisodes();
		VideoView videoInfo = toBaseVideoInfo(episodes);

		videoInfo.type = this.type();
		videoInfo.fileType = FileFormatType.M4S;

		BilibiliM4SPlayUrlResponse m4SPlayUrlResponse = BilibiliClientCore.getBilibiliClient().getM4SFormatVideoPlayUrl(videoInfo.id, videoInfo.cId, headers);
		toM4sDetailVideoInfo(videoInfo, m4SPlayUrlResponse.getData());

		Map<String, String> biliWwwM4SHeaders = BilibiliHttpHeaders.getBilibiliM4sHeaders(videoInfo.id);
		biliWwwM4SHeaders.putIfAbsent("cookie", headers.get("cookie"));
		videoInfo.headers = biliWwwM4SHeaders;

		BilibiliVideoResponse videoInfoResponse = BilibiliClientCore.getBilibiliClient().getVideoInfo(videoInfo.id, headers);
		Data data = videoInfoResponse.getData();
		videoInfo.title = data.getTitle();
		videoInfo.description = data.getDesc();
		videoInfo.author = data.getOwner().getName();
		videoInfo.preViewUrl = data.getPic();
		return videoInfo;
	}

	private Map<String, String> handleHeaders(Map<String, String> headers) {
		if (headers == null) {
			headers = BilibiliHttpHeaders.getCommonHeaders();
		} else {
			Map<String, String> biliWwwM4SHeaders = BilibiliHttpHeaders.getCommonHeaders();
			for (Map.Entry<String, String> entry : biliWwwM4SHeaders.entrySet()) {
				headers.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		return headers;
	}

	private VideoView toBaseVideoInfo(List<Episode> episodes) {
		VideoView videoInfo = new VideoView();
		List<SubVideoView> subVideoInfos = new ArrayList<>();
		Episode episode1 = episodes.get(0);
		if (StringUtils.isNotBlank(episode1.getBvId())) {
			videoInfo.id = episode1.getBvId();
		} else {
			videoInfo.id = ConvertUtil.Av2Bv(episode1.getAid());
		}
		videoInfo.preViewUrl = episode1.getCover();
		videoInfo.cId = episode1.getCid();

		for (Episode episode : episodes) {
			SubVideoView subVideoView = new SubVideoView();
			if (StringUtils.isNotBlank(episode.getBvId())) {
				subVideoView.bvId = episode.getBvId();
			} else {
				subVideoView.bvId = ConvertUtil.Av2Bv(episode.getAid());
			}
			subVideoView.cid = episode.getCid();
			subVideoView.name = episode.getTitle() + "_" + episode.getLongTitle();
			subVideoInfos.add(subVideoView);
		}
		videoInfo.subVideoInfos = subVideoInfos;
		return videoInfo;
	}

	private VideoView getMDVideoInfoWithMS4(String mdId, Map<String, String> headers) throws BilibiliException {
		headers = handleHeaders(headers);
		BilibiliMediaResponse mediaResponse = BilibiliClientCore.getBilibiliClient().getMedia(mdId, headers);
		Media media = mediaResponse.getResult().getMedia();
		VideoView videoInfo = new VideoView();
		videoInfo.title = media.getTitle();
		videoInfo.preViewUrl = media.getCover();
		return getSSVideoInfoWithMS4(String.valueOf(media.getSeasonID()), videoInfo, headers);
	}

	private VideoView getSSVideoInfoWithMS4(String ssId, VideoView videoInfo, Map<String, String> headers) throws BilibiliException {
		VideoView ssVideoInfo = getSSVideoInfoWithMS4(ssId, headers);
		ssVideoInfo.title = videoInfo.title;
		ssVideoInfo.preViewUrl = videoInfo.preViewUrl;
		return ssVideoInfo;
	}

	private VideoView getEPVideoInfoWithMS4(String epId, Map<String, String> headers) throws BilibiliException {
		headers = handleHeaders(headers);
		String url = "https://www.bilibili.com/bangumi/play/ep" + epId;
		String html = HttpUtil.doGet(url, headers);
		int begin = html.indexOf("window.__INITIAL_STATE__=");
		int end = html.indexOf(";(function()", begin);
		String json = html.substring(begin + 25, end);
		LOGGER.debug("json = {}", json);
		JSONObject jsonObject = new JSONObject(json);
		VideoView videoInfo = toBaseVideoInfo(jsonObject, headers);
		videoInfo.type = this.type();
		videoInfo.fileType = FileFormatType.M4S;
		return videoInfo;
	}

	private VideoView toBaseVideoInfo(JSONObject jsonObject, Map<String, String> headers) throws BilibiliException {
		VideoView videoInfo = new VideoView();
		List<SubVideoView> subVideoInfos = new ArrayList<>();
		JSONObject epInfo = jsonObject.getJSONObject("epInfo");
		videoInfo.id = epInfo.getString("bvid");
		videoInfo.cId = epInfo.getInt("cid");
		JSONObject mediaInfo = jsonObject.getJSONObject("mediaInfo");
		videoInfo.title = mediaInfo.getString("title");
		videoInfo.preViewUrl = "https:" + mediaInfo.getString("cover");
		videoInfo.description = mediaInfo.getString("evaluate");
		JSONArray epList = jsonObject.getJSONArray("epList");
		for (int i = 0; i < epList.length(); i++) {
			SubVideoView subVideoView = new SubVideoView();
			JSONObject epObject = epList.getJSONObject(i);
			subVideoView.name = epObject.getString("titleFormat") + "_" + epObject.getString("longTitle");
			subVideoView.bvId = epObject.getString("bvid");
			subVideoView.cid = epObject.getInt("cid");
			subVideoInfos.add(subVideoView);
		}
		videoInfo.subVideoInfos = subVideoInfos;

		// get acceptDescription
		Map<String, String> biliWwwM4SHeaders = BilibiliHttpHeaders.getBilibiliM4sHeaders(videoInfo.id);
		biliWwwM4SHeaders.putIfAbsent("cookie", headers.get("cookie"));
		videoInfo.headers = biliWwwM4SHeaders;
		BilibiliM4SPlayUrlResponse m4SFormatVideoPlayUrl = BilibiliClientCore.getBilibiliClient()
													.getM4SFormatVideoPlayUrl(videoInfo.id, videoInfo.cId, biliWwwM4SHeaders);
		toM4sDetailVideoInfo(videoInfo, m4SFormatVideoPlayUrl.getData());
		return videoInfo;
	}

}

