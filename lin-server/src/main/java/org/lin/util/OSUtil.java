package org.lin.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Locale;

/**
 * @author Lin =￣ω￣=
 * @date 2021/8/1
 */
public class OSUtil {

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ROOT);

	public static boolean isWindows() {
		return OS_NAME.startsWith("win");
	}

	public static boolean isMac() {
		return OS_NAME.startsWith("mac");
	}

	public static boolean isBusyPort(int port) {
		boolean result = true;
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			result = false;
		} catch (IOException e) {
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

}
