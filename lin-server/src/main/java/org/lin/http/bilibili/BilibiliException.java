package org.lin.http.bilibili;

public class BilibiliException extends Exception {

	private static final long serialVersionUID = 3113398967393655595L;

	private int code;

	private String message;

	public BilibiliException() {

	}
	public BilibiliException(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
