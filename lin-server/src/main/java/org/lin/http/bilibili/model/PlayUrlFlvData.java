package org.lin.http.bilibili.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Lin =￣ω￣=
 * @date 2020/8/31
 */
public class PlayUrlFlvData {

	private int quality;
	private String format;
	@SerializedName("accept_description")
	private List<String> acceptDescription;
	@SerializedName("accept_quality")
	private List<Integer> acceptQuality;
	private List<Durl> durl;


	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public List<String> getAcceptDescription() {
		return acceptDescription;
	}

	public void setAcceptDescription(List<String> acceptDescription) {
		this.acceptDescription = acceptDescription;
	}

	public List<Integer> getAcceptQuality() {
		return acceptQuality;
	}

	public void setAcceptQuality(List<Integer> acceptQuality) {
		this.acceptQuality = acceptQuality;
	}

	public List<Durl> getDurl() {
		return durl;
	}

	public void setDurl(List<Durl> durl) {
		this.durl = durl;
	}
}
