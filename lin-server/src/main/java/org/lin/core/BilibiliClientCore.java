package org.lin.core;


import org.lin.http.bilibili.BilibiliApiClient;

/**
 * @author Lin =￣ω￣=
 * @date 2020/7/31
 */
public class BilibiliClientCore {

	private static BilibiliApiClient bilibiliClient = null;

	public static void init() {
		bilibiliClient = new BilibiliApiClient();
	}

	public static BilibiliApiClient getBilibiliClient() {
		return bilibiliClient;
	}


}
