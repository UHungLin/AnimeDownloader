package org.lin.constant;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/4
 */
public enum FileFormatType {

	@JsonProperty("flv")
	FLV("flv"),
	@JsonProperty("m4s")
	M4S("m4s"),
	@JsonProperty("mp4")
	MP4("mp4"),
	@JsonProperty("m3u8")
	M3U8("m3u8");

	private String formatType;

	FileFormatType(String formatType) {
		this.formatType = formatType;
	}

	public String value() {
		return formatType;
	}

}
