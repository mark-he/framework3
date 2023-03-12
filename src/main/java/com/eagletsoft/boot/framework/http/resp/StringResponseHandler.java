package com.eagletsoft.boot.framework.http.resp;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public abstract class StringResponseHandler implements IResponseHandler<String> {
	protected boolean isDebug;

	@Override
	public void setDebug(boolean debug) {
		this.isDebug = debug;		
	}

	@Override
	public String handle(HttpEntity entity) throws IOException {
		
		return EntityUtils.toString(entity);
	}
}
