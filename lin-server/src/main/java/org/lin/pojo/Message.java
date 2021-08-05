package org.lin.pojo;

import java.util.Map;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/30
 */
public class Message {

	private String id;
	private int type;
	private int status;
	private String msg;
	private Map<String, Object> data;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Message{" +
				"id='" + id + '\'' +
				", type=" + type +
				", status=" + status +
				", msg='" + msg + '\'' +
				", data=" + data +
				'}';
	}
}
