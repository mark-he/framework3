package com.eagletsoft.boot.framework.http.resp.impl;


import com.eagletsoft.boot.framework.http.resp.SyncResponseHandler;

public class TextResponseHandler extends SyncResponseHandler<String> {

	@Override
	public String parse(String content) {
		return content;
	}
}
