package org.lin.http.bilibili.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/3
 */
public class Media {

	// 番剧封面图
	private String cover;
	@SerializedName("media_id")
	private int mediaID;
	@SerializedName("season_id")
	private int seasonID;
	@SerializedName("share_url")
	private String shareUrl;
	private String title;
	@SerializedName("type_name")
	private String typeName;

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public int getMediaID() {
		return mediaID;
	}

	public void setMediaID(int mediaID) {
		this.mediaID = mediaID;
	}

	public int getSeasonID() {
		return seasonID;
	}

	public void setSeasonID(int seasonID) {
		this.seasonID = seasonID;
	}

	public String getShareUrl() {
		return shareUrl;
	}

	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
}
