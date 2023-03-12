package com.eagletsoft.boot.framework.http;

import org.apache.http.Consts;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.nio.charset.CodingErrorAction;

public class HttpClientConnectionManagerFactory {
	public static HttpClientConnectionManager defaultPoolingConfig()
	{
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	    MessageConstraints messageConstraints = MessageConstraints.custom()
	            .setMaxHeaderCount(500)
	            .setMaxLineLength(2000)
	            .build();
	     
	    ConnectionConfig connectionConfig = ConnectionConfig.custom()
	            .setMalformedInputAction(CodingErrorAction.IGNORE)
	            .setUnmappableInputAction(CodingErrorAction.IGNORE)
	            .setCharset(Consts.UTF_8)
	            .setMessageConstraints(messageConstraints)
	            .build();
	    cm.setDefaultConnectionConfig(connectionConfig);
	    cm.setMaxTotal(1024);
	    cm.setDefaultMaxPerRoute(512);
	    return cm;
	}
}
