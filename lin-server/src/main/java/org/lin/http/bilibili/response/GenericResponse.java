package org.lin.http.bilibili.response;


/**
 * @author Lin =￣ω￣=
 * @date 2020/7/31
 */
public class GenericResponse {

	private Integer code;
	private String message;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
