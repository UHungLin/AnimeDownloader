package org.lin.parser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lin.annotation.Parser;
import org.lin.constant.AcFunType;
import org.lin.constant.FileFormatType;
import org.lin.downloader.AcFunM3U8HttpDownloader;
import org.lin.downloader.DefaultHttpDownloader;
import org.lin.pojo.entity.TaskInfo;
import org.lin.pojo.entity.VideoInfo;
import org.lin.exception.ParseException;
import org.lin.http.acfun.AcFunException;
import org.lin.http.view.SubVideoView;
import org.lin.http.view.VideoView;
import org.lin.util.CommonUtil;
import org.lin.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/21
 */
@Parser(name = "acfun")
public final class AcFunParser extends AbstractParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcFunParser.class);

	private static final Pattern ACPATTERN = Pattern.compile("ac([0-9]+)");
	private static final Pattern AAPATTERN = Pattern.compile("aa([0-9]+)");

	private static final Pattern ACHTMLPATTERN = Pattern.compile("window.pageInfo = window.videoInfo = (.*?)};");
	private static final Pattern AA_BANGUMILIST_HTMLPATTERN = Pattern.compile("window.bangumiList = (.*?);");
	private static final Pattern AA_BANGUMIDATA_HTMLPATTERN = Pattern.compile("window.pageInfo = window.bangumiData = (\\{.*?\\});");

	private static final Map<String, String> DEAFULTHEADERS = new HashMap<>();
	static {
		DEAFULTHEADERS.put("referer", "https://www.acfun.cn/");
		DEAFULTHEADERS.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83");
	}


	public AcFunParser() {
		super(Type.ACFUN);
	}

	@Override
	public VideoView parse(String url, Map<String, String> headers) throws ParseException {
		LOGGER.info("AcfunParser parsing...");
		Matcher matcher = null;
		try {
			if ((matcher = ACPATTERN.matcher(url)).find()) {
				String acId = matcher.group();
				return getVideoInfo(acId, AcFunType.AC);
			} else if ((matcher = AAPATTERN.matcher(url)).find()) {
				String aaId = matcher.group();
				return getVideoInfo(aaId, AcFunType.AA);
			}
		} catch (AcFunException e) {
			throw new ParseException(e);
		}
		return null;
	}

	@Override
	public TaskInfo buildTaskInfo(Map<String, String> headers, VideoInfo videoInfo) {
		TaskInfo taskInfo = new TaskInfo();
		if (videoInfo.getTotalSize() > 0L) { // AC type video
			taskInfo.setUrl(videoInfo.getUrl());
			taskInfo.setCoverImg(videoInfo.getCoverImg());
			taskInfo.setTotalSize(videoInfo.getTotalSize());
		} else { // AA type video
			String videoUrl = videoInfo.getUrl();
			String html = HttpUtil.doGet(videoUrl, DEAFULTHEADERS);
			VideoView videoView = toBaseAAVideoInfo(html);
//			Map<Integer, String> dashVideoMap = videoView.dashVideoMap;
			taskInfo.setUrl(videoView.dashVideoMap.get(videoInfo.getQuality()));
			taskInfo.setCoverImg(videoView.preViewUrl);
			taskInfo.setTotalSize(videoView.totalSizeMap.get(videoInfo.getQuality()));
		}
		taskInfo.setName(CommonUtil.clearInvalidChars(videoInfo.getTitle()));
		taskInfo.setId(videoInfo.getId());
		taskInfo.setType(videoInfo.getType());
		taskInfo.setFileType(videoInfo.getFileType());
		taskInfo.setFilePath(videoInfo.getSavePath());
		return taskInfo;
	}

	@Override
	public DefaultHttpDownloader buildDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		DefaultHttpDownloader httpDownloader = AcFunM3U8HttpDownloader.newInstance(headers, taskInfo);
		return httpDownloader;
	}

	private VideoView getVideoInfo(String id, AcFunType type) throws AcFunException {
		VideoView videoInfo = null;
		switch (type) {
			case AC:
				videoInfo = getACVideoInfo(id);
				break;
			case AA:
				videoInfo = getAAVideoInfo(id);
				break;
		}
		return videoInfo;
	}

	private VideoView getACVideoInfo(String id) {
		String url = "https://www.acfun.cn/v/" + id;
		String html = HttpUtil.doGet(url, DEAFULTHEADERS);
		VideoView videoView = toBaseACVideoInfo(html);
		return videoView;
	}

	private VideoView toBaseACVideoInfo(String htmlContent) {
		Matcher matcher = ACHTMLPATTERN.matcher(htmlContent);
		if (matcher.find()) {
			VideoView videoView = new VideoView();
			String jsonStr = matcher.group();
			JSONObject jsonObject = new JSONObject(jsonStr.substring(jsonStr.indexOf('{'), jsonStr.lastIndexOf(';')));
			String title = jsonObject.getString("title");
			videoView.title = title;
			String cover = jsonObject.getString("coverUrl");
			videoView.preViewUrl = cover;

			JSONObject videoInfo = jsonObject.getJSONObject("currentVideoInfo");
			String ksPlayJson = videoInfo.getString("ksPlayJson");
			JSONObject jsonObject1 = new JSONObject(ksPlayJson).getJSONArray("adaptationSet").getJSONObject(0);
//			JSONArray adaptationSet = jsonObject1.getJSONArray("adaptationSet");
//			JSONObject jsonObject2 = adaptationSet.getJSONObject(0);
			JSONArray m3u8Array = jsonObject1.getJSONArray("representation");

			Map<String, Integer> acceptDescription = new LinkedHashMap<>();
			Map<Integer, String> dashVideoMap = new HashMap<>();
			for (int i = 0; i < m3u8Array.length(); i++) {
				JSONObject jsonObject2 = m3u8Array.getJSONObject(i);
				acceptDescription.put(jsonObject2.getString("qualityLabel"), (i + 1));
				dashVideoMap.put((i + 1), jsonObject2.getString("url"));
			}
			videoView.acceptDescription = acceptDescription;
			videoView.dashVideoMap = dashVideoMap;

			Map<Integer, Long> totalSizeMap = new LinkedHashMap<>();
			JSONArray transcodeInfos = videoInfo.getJSONArray("transcodeInfos");
			for (int i = 0; i < transcodeInfos.length(); i++) {
				JSONObject jsonObject3 = transcodeInfos.getJSONObject(i);
				totalSizeMap.put(i + 1, jsonObject3.getLong("sizeInBytes"));
			}
			videoView.totalSizeMap = totalSizeMap;

			videoView.type = this.type();
			videoView.fileType = FileFormatType.M3U8;
			videoView.headers = DEAFULTHEADERS;
			return videoView;
		}
		return null;
	}

	private VideoView getAAVideoInfo(String id) {
		String url = "https://www.acfun.cn/bangumi/" + id;
		String html = HttpUtil.doGet(url, DEAFULTHEADERS);
		VideoView videoInfo = toBaseAAVideoInfo(html);
		return videoInfo;
	}

	private VideoView toBaseAAVideoInfo(String htmlContent) {
		Matcher matcher = AA_BANGUMIDATA_HTMLPATTERN.matcher(htmlContent);
		VideoView videoView = new VideoView();
		if (matcher.find()) {
			String dataJsonStr = matcher.group(1);
			JSONObject dataJsonObject = new JSONObject(dataJsonStr);
			String bangumiId = String.valueOf(dataJsonObject.getLong("bangumiId"));
			String title = dataJsonObject.getString("bangumiTitle");
			String image = dataJsonObject.getString("image");
			JSONObject currentVideoInfo = dataJsonObject.getJSONObject("currentVideoInfo");
			String ksPlayJson = currentVideoInfo.getString("ksPlayJson");
			JSONObject jsonObject1 = new JSONObject(ksPlayJson).getJSONArray("adaptationSet").getJSONObject(0);
			JSONArray m3u8Array = jsonObject1.getJSONArray("representation");

			Map<String, Integer> acceptDescription = new LinkedHashMap<>();
			Map<Integer, String> dashVideoMap = new HashMap<>();
			for (int i = 0; i < m3u8Array.length(); i++) {
				JSONObject jsonObject2 = m3u8Array.getJSONObject(i);
				acceptDescription.put(jsonObject2.getString("qualityLabel"), (i + 1));
				dashVideoMap.put((i + 1), jsonObject2.getString("url"));
			}

			Map<Integer, Long> totalSizeMap = new LinkedHashMap<>();
			JSONArray transcodeInfos = currentVideoInfo.getJSONArray("transcodeInfos");
			for (int i = 0; i < transcodeInfos.length(); i++) {
				JSONObject jsonObject3 = transcodeInfos.getJSONObject(i);
				totalSizeMap.put(i + 1, jsonObject3.getLong("sizeInBytes"));
			}

			videoView.id = bangumiId;
			videoView.title = title;
			videoView.preViewUrl = image;
			videoView.acceptDescription = acceptDescription;
			videoView.dashVideoMap = dashVideoMap;
			videoView.totalSizeMap = totalSizeMap;
		}
		matcher = AA_BANGUMILIST_HTMLPATTERN.matcher(htmlContent);
		List<SubVideoView> list = new ArrayList<>();
		if (matcher.find()) {
			String listJsonStr = matcher.group(1);
			JSONObject listJsonObject = new JSONObject(listJsonStr);
			JSONArray items = listJsonObject.getJSONArray("items");
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				SubVideoView subVideoView = new SubVideoView();
				subVideoView.cid = i;
				subVideoView.name = item.getString("episodeName") + "-" + item.getString("title");
				subVideoView.url = "https://www.acfun.cn/bangumi/aa" + item.getLong("bangumiId") + "_36188_" + item.getLong("itemId");
				list.add(subVideoView);
			}
			videoView.subVideoInfos = list;
		}
		videoView.type = this.type();
		videoView.fileType = FileFormatType.M3U8;
		videoView.headers = DEAFULTHEADERS;
		return videoView;
	}

}

