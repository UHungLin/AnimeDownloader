package org.lin.config;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/25
 */
public final class ApiConfig {

	// bilibili api
	public static final String BILIBILI_VIEW = "https://api.bilibili.com/x/web-interface/view?bvid=%s";
	public static final String BILIBILI_PLAYURL = "https://api.bilibili.com/x/player/playurl?bvid=%s&cid=%s&fnval=%s&qn=%s&type=&otype=json&fnver=0&fourk=1";
	public static final String BILIBILI_MEDIA = "https://api.bilibili.com/pgc/review/user?media_id=%s";
	public static final String BILIBILI_SEASON = "https://api.bilibili.com/pgc/web/season/section?season_id=%s";
	public static final String BILIBILI_PGCPLAYURL = "https://api.bilibili.com/pgc/player/web/playurl?cid=&qn=%d&bvid=&ep_id=%s&fourk=&fnval=%d";
	public static final String BILIBILI_USERINFO = "https://api.bilibili.com/x/web-interface/nav";

}
