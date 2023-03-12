package com.eagletsoft.boot.framework.common.i18n;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageMaker {
	private static final Logger LOG = LoggerFactory.getLogger(MessageMaker.class);

	@Autowired
	private MessageSource messageSource;

	public String make(Exception ex)
	{
		return make(null, ex);
	}

	public String make(String key, Object... params)
	{
		try {
			return messageSource.getMessage(key, params, UserSession.getLocale());
		}
		catch (Exception ex) {
			return key;
		}
	}

	public String makeWithNamespace(String namespace, Throwable ex)
	{
		String key = null;
	    if (ex instanceof ServiceException)
		{
	    	if (null != ex.getCause())
	    	{
	    		LOG.error("Inner Error", ex);
	    	}
	    	key = ((ServiceException)ex).getKey();
			return makeWithNamespace(namespace, key, ((ServiceException)ex).getParams());
		}
		return ex.getMessage();
	}

	public String makeWithNamespace(String namespace, String key, Object... params)
	{
		if (null != namespace && namespace.length() > 0)
		{
			String nsParamKey = namespace + "@" + key;
			String nsKeyValue = this.make(nsParamKey, params);
			if (nsKeyValue.equals(nsParamKey)) {
				nsKeyValue = this.make(key, params);
			}
			return nsKeyValue;
		}
		else
		{
			return this.make(key, params);
		}
	}
}
