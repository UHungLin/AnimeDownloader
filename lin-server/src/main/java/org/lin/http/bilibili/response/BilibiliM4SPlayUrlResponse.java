package org.lin.http.bilibili.response;


import org.lin.http.bilibili.model.PlayUrlM4SData;

/**
 * @author Lin =￣ω￣=
 * @date 2020/7/31
 */
public class BilibiliM4SPlayUrlResponse extends GenericResponse {

	private PlayUrlM4SData data;

	public PlayUrlM4SData getData() {
		return data;
	}

	public void setData(PlayUrlM4SData data) {
		this.data = data;
	}
}
