package org.lin.exception;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/26
 */
public class ParseException extends Exception {

	public ParseException() {
		super();
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

}
