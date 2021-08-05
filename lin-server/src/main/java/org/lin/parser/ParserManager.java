package org.lin.parser;

import org.lin.annotation.Parser;
import org.lin.downloader.DefaultHttpDownloader;
import org.lin.exception.ParseException;
import org.lin.http.bilibili.BilibiliException;
import org.lin.http.view.VideoView;
import org.lin.pojo.entity.TaskInfo;
import org.lin.pojo.entity.VideoInfo;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/4
 */
public class ParserManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParserManager.class);

	private static final ParserManager INSTANCE = new ParserManager();

	public static ParserManager getInstance() {
		return INSTANCE;
	}

	private static final String PARSER_PACKAGE_NAME = "org.lin.parser";
	private List<AbstractParser> parserList = new ArrayList<>();

	private ParserManager() {}

	public void init() {
		LOGGER.info("loadParser...");
		List<AbstractParser> tempParserList = new ArrayList<>();
		Reflections f = new Reflections(PARSER_PACKAGE_NAME);
		Set<Class<?>> set = f.getTypesAnnotatedWith(Parser.class);
		ArrayList<Class<?>> classes = new ArrayList<>(set);

		classes.stream().sorted(Comparator.comparingInt(c -> c.getAnnotation(Parser.class).weight()))
				.forEach(c -> {
					Object o = null;
					try {
						o = c.newInstance();
						LOGGER.debug("instance: " + o.getClass().getName());
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
					}
					tempParserList.add((AbstractParser) o);
				});
		parserList = tempParserList;
		LOGGER.info("loadParser finish...");
	}

	public VideoView parse(String url, Map<String, String> headers) throws ParseException {
		LOGGER.info("parse {}", url);
		VideoView result = null;
		for (AbstractParser parser : parserList) {
			if (parser.matchParser(url)) {
				result = parser.parse(url, headers);
			}
		}
		return result;
	}

	public TaskInfo buildTaskInfo(Map<String, String> headers, VideoInfo videoInfo) throws BilibiliException {
		for (AbstractParser parser : parserList) {
			if (parser.matchParser(videoInfo.getType())) {
				return parser.buildTaskInfo(headers, videoInfo);
			}
		}
		return null;
	}

	public DefaultHttpDownloader buildDownloader(TaskInfo taskInfo, Map<String, String> headers) {
		for (AbstractParser parser : parserList) {
			if (parser.type() == taskInfo.getType()) {
				return parser.buildDownloader(headers, taskInfo);
			}
		}
		return null;
	}

}

