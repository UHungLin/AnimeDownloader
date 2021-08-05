package org.lin.pojo.entity;

import org.lin.constant.FileFormatType;
import org.lin.constant.HttpDownStatus;
import org.lin.downloader.DefaultHttpDownloader;
import org.lin.parser.AbstractParser.Type;
import org.lin.parser.ParserManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/25
 */
public class TaskInfo {

	private String id;
	private String coverImg;
	private String name;
	private String url;
//	@JsonProperty("type")
	private Type type;
//	@JsonProperty("fileType")
	private FileFormatType fileType;
	private String filePath;
	private AtomicLong currentOffset = new AtomicLong(0);
	private AtomicLong totalSize = new AtomicLong(0);
	private int status;
	private long speed;
	private int connectionCount;
	private List<ChunkInfo> chunkInfoList;

	public DefaultHttpDownloader buildDownloader(Map<String, String> headers) {
		return ParserManager.getInstance().buildDownloader(this, headers);
	}

	public void addTotalSize(long size) {
		totalSize.addAndGet(size);
	}

	public void increaseSize(long increaseBytes) {
		this.currentOffset.addAndGet(increaseBytes);
	}

	public boolean pause() {
		return status == HttpDownStatus.PAUSE.getStatus();
	}

	public boolean downloading() {
		return status == HttpDownStatus.DOWNLOADING.getStatus();
	}

	public boolean waiting() {
		return status == HttpDownStatus.WAITING.getStatus();
	}

	public boolean complete() {
		return status == HttpDownStatus.COMPLETE.getStatus();
	}

	public boolean error() {
		return status == HttpDownStatus.FAIL.getStatus();
	}

	public boolean isBilibiliType() {
		return type == Type.BILIBILI;
	}

	public boolean isM4SFileType() {
		return fileType == FileFormatType.M4S;
	}

	public boolean isMP4FileType() {
		return fileType == FileFormatType.MP4;
	}

	public boolean isM3U8FileType() {
		return fileType == FileFormatType.M3U8;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCoverImg() {
		return coverImg;
	}

	public void setCoverImg(String coverImg) {
		this.coverImg = coverImg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public FileFormatType getFileType() {
		return fileType;
	}

	public void setFileType(FileFormatType fileType) {
		this.fileType = fileType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public long getCurrentOffset() {
		return currentOffset.get();
	}

	public void setCurrentOffset(long currentOffset) {
		this.currentOffset = new AtomicLong(currentOffset);
	}

	public void setTotalSize(AtomicLong totalSize) {
		this.totalSize = totalSize;
	}

	public long getTotalSize() {
		return totalSize.get();
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = new AtomicLong(totalSize);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getSpeed() {
		return speed;
	}

	public void setSpeed(long speed) {
		this.speed = speed;
	}

	public int getConnectionCount() {
		return connectionCount;
	}

	public void setConnectionCount(int connectionCount) {
		this.connectionCount = connectionCount;
	}

	public List<ChunkInfo> getChunkInfoList() {
		return chunkInfoList;
	}

	public void setChunkInfoList(List<ChunkInfo> chunkInfoList) {
		this.chunkInfoList = chunkInfoList;
	}

	@Override
	public String toString() {
		return "TaskInfo{" +
				"name='" + name + '\'' +
				", url='" + url + '\'' +
				", type=" + type +
				", fileType=" + fileType +
				", filePath='" + filePath + '\'' +
				", totalSize=" + totalSize +
				", status=" + status +
				'}';
	}
}
