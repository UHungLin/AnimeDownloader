package org.lin.http.acfun;

public class AcFunException extends Exception {

	private int code;

	private String message;

	public AcFunException() {

	}
	public AcFunException(int code, String message) {
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
