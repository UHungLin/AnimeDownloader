package org.lin.pojo.entity;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/25
 */
public class ChunkInfo {

	private int index;
	private long startOffset;
	private long endOffset;
	private long currentOffset;
	private int status;
	private long speed;

	public void increaseSize(long increaseBytes) {
		this.currentOffset += increaseBytes;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(long startOffset) {
		this.startOffset = startOffset;
	}

	public long getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(long endOffset) {
		this.endOffset = endOffset;
	}

	public long getCurrentOffset() {
		return currentOffset;
	}

	public void setCurrentOffset(long currentOffset) {
		this.currentOffset = currentOffset;
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

	@Override
	public String toString() {
		return "ChunkInfo{" +
				"index=" + index +
				", startOffset=" + startOffset +
				", endOffset=" + endOffset +
				", currentOffset=" + currentOffset +
				", status=" + status +
				", speed=" + speed +
				'}';
	}
}
