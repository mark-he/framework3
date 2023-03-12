package com.eagletsoft.boot.framework.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.*;

public class JsonUtils {
	private static ObjectMapper SAFE_MAPPER = new ObjectMapper();
	static {
		SAFE_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		SAFE_MAPPER.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		SAFE_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		SAFE_MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		SAFE_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		SAFE_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
		SAFE_MAPPER.setSerializationInclusion(Include.NON_NULL);
	}

	public static ObjectMapper getMapperInstance() {
		return SAFE_MAPPER;
	}
	
	public static ObjectMapper createMapper()
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		mapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
		mapper.setSerializationInclusion(Include.NON_NULL);

		return mapper;
	}

	
	public static <T> T readValue(String value, Class<T> clazz) {
		try
		{
			return SAFE_MAPPER.readValue(value, clazz);
		}
		catch (Exception ex) {
			throw new RuntimeException("Parse JSON errors: " + value, ex);
		}
	}

	public static JavaType makeParametrizedClass(Class parametrizedClass, Class... parameterClass) {
		return SAFE_MAPPER.getTypeFactory().constructParametricType(parametrizedClass, parameterClass);
	}

	public static <T> T readValue(String value, JavaType clazz) {
		try
		{
			return SAFE_MAPPER.readValue(value, clazz);
		}
		catch (Exception ex) {
			throw new RuntimeException("Parse JSON errors: " + value, ex);
		}
	}
	
	public static String writeValue(Object obj) {
		try
		{
			return SAFE_MAPPER.writeValueAsString(obj);
		}
		catch (Exception ex) {
			throw new RuntimeException("Parse JSON errors: " + obj, ex);
		}
	}
}
