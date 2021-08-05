package org.lin.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.lin.downloader.DefaultHttpDownloader;
import org.lin.pojo.entity.TaskInfo;
import org.lin.pojo.entity.VideoInfo;
import org.lin.exception.ParseException;
import org.lin.http.bilibili.BilibiliException;
import org.lin.http.view.VideoView;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/24
 */
public abstract class AbstractParser {

	public abstract VideoView parse(String url, Map<String, String> headers) throws ParseException;

	public boolean matchParser(String url) {
		if (StringUtils.isNotBlank(url)) {
			return this.type.verify(url);
		}
		return false;
	}

	public boolean matchParser(Type type) {
		return this.type == type;
	}

	public enum Type {

		@JsonProperty("BILIBILI")
		BILIBILI(new Pattern[]{Pattern.compile("https?://(www\\.)?bilibili\\.com"),
			Pattern.compile("av(\\d+)"),
			Pattern.compile("BV(\\S+)"),
			Pattern.compile("ss(\\d+)"),
			Pattern.compile("md(\\d+)"),
			Pattern.compile("ep(\\d+)")
		}),

		@JsonProperty("ACFUN")
		ACFUN(new Pattern[]{Pattern.compile("https?://(www\\.)?acfun\\.cn"),
			Pattern.compile("ac([0-9]+)"),
			Pattern.compile("aa([0-9]+)")
		}),

		@JsonProperty("IMOMOE_LA")
		IMOMOE_LA(new Pattern[]{Pattern.compile("http?://(www\\.)?imomoe\\.la/view/([0-9]+)\\.html"),
			Pattern.compile("http?://(www\\.)?imomoe\\.la/player/[0-9]+-[0-9]+-[0-9]+\\.html")
		});

		public boolean verify(String url) {
			for (Pattern type : types) {
				if (type.matcher(url).find()) {
					return true;
				}
			}
			return false;
		}

		private Pattern[] types;

		Type(Pattern[] types) {
			this.types = types;
		}
	}

	private Type type;

	public AbstractParser(Type type) {
		this.type = type;
	}

	protected Type type() {
		return type;
	}

	public abstract TaskInfo buildTaskInfo(Map<String, String> headers, VideoInfo videoInfo) throws BilibiliException;

	public abstract DefaultHttpDownloader buildDownloader(Map<String, String> headers, TaskInfo taskInfo);

}
