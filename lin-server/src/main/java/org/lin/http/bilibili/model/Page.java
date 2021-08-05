package org.lin.http.bilibili.model;


/**
 * @author Lin =￣ω￣=
 * @date 2020/8/1
 */
public class Page {

	private int cid;
	// title
	private int page;
	// long_title
	private String part;

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}
}
