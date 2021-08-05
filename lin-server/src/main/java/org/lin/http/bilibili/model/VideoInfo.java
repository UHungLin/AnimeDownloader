package org.lin.http.bilibili.model;


import java.util.List;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/4
 */
public class VideoInfo {

	private String id;
	private String title;
	private String cover;
	private int quality;
	private String description;
	private List<String> acceptDescription;
	private List<Integer> acceptQuality;
	private Owner owner;
	private List<Episode> episodes;
	private List<Dash> dashes;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	public List<Episode> getEpisodes() {
		return episodes;
	}

	public void setEpisodes(List<Episode> episodes) {
		this.episodes = episodes;
	}

	public List<Dash> getDashes() {
		return dashes;
	}

	public void setDashes(List<Dash> dashes) {
		this.dashes = dashes;
	}
}
