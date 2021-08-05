package org.lin.pojo.entity;

import org.lin.constant.FileFormatType;
import org.lin.parser.AbstractParser;
import org.lin.parser.AbstractParser.Type;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/10
 */
public class VideoInfo {

	private String id;
	private String bId; // bilibili id
	public  Integer cId;
	private String title;
	private Type type;
	private FileFormatType fileType;
	private Integer quality;
	private String url;
	private String savePath;
	private String coverImg;
	private Long totalSize;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getbId() {
		return bId;
	}

	public void setbId(String bId) {
		this.bId = bId;
	}

	public Integer getcId() {
		return cId;
	}

	public void setcId(Integer cId) {
		this.cId = cId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AbstractParser.Type getType() {
		return type;
	}

	public void setType(AbstractParser.Type type) {
		this.type = type;
	}

	public FileFormatType getFileType() {
		return fileType;
	}

	public void setFileType(FileFormatType fileType) {
		this.fileType = fileType;
	}

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getCoverImg() {
		return coverImg;
	}

	public void setCoverImg(String coverImg) {
		this.coverImg = coverImg;
	}

	public Long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(Long totalSize) {
		this.totalSize = totalSize;
	}

	@Override
	public String toString() {
		return "VideoInfo{" +
				"id='" + id + '\'' +
				", cId=" + cId +
				", title='" + title + '\'' +
				", type=" + type +
				", fileType=" + fileType +
				", quality=" + quality +
				", url='" + url + '\'' +
				", savePath='" + savePath + '\'' +
				", coverImg='" + coverImg + '\'' +
				'}';
	}
}
