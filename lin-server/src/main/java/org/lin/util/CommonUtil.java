package org.lin.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/14
 */
public class CommonUtil {

	private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private static final Pattern FILEPATTERN = Pattern.compile("[\\\\/:*?\"\'\\s+<>|]");


	public static String IDGenerator() {
		String time = DATEFORMAT.format(new Date());
		String randomStr = RandomStringUtils.randomNumeric(6);
		return time + randomStr;
	}

	public static String clearInvalidChars(String content) {
		content = FILEPATTERN.matcher(content).replaceAll("");
		while (content.startsWith("-") || content.startsWith("#")) { // linux invalid char
			content = content.substring(1);
//			System.out.println(content);
		}
		return content;
	}

	public static void main(String[] args) {
		System.out.println(IDGenerator());
	}

}
