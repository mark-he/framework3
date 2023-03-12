package com.eagletsoft.boot.framework.http.resp;

import org.apache.http.HttpEntity;

import java.io.IOException;


public interface IResponseHandler<T> {
	void setDebug(boolean debug);
	void onSuccess(T content);
	void onFailure(int statusCode, byte[] content);
	
	T handle(HttpEntity entity) throws IOException ;
}
