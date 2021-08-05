package org.lin.http.bilibili.response;


import org.lin.http.bilibili.model.SSResult;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/3
 */
public class BilibiliSeasonResponse extends GenericResponse {

	private SSResult result;

	public SSResult getResult() {
		return result;
	}

	public void setResult(SSResult result) {
		this.result = result;
	}
}
