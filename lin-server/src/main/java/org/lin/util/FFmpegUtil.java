package org.lin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/12
 */
public class FFmpegUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegUtil.class);

	private static final String FFMPEG_PATH = "ffmpeg";

//	static {
//		FFMPEG_PATH = OSUtil.isWindows() ?
//				new File(System.getProperty("user.dir")) + File.separator + "ffmpeg.exe" :
//				"ffmpeg";
//	}

	public static boolean run(String command) {
		Process process = null;
		InputStream errorStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader br = null;
		try {
			process = Runtime.getRuntime().exec(command);
			errorStream = process.getErrorStream();
			inputStreamReader = new InputStreamReader(errorStream);
			br = new BufferedReader(inputStreamReader);

			String str = "";
			while ((str = br.readLine()) != null) {
				LOGGER.debug(str);
			}
			process.waitFor();
			process.destroy();
			process.destroyForcibly();
			process.exitValue();
			System.out.println("process 执行完毕");
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			LOGGER.error(e.toString());
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
				}
			}
			if (errorStream != null) {
				try {
					errorStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static boolean convert(String filePath, String videoFilePath, String audioFilePath) {
		String command = createConvertCmd(filePath, videoFilePath, audioFilePath);
		File mp4File = new File(filePath);
		if (!mp4File.exists()) {
			LOGGER.info("The download is complete, and the merging video {} and audio {} ...", videoFilePath, audioFilePath);
			run(command);
			if (mp4File.exists()) {
				LOGGER.info("merge finish");
				return true;
			}
		} else {
			LOGGER.info("skip merge");
			return true;
		}
		return false;
	}

	public static String createConvertCmd(String filePath, String videoFilePath, String audioFilePath) {
//		String cmd[] = { FFMPEG_PATH, "-i", videoFilePath, "-i", audioFilePath,
//				"-c:v copy -c:a aac -strict experimental -map 0:v:0 -map 1:a:0 -y",
//				filePath };
//		String str = String.format("ffmpeg: \r\n%s -i %s -i %s -c copy %s", FFMPEG_PATH,
//				videoFilePath, audioFilePath, filePath);
//		LOGGER.info(str);
		String command = FFMPEG_PATH + " -i " + videoFilePath + " -i " + audioFilePath
				+ " -c copy " + filePath;
		LOGGER.info(command);
		return command;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(FFMPEG_PATH);
		System.out.println(Runtime.getRuntime().exec(FFMPEG_PATH));

	}

}
