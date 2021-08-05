package org.lin.parser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lin.annotation.Parser;
import org.lin.constant.FileFormatType;
import org.lin.constant.ImomoeLaType;
import org.lin.downloader.DefaultHttpDownloader;
import org.lin.downloader.M3U8HttpDownloader;
import org.lin.downloader.MP4HttpDownloader;
import org.lin.pojo.entity.TaskInfo;
import org.lin.pojo.entity.VideoInfo;
import org.lin.exception.ParseException;
import org.lin.http.bilibili.BilibiliException;
import org.lin.http.view.SubVideoView;
import org.lin.http.view.VideoView;
import org.lin.util.CommonUtil;
import org.lin.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lin.constant.ImomoeLaType.PLAYER;
import static org.lin.constant.ImomoeLaType.VIEW;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/25
 */
@Parser(name = "imomoe_la")
public final class ImomoeLaParser extends AbstractParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImomoeLaParser.class);

	private static final Pattern BANGUMIVIEWPATTERN = Pattern.compile("http?://(www\\.)?imomoe\\.la/view/[0-9]+\\.html");
	private static final Pattern BANGUMIPLAYERPATTERN = Pattern.compile("http?://(www\\.)?imomoe\\.la/player/[0-9]+-[0-9]+-[0-9]+\\.html");

	private static final Pattern BDS_CONFIG_PATTERN = Pattern.compile("var bds_config = (\\{.*?\\});");
	private static final Pattern MMLETV_PATTERN = Pattern.compile("var video =  \\'(.*?)\\' ;");

	private static final String HOST = "http://www.imomoe.la";
	private static final String PVOD_TYPE_VIDEO_URL = "https://v.jialingmm.net/mmletv/mms.php?vid=%s&type=letv";
	private static final String SINA_TYPE_VIDEO_URL = "https://api.xiaomingming.org/cloud/sina.php?vid=%s";

	private static final Map<String, String> DEAFULTHEADERS = new HashMap<>();
	static {
		DEAFULTHEADERS.put("referer", "http://www.imomoe.la/");
		DEAFULTHEADERS.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83");
	}

	public ImomoeLaParser() {
		super(Type.IMOMOE_LA);
	}

	@Override
	public VideoView parse(String url, Map<String, String> headers) throws ParseException {
		LOGGER.info("ImomoeLaParser parsing...");
		Matcher matcher = null;
		if ((matcher = BANGUMIVIEWPATTERN.matcher(url)).find()) {
			return getVideoInfo(url, VIEW);
		} else if ((matcher = BANGUMIPLAYERPATTERN.matcher(url)).find()) {
			return getVideoInfo(url, ImomoeLaType.PLAYER);
		}
		return null;
	}

	@Override
	public TaskInfo buildTaskInfo(Map<String, String> headers, VideoInfo videoInfo) throws BilibiliException {
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.setName(CommonUtil.clearInvalidChars(videoInfo.getTitle()));
		taskInfo.setUrl(videoInfo.getUrl());
		taskInfo.setCoverImg(videoInfo.getCoverImg());
		taskInfo.setId(videoInfo.getId());
		taskInfo.setType(videoInfo.getType());
		taskInfo.setFileType(videoInfo.getFileType());
		taskInfo.setFilePath(videoInfo.getSavePath());
		return taskInfo;
	}

	@Override
	public DefaultHttpDownloader buildDownloader(Map<String, String> headers, TaskInfo taskInfo) {
		DefaultHttpDownloader httpDownloader = null;
		if (taskInfo.isM3U8FileType()) {
			httpDownloader = M3U8HttpDownloader.newInstance(headers, taskInfo);
		} else {
			httpDownloader = MP4HttpDownloader.newInstance(headers, taskInfo);
		}
		return httpDownloader;
	}


	private VideoView getVideoInfo(String url, ImomoeLaType type) {
		VideoView videoInfo = null;
		switch (type) {
			case VIEW:
				videoInfo = getVideoInfoByUrl(url, VIEW);
				break;
			case PLAYER:
				videoInfo = getVideoInfoByUrl(url, PLAYER);
				break;
		}
		return videoInfo;
	}

	private VideoView getVideoInfoByUrl(String url, ImomoeLaType type) {
		String htmlContent = HttpUtil.doGet(url, DEAFULTHEADERS, "GB2312");
		Matcher matcher = BDS_CONFIG_PATTERN.matcher(htmlContent);
		if (matcher.find()) {
			VideoView videoView = new VideoView();
			String jsonStr = matcher.group(1);
			System.out.println("jsonStr = " + jsonStr);
			JSONObject jsonObject = new JSONObject(jsonStr);
			String title = jsonObject.getString("bdText").split(" ")[0];
			if (title.startsWith("#"))
				title = title.substring(1);
			if (title.endsWith("#"))
				title = title.substring(0, title.length() - 1);
			String cover = jsonObject.getString("bdPic");
			videoView.title = title;
			videoView.preViewUrl = cover;

			Document parse = Jsoup.parse(htmlContent);
			Element play0Element = parse.getElementById("play_0");
			Elements aTags = play0Element.getElementsByTag("a");
			getVideoPlayUrl(HOST + aTags.get(0).attr("href"), htmlContent, videoView, type);

			videoView.type = this.type();
			videoView.headers = DEAFULTHEADERS;
			return videoView;
		}
		return null;
	}

	private void getVideoPlayUrl(String url, String htmlContent, VideoView videoView, ImomoeLaType type) {
		String html = null;
		if (type == PLAYER) {
			html = htmlContent;
		} else {
			html = HttpUtil.doGet(url, DEAFULTHEADERS);
		}
		Element jsoupHtml = Jsoup.parse(html);
		Elements select = jsoupHtml.select("script[src^=/playdata]");
		String jsUrl = HOST + select.attr("src");
		LOGGER.debug("jsUrl = {}", jsUrl);
		String videoListContent = HttpUtil.doGet(jsUrl, DEAFULTHEADERS, "GB2312");
		int begin = videoListContent.indexOf("var VideoListJson=");
		int end = videoListContent.lastIndexOf(",urlinfo");
		String jsonStr = videoListContent.substring(begin + 18, end);
		JSONArray jsonArray = new JSONArray(jsonStr).getJSONArray(0).getJSONArray(1);
		List<SubVideoView> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			String playUrl = jsonArray.getString(i);
			SubVideoView subVideoView = new SubVideoView();
			subVideoView.cid = i;
			subVideoView.name = videoView.title + "-" + playUrl.split("\\$")[0];
			if (playUrl.contains(VideoSuffix.M3U8.suffix) || playUrl.contains(VideoSuffix.BDHD.suffix)) {
				videoView.fileType = FileFormatType.M3U8;
				String url_ = playUrl.split("\\$")[1];
				subVideoView.url = url_.split("\\$")[0];
			} else if (playUrl.contains(VideoSuffix.PVOD.suffix)) {
				videoView.fileType = FileFormatType.MP4;
				String url_ = playUrl.split("\\$")[1];
				url_ = String.format(PVOD_TYPE_VIDEO_URL, url_.split("\\$")[0]);
				String mmletvHtml = HttpUtil.doGet(url_, DEAFULTHEADERS);
				Matcher matcher = MMLETV_PATTERN.matcher(mmletvHtml);
				if (matcher.find()) {
					subVideoView.url = matcher.group(1);
				}
			} else if (playUrl.contains(VideoSuffix.SINA.suffix)) {
				videoView.fileType = FileFormatType.MP4;
				String url_ = playUrl.split("\\$")[1];
				url_ = String.format(SINA_TYPE_VIDEO_URL, url_.split("\\$")[0]);
				String mmletvHtml = HttpUtil.doGet(url_, DEAFULTHEADERS);
				Matcher matcher = MMLETV_PATTERN.matcher(mmletvHtml);
				if (matcher.find()) {
					subVideoView.url = matcher.group(1);
				}
			} else {
				String url_ = playUrl.split("\\$")[1];
				videoView.fileType = FileFormatType.MP4;
				subVideoView.url = url_;
			}

			list.add(subVideoView);
		}
		videoView.subVideoInfos = list;
	}


	private enum VideoSuffix {
		M3U8("m3u8"), FLV("flv"), PVOD("pvod"), SINA("sina"), BDHD("bdhd");

		private String suffix;

		VideoSuffix(String suffix) {
			this.suffix = suffix;
		}
	}

}

