package org.lin.http.bilibili.model;


import java.util.List;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/3
 */
public class Bangumi {

	private Media media;
	private Data data;
	private List<Episode> episodes;
	private PlayUrlM4SData playUrlM4SData;

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public List<Episode> getEpisodes() {
		return episodes;
	}

	public void setEpisodes(List<Episode> episodes) {
		this.episodes = episodes;
	}

	public PlayUrlM4SData getPlayUrlM4SData() {
		return playUrlM4SData;
	}

	public void setPlayUrlM4SData(PlayUrlM4SData playUrlM4SData) {
		this.playUrlM4SData = playUrlM4SData;
	}
}
