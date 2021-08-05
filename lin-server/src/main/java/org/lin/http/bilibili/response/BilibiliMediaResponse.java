package org.lin.http.bilibili.response;


import org.lin.http.bilibili.model.MDResult;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/3
 */
public class BilibiliMediaResponse extends GenericResponse {

	private MDResult result;


	public MDResult getResult() {
		return result;
	}

	public void setResult(MDResult result) {
		this.result = result;
	}
}

