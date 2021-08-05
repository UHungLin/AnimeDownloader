package org.lin.http.bilibili.response;


import org.lin.http.bilibili.model.Data;

/**
 * @author Lin =￣ω￣=
 * @date 2020/7/31
 */
public class BilibiliVideoResponse extends GenericResponse {

	private Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
}
