package com.eagletsoft.boot.framework.http.resp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SyncResponseHandler<T> extends StringResponseHandler {
	protected static final Logger LOG = LoggerFactory.getLogger(SyncResponseHandler.class);
	public static final int OK = 200;

	protected int statusCode;
	protected T data;

	@Override
	public void onSuccess(String content) {
		this.statusCode = 200;

    	if (isDebug)
		{
    		LOG.debug("Return From Server: " + statusCode + ":" + content);
		}
		try
		{
			this.data = parse(content);
		}
		catch (RuntimeException ex2)
		{
			throw ex2;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void onFailure(int statusCode, byte[] content) {
		this.statusCode = statusCode;

		String strContent = new String(content);
		if (isDebug)
		{
			LOG.debug("Return From Server: " + statusCode + ":" + strContent);
		}
		throw new RuntimeException("With Error " + statusCode + " " + strContent);
	}
	
	public abstract T parse(String content) ;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
