package org.lin.http.bilibili.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Lin =￣ω￣=
 * @date 2020/7/31
 */
public class PlayUrlM4SData {

	private int quality;
	private String format;
	@SerializedName("accept_description")
	private List<String> acceptDescription;
	@SerializedName("accept_quality")
	private List<Integer> acceptQuality;
	private Dash dash;


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

	public Dash getDash() {
		return dash;
	}

	public void setDash(Dash dash) {
		this.dash = dash;
	}
}


