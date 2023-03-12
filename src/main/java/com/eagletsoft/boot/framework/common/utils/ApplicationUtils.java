package com.eagletsoft.boot.framework.common.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.ResolvableType;

public class ApplicationUtils {
	public static ApplicationContext CONTEXT;
	
	public static MessageSource getMessageSource()
	{
		MessageSource message = CONTEXT.getBean(MessageSource.class);
		return message;
	}
	
	public static <T> T getBean(Class<T> clazz)
	{
		return CONTEXT.getBean(clazz);
	}

	
	public static <T> T getBean(Class<T> clazz, boolean required)
	{
		try {
			return CONTEXT.getBean(clazz);
		}
		catch (Exception ex) {
			if (!required) {
				return null;
			}
			throw ex;
		}
	}
	
	public static <T> T getBean(String clazz)
	{
		return (T)CONTEXT.getBean(clazz);
	}

	public static <T> T getBean(Class<T> clazz, Class genericType) {

		String[] beanNamesForType = CONTEXT.getBeanNamesForType(ResolvableType.forClassWithGenerics(clazz, genericType));

		return (T)CONTEXT.getBean(beanNamesForType[0]);
	}
}
