package org.lin.http.bilibili.response;


import org.lin.http.bilibili.model.UserInfo;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/5
 */
public class BilibiliUserInfoResponse extends GenericResponse {

	private UserInfo data;

	public UserInfo getData() {
		return data;
	}

	public void setData(UserInfo data) {
		this.data = data;
	}
}
