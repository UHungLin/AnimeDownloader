package org.lin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Lin =￣ω￣=
 * @date 2020/6/17
 */
public class JsonUtil {

	private final static Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

	private final static ObjectMapper objectMapper = new ObjectMapper();

	public static ObjectMapper getInstance() {
		return objectMapper;
	}

	public static String objToJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			LOGGER.error("objToJson error {}", e);
			return null;
		}
	}

	public static String mapToJson(Map map) {
		try {
			return objectMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			LOGGER.error("mapToJson error {}", e);
			return null;
		}
	}


//	public static <T> T mapToBean(Map<String, Object> map, T bean) {
//		BeanMap beanMap = BeanMap.create(bean);
//		beanMap.putAll(map);
//		return bean;
//	}

}
