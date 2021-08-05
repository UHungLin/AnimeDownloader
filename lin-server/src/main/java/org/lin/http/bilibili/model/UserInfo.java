package org.lin.http.bilibili.model;


/**
 * @author Lin =￣ω￣=
 * @date 2020/9/4
 */
public class UserInfo {

	private boolean isLogin;
	private int mid;
	private String username;
	private String face;
	// vipStatus = 0 非大会员 vipStatus = 1 大会员
	private int vipStatus;

	public boolean isLogin() {
		return isLogin;
	}

	public void setLogin(boolean login) {
		isLogin = login;
	}

	public int getMid() {
		return mid;
	}

	public void setMid(int mid) {
		this.mid = mid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFace() {
		return face;
	}

	public void setFace(String face) {
		this.face = face;
	}

	public int getVipStatus() {
		return vipStatus;
	}

	public void setVipStatus(int vipStatus) {
		this.vipStatus = vipStatus;
	}
}
