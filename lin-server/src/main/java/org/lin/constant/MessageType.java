package org.lin.constant;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/14
 */
public enum MessageType {
	NORMAL(0),
	/**
	 * 当发送的消息类型是<b>CALL_BACK</b>,在发送前会存进
	 * LinkedList 中, 当收到客户端的回调时再从 LinkedList 中移除,
	 * 如果在一段时间内没收到, 则重复发送
 	 */
	CALL_BACK(1);

	public int getType() {
		return type;
	}

	private final int type;

	MessageType(int type) {
		this.type = type;
	}
}
