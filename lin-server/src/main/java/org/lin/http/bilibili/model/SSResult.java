package org.lin.http.bilibili.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Lin =￣ω￣=
 * @date 2020/9/4
 */
public class SSResult {

	@SerializedName("main_section")
	private MainSection mainSection;

	public MainSection getMainSection() {
		return mainSection;
	}

	public void setMainSection(MainSection mainSection) {
		this.mainSection = mainSection;
	}
}
