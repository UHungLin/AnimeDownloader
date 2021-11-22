package org.lin.core;

import org.apache.commons.lang3.StringUtils;
import org.lin.constant.HttpDownStatus;
import org.lin.constant.MessageType;
import org.lin.pojo.Message;
import org.lin.pojo.entity.TaskInfo;
import org.lin.websocket.ClientCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 当客户端切出【下载页面】时，如果这时下载任务完成，【下载页面】无法收到下载完成的提示信息
 * 所以需要确保客户端收到信息，如果客户端没有响应，则重复发送
 *
 * @author Lin =￣ω￣=
 * @date 2021/7/30
 */
public class MessageCore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageCore.class);

	private static final String SEPERATE = "_";
	private static final Object LOCK = new Object();
	private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

	// TODO 换成 LinkedHashMap
	private static final Map<String, Map<String, Message>> taskIdMessageMap = new HashMap<>();
	private static final LinkedList<Message> callbackMessageList = new LinkedList<>();

	private ScheduledFuture<?> sendMessageTimer;

	private static final MessageCore INSTANCE = new MessageCore();

	public void init() {
		loadSendMessageTimer();
	}

	private MessageCore() {}

	public static MessageCore getInstance() {
		return INSTANCE;
	}

	public static void send(HttpDownStatus status, String msg, TaskInfo taskInfo) {
		if (status == HttpDownStatus.FAIL || status == HttpDownStatus.COMPLETE) {
			send(MessageType.CALL_BACK, status, msg, taskInfo);
		} else {
			send(MessageType.NORMAL, status, msg, taskInfo);
		}
	}

	public static void send(MessageType type, HttpDownStatus status, String msg, TaskInfo taskInfo) {

		Message message = createMessage(type, status, msg, taskInfo);

		if (message.getType() == MessageType.CALL_BACK.getType()) {
			// messageId = taskId_incrementId
			String messageId = message.getData().get("id") + SEPERATE + ID_GENERATOR.getAndIncrement();
			message.setId(messageId);

			synchronized (LOCK) {
				String taskId = (String) message.getData().get("id");
				Map<String, Message> map = taskIdMessageMap.get(taskId);
				if (map != null) {
					// delete old message
//				    callbackMessageList.remove(map.values().toArray()[0]);
					callbackMessageList.remove(map.values().stream().findFirst().get());
					taskIdMessageMap.remove(taskId);
				}
				callbackMessageList.add(message);
				taskIdMessageMap.put((String) message.getData().get("id"), Collections.singletonMap(message.getId(), message));
			}
		}
		ClientCallback.push(message);
	}

	public static void send(Message message) {
		ClientCallback.push(message);
	}

	/**
	 * 当收到客户端回调的 messageId 时，从 callbackMessageList 删除 message
	 *
	 * @param messageId
	 */
	public static void deleteMessage(String messageId) {
		System.out.println("删除 messageId - " + messageId);
		if (StringUtils.isNotBlank(messageId) && !taskIdMessageMap.isEmpty()) {
			String[] ids = messageId.split(SEPERATE);
			if (ids.length != 2) {
				LOGGER.warn("messageId format error: {}", messageId);
				return;
			}
			synchronized (LOCK) {
				String taskId = ids[0];
				Map<String, Message> messageMap = taskIdMessageMap.get(taskId);
				if (messageMap != null) {
					String id = messageMap.keySet().stream().findFirst().get();
					if (Objects.equals(messageId, id)) {
						callbackMessageList.remove(messageMap.values().stream().findFirst().get());
						taskIdMessageMap.remove(taskId);
					}
				}
			}
		}
	}

	public static void deleteMessageByTaskId(String taskId) {
		if (StringUtils.isNotBlank(taskId) && !taskIdMessageMap.isEmpty()) {
			synchronized (LOCK) {
				Map<String, Message> messageMap = taskIdMessageMap.get(taskId);
				if (messageMap != null && !messageMap.isEmpty()) {
					callbackMessageList.remove(messageMap.values().stream().findFirst().get());
					taskIdMessageMap.remove(taskId);
				}
			}
		}
	}

	private static Message createMessage(MessageType type, HttpDownStatus status, String msg, TaskInfo taskInfo) {
		Message message = new Message();
		message.setType(type.getType());
		message.setStatus(status.getStatus());
		message.setMsg(msg);
		Map<String, Object> data = new HashMap<>();
		data.put("id", taskInfo.getId());
		data.put("title", taskInfo.getName());
		data.put("currentSize", taskInfo.getCurrentOffset());
		data.put("totalSize", taskInfo.getTotalSize());
		data.put("speed", taskInfo.getSpeed());
		data.put("filePath", taskInfo.getFilePath());
		data.put("cover", taskInfo.getCoverImg());
		message.setData(data);
		return message;
	}

	public void stop() {
		ThreadContext.shutdown(this.sendMessageTimer);
	}


	private void loadSendMessageTimer() {
		this.sendMessageTimer = ThreadContext.timer(5, 2, TimeUnit.SECONDS, new SendMessageTimer());
	}

	private static class SendMessageTimer implements Runnable {

		@Override
		public void run() {
//			System.out.println("定时器 SendMessageTimer 执行...");
			if (callbackMessageList.isEmpty()) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
				}
				return;
			}
			System.out.println("callbackMessageList = " + callbackMessageList);
			for (Message message : callbackMessageList) {
				send(message);
			}
		}
	}

}
