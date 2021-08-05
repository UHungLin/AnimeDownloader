package org.lin.http.bilibili.model;


import java.util.List;

/**
 * @author Lin =￣ω￣=
 * @date 2020/7/31
 */
public class Data {

	private String bvid;
	private Integer aid;
	private Integer videos; // 视频数量
	private String pic;
	private String title;
	private String desc;
	private Owner owner;
	private Integer cid;
	private List<Page> pages;


	public String getBvid() {
		return bvid;
	}

	public void setBvid(String bvid) {
		this.bvid = bvid;
	}

	public Integer getAid() {
		return aid;
	}

	public void setAid(Integer aid) {
		this.aid = aid;
	}

	public Integer getVideos() {
		return videos;
	}

	public void setVideos(Integer videos) {
		this.videos = videos;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	public Integer getCid() {
		return cid;
	}

	public void setCid(Integer cid) {
		this.cid = cid;
	}

	public List<Page> getPages() {
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}
}


