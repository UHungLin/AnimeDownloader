package org.lin.http.bilibili.model;


import com.google.gson.annotations.SerializedName;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/4
 */
public class Episode {

	private int id;
	private String bvId;
	private int aid;
	private int cid;
	// status = 2 普通 status = 13 大会员
	private int status;
	private String badge;
	private String title;
	@SerializedName("long_title")
	private String longTitle;
	private String cover;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBvId() {
		return bvId;
	}

	public void setBvId(String bvId) {
		this.bvId = bvId;
	}

	public int getAid() {
		return aid;
	}

	public void setAid(int aid) {
		this.aid = aid;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLongTitle() {
		return longTitle;
	}

	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}
}
