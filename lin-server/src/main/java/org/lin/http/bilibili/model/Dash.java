package org.lin.http.bilibili.model;


import java.util.List;

/**
 * @author Lin =￣ω￣=
 * @date 2020/8/1
 */
public class Dash {

	private List<Video> video;
	private List<Audio> audio;

	public List<Video> getVideo() {
		return video;
	}

	public void setVideo(List<Video> video) {
		this.video = video;
	}

	public List<Audio> getAudio() {
		return audio;
	}

	public void setAudio(List<Audio> audio) {
		this.audio = audio;
	}
}
