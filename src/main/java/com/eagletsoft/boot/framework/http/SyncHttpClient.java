package com.eagletsoft.boot.framework.http;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.eagletsoft.boot.framework.http.resp.IResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncHttpClient {
	private static final Logger LOG = LoggerFactory.getLogger(SyncHttpClient.class);
	private static int REQUEST_TIMEOUT = 30000;
	private static int CONNECT_TIMEOUT = 23000;
	
	private int timeoutRequest = 10000;
	private int timeoutConnect = 20000;
	
	private HttpClientConnectionManager connectionManager;
	private HttpRequestRetryHandler retryHandler;
	private int retryTimes = 3;
	private ObjectMapper mapper = JsonUtils.createMapper();
	
	private boolean debug;

	@PostConstruct
	public void init() {
		if (null == connectionManager)
		{
			//connectionManager = new BasicHttpClientConnectionManager();
			connectionManager = new PoolingHttpClientConnectionManager();
			// 设置最大连接数
			((PoolingHttpClientConnectionManager)connectionManager).setMaxTotal(100);
		}
		if (null == retryHandler)
		{
			retryHandler = new HttpRequestRetryHandler() {
			    @Override
				public boolean retryRequest(IOException exception, int executionCount, org.apache.http.protocol.HttpContext context) {
			    	if (executionCount >= retryTimes) {
			            // Do not retry if over max retry count
			            return false;
			        }
			        if (exception instanceof InterruptedIOException) {
			            // Timeout
			            return false;
			        }
			        if (exception instanceof UnknownHostException) {
			            // Unknown host
			            return false;
			        }
			        if (exception instanceof ConnectTimeoutException) {
			            // Connection refused
			            return false;
			        }
			        if (exception instanceof SSLException) {
			            // SSL handshake exception
			            return false;
			        }
			        HttpClientContext clientContext = HttpClientContext.adapt(context);
			        HttpRequest request = clientContext.getRequest();
			        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			        if (idempotent) {
			            return true;
			        }
			        return false;
				}
			};
		}
	}

	public int getTimeoutRequest() {
		return timeoutRequest;
	}

	public void setTimeoutRequest(int timeoutRequest) {
		this.timeoutRequest = timeoutRequest;
	}

	public int getTimeoutConnect() {
		return timeoutConnect;
	}

	public void setTimeoutConnect(int timeoutConnect) {
		this.timeoutConnect = timeoutConnect;
	}

	public HttpClientConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(HttpClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public HttpRequestRetryHandler getRetryHandler() {
		return retryHandler;
	}

	public void setRetryHandler(HttpRequestRetryHandler retryHandler) {
		this.retryHandler = retryHandler;
	}

	public void postWithJson(URI uri, Object data, IResponseHandler handler, Map<String, String> headers) {
		CloseableHttpResponse response = null;
		HttpPost httppost = null;
		try {
			String body = mapper.writeValueAsString(data);
			
			if (debug) {
				LOG.info(uri.toASCIIString());
				LOG.info(body);
			}

			httppost = new HttpPost(uri);
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).build();
			
			httppost.setConfig(requestConfig);
			StringEntity entity = new StringEntity(body,"UTF-8");
			entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json"); 
			httppost.setEntity(entity);

			if (null != headers) {
				for (String key : headers.keySet()) {
					httppost.setHeader(key, headers.get(key));
				}
			}

			response = buildClient().execute(httppost, new BasicHttpContext());
			handleResponse(response, handler);
		}
		catch (ServiceException se) {
			throw se;
		}
		catch (Exception ex) {
			throw new ServiceException(StandardErrors.EXTERNAL_ERROR.getStatus(), "error.http", ex.getMessage());
		} finally {
			if (null != httppost) {
				httppost.completed();
			}
			IOUtils.closeQuietly(response);
		}
	}
	
	public void post(URI uri, Map<String, String> parameters, IResponseHandler handler, Map<String, String> headers) {
		CloseableHttpResponse response = null;
		HttpPost httppost = null;
		try {
			if (debug) {
				LOG.info(uri.toASCIIString());
				for (String key : parameters.keySet()) {
					LOG.info("key:" + key + " = " + parameters.get(key));
				}
			}
			httppost = new HttpPost(uri);
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).build();
			httppost.setConfig(requestConfig);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if (null != parameters) {
				for (String key : parameters.keySet()) {
					params.add(new BasicNameValuePair(key, parameters.get(key)));
				}
			}
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			if (null != headers) {
				for (String key : headers.keySet()) {
					httppost.setHeader(key, headers.get(key));
				}
			}

			response = buildClient().execute(httppost, new BasicHttpContext());
			handleResponse(response, handler);
		}
		catch (ServiceException se) {
			throw se;
		}
		catch (Exception ex) {
			throw new ServiceException(StandardErrors.EXTERNAL_ERROR.getStatus(), "error.http", ex.getMessage());
		} finally {
			if (null != httppost) {
				httppost.completed();
			}
			IOUtils.closeQuietly(response);
		}
	}

	public CloseableHttpResponse get(URI uri, Map<String, String> parameters, Map<String, String> headers) {
		CloseableHttpResponse response = null;
		HttpGet httpget = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if (null != parameters) {
				for (String key : parameters.keySet()) {
					params.add(new BasicNameValuePair(key, parameters.get(key)));
				}
				String query = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
				uri = new URI(uri + "?" + query);
			}
			httpget = new HttpGet(uri);

			if (null != headers) {
				for (String key : headers.keySet()) {
					httpget.setHeader(key, headers.get(key));
				}
			}
			if (debug) {
				LOG.info(httpget.getURI().toASCIIString());
			}

			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeoutRequest).setConnectTimeout(timeoutConnect).build();
			httpget.setConfig(requestConfig);
			response = buildClient().execute(httpget, new BasicHttpContext());
			httpget.completed();
			return response;
		}
		catch (ServiceException se) {
			throw se;
		}
		catch (Exception ex) {
			throw new ServiceException(StandardErrors.EXTERNAL_ERROR.getStatus(), "error.http", ex.getMessage());
		} finally {
			if (null != httpget) {
				httpget.completed();
			}
			IOUtils.closeQuietly(response);
		}
	}

	public void get(URI uri, Map<String, String> parameters, IResponseHandler handler, Map<String, String> headers) {
		CloseableHttpResponse response = null;
		HttpGet httpget = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if (null != parameters) {
				for (String key : parameters.keySet()) {
					params.add(new BasicNameValuePair(key, parameters.get(key)));
				}
				String query = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
				uri = new URI(uri + "?" + query);
			}
			httpget = new HttpGet(uri);

			if (null != headers) {
				for (String key : headers.keySet()) {
					httpget.setHeader(key, headers.get(key));
				}
			}
			if (debug) {
				LOG.info(httpget.getURI().toASCIIString());
			}

			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeoutRequest).setConnectTimeout(timeoutConnect).build();
			httpget.setConfig(requestConfig);
			response = buildClient().execute(httpget, new BasicHttpContext());
			handleResponse(response, handler);
			httpget.completed();
		}
		catch (ServiceException se) {
			throw se;
		}
		catch (Exception ex) {
			throw new ServiceException(StandardErrors.EXTERNAL_ERROR.getStatus(), "error.http", ex.getMessage());
		} finally {
			if (null != httpget) {
				httpget.completed();
			}
			IOUtils.closeQuietly(response);
		}
	}

	public InputStream download(URI uri, Map<String, String> parameters) {
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if (null != parameters) {
				for (String key : parameters.keySet()) {
					params.add(new BasicNameValuePair(key, parameters.get(key)));
				}
				String query = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
				uri = new URI(uri + "?" + query);
			}
			HttpGet httpget = new HttpGet(uri);
			response = buildClient().execute(httpget, new BasicHttpContext());

			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			return is;
		 }
		 catch (ServiceException se) {
			throw se;
		 }
		 catch (Exception ex) {
			 IOUtils.closeQuietly(response);
			 throw new ServiceException(StandardErrors.EXTERNAL_ERROR.getStatus(), "error.http", ex.getMessage());
		 }
		finally {
			//do not close response
		}
	}

	public void upload(URI uri, String filename, InputStream is, Map<String, String> parameters) {
		CloseableHttpResponse response = null;
		try {
			HttpPost httpPost = new HttpPost(uri);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			//3)
			builder.addBinaryBody("file", is, ContentType.create("multipart/form-data"), filename);
			//4)构建请求参数 普通表单项
			if (null != parameters) {
				for (String key : parameters.keySet()) {
					StringBody stringBody = new StringBody(String.valueOf(parameters.get(key)),ContentType.MULTIPART_FORM_DATA);
					builder.addPart(key, stringBody);
				}
			}

			HttpEntity entity = builder.build();
			httpPost.setEntity(entity);
			//发送请求
			response = buildClient().execute(httpPost);
			entity = response.getEntity();
		}
		catch (Exception ex) {
			throw new ServiceException(StandardErrors.EXTERNAL_ERROR.getStatus(), "error.http", ex.getMessage());
		}
		finally {
			IOUtils.closeQuietly(response);
		}
	}

	private void handleResponse(HttpResponse response, IResponseHandler handler) throws Exception {
		handler.setDebug(debug);
		HttpEntity entity = response.getEntity();
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			handler.onSuccess(handler.handle(entity));
		} else {
			handler.onFailure(statusCode, EntityUtils.toByteArray(entity));
		}
	}
	
	public CloseableHttpClient buildClient()
	{
		//return HttpClients.createDefault();
		//.setRetryHandler(retryHandler)
		return HttpClients.custom().setConnectionManager(connectionManager).build();
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}
}
