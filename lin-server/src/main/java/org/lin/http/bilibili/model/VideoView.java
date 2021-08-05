package org.lin.http.bilibili.model;


/**
 * @author Lin =￣ω￣=
 * @date 2020/7/31
 */
public class VideoView {

	private Data data;
	private PlayUrlM4SData playUrlM4SData;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public PlayUrlM4SData getPlayUrlM4SData() {
		return playUrlM4SData;
	}

	public void setPlayUrlM4SData(PlayUrlM4SData playUrlM4SData) {
		this.playUrlM4SData = playUrlM4SData;
	}
}

