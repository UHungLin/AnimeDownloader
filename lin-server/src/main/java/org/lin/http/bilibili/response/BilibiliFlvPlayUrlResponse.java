package org.lin.http.bilibili.response;


import org.lin.http.bilibili.model.PlayUrlFlvData;

/**
 * @author Lin =￣ω￣=
 * @date 2020/8/31
 */
public class BilibiliFlvPlayUrlResponse extends GenericResponse {

	private PlayUrlFlvData data;

	public PlayUrlFlvData getPlayUrlFlvData() {
		return data;
	}

	public void setPlayUrlFlvData(PlayUrlFlvData playUrlFlvData) {
		this.data = playUrlFlvData;
	}
}
