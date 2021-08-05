package org.lin.http.ImomoeLa;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/25
 */
public class ImomoeLaException extends Exception {

	private int code;

	private String message;

	public ImomoeLaException() {
	}

	public ImomoeLaException(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
