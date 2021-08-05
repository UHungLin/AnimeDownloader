package org.lin.http.bilibili.response;


import org.lin.http.bilibili.model.PlayUrlM4SData;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/4
 */
public class BilibiliPgcPlayUrlResponse extends GenericResponse {

	private PlayUrlM4SData result;

	public PlayUrlM4SData getResult() {
		return result;
	}

	public void setResult(PlayUrlM4SData result) {
		this.result = result;
	}
}
