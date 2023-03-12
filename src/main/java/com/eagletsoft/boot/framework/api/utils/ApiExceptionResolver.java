package com.eagletsoft.boot.framework.api.utils;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.common.i18n.MessageMaker;
import com.eagletsoft.boot.framework.common.i18n.meta.I18n;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ApiExceptionResolver implements HandlerExceptionResolver, Ordered {

	@Autowired
	private MessageMaker messageMaker;

	private static final Logger LOG = getLogger(ApiExceptionResolver.class);

	
    public int getOrder() {
        return HIGHEST_PRECEDENCE; 
    }
	
	@PostConstruct
	private void init()
	{
	}
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");

		response.setStatus(HttpServletResponse.SC_OK);
		ApiResponse result = null;

		Throwable cause = findCause(ex, ConstraintViolationException.class);
		if (null != cause) {
			ConstraintViolationException cve = (ConstraintViolationException)cause;
			StringBuffer sb = new StringBuffer();
			for (ConstraintViolation cv : cve.getConstraintViolations()) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
	            sb.append(handleConstraintViolation(cv));
			}
			result = ApiResponse.make(StandardErrors.VALIDATION.getStatus(), sb.toString(), null);
			
		} else if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
			cause = findCause(ex, "com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException");
	        result = ApiResponse.make(StandardErrors.INTERNAL_ERROR.getStatus(), messageMaker.make("error.framework.db.update"), null);
			if (null != cause) {
				if (JsonUtils.writeValue(cause).contains("foreign key constraint")) {
					result = ApiResponse.make(StandardErrors.INTERNAL_ERROR.getStatus(), messageMaker.make("error.framework.db.remove"), null);
				}
			}
		} else if (ex instanceof MethodArgumentNotValidException){
			BindingResult errorResult = ((MethodArgumentNotValidException)ex).getBindingResult();
	        List<FieldError> errors = errorResult.getFieldErrors();
	        StringBuffer sb = new StringBuffer();
	        for(FieldError error : errors) {
	        	if (sb.length() > 0) {
	        		sb.append(";");
				}
				sb.append(handleFieldError(error));
	        }
	        result = ApiResponse.make(StandardErrors.VALIDATION.getStatus(), sb.toString(), null);
		}
		else if (ex instanceof ServiceException)
		{
			ServiceException serviceEx = (ServiceException)ex;
			result = ApiResponse.make(serviceEx.getStatus(), messageMaker.makeWithNamespace(serviceEx.getNamespace(), serviceEx.getKey(), serviceEx.getParams()), null);
		}
		else if (ex instanceof org.springframework.http.converter.HttpMessageNotReadableException)
		{
			result = ApiResponse.make(StandardErrors.CLIENT_ERROR.getStatus(), messageMaker.make("error.framework.request.data"), null);
		}
		else {
			result = otherError(ex);
		}
		
		Writer writer = null;
		try
		{
			ObjectMapper om = JsonUtils.createMapper();
			String value = om.writeValueAsString(result);
			writer = response.getWriter();
			writer.write(value);
			writer.flush();
		}
		catch (Exception ex2)
		{
			throw new ServiceException(ex2);
		}
		finally
		{
			IOUtils.closeQuietly(writer);
		}
		return new ModelAndView();
	}

	protected ApiResponse otherError(Exception ex) {
    	String message = messageMaker.make(StandardErrors.UNKNOWN.getMessage());
    	LOG.error(message, ex);
		return ApiResponse.make(StandardErrors.UNKNOWN.getStatus(), message);
	}

	protected String handleFieldError(FieldError error) {
		ConstraintViolation cv = error.unwrap(ConstraintViolation.class);
    	return handleConstraintViolation(cv);
	}

	protected String handleConstraintViolation(ConstraintViolation cv) {
		String context = getI18nPrefix(cv.getRootBean().getClass()) + "." + cv.getPropertyPath();
		String message = simplizeMessageKey(cv.getMessageTemplate());
		String fieldName = messageMaker.make(context);
		message = messageMaker.makeWithNamespace(context, message, fieldName, cv.getInvalidValue());
		message = this.replaceAttributes(cv.getConstraintDescriptor().getAttributes(), message);
		return message;
	}

	protected String simplizeMessageKey(String message) {
		if (message.startsWith("{")) {
			int idx = message.indexOf("}");
			message = message.substring(1, idx);

			if (message.endsWith(".message")) {
				String[] ms = message.split("\\.");
				if (ms.length > 1) {
					message = ms[ms.length - 1 - 1] + ".message";
				}
			}
		}
		return message;
	}

	protected String getI18nPrefix(Class clazz) {
		I18n i = (I18n)clazz.getAnnotation(I18n.class);
		if (null != i) {
			return i.value();
		}
    	return clazz.getSimpleName();
	}

	protected String replaceAttributes(Map<String, Object> attrs, String message) {
		for (String key : attrs.keySet()) {
			message = message.replaceAll("\\[" + key +  "\\]", String.valueOf(attrs.get(key)));
		}
		return message;
	}

	protected Throwable findCause(Throwable ex, Class clazz) {
		if (ex.getClass().equals(clazz)) {
			return ex;
		}
		if (null == ex.getCause()) {
			return null;
		}
		else {
			if (ex.getCause().getClass().equals(clazz)) {
				return ex.getCause();
			}
			else {
				return findCause(ex.getCause(), clazz);
			}
		}
	}

	protected Throwable findCause(Throwable ex, String clazzName) {
		if (ex.getClass().getName().equals(clazzName)) {
			return ex;
		}
		if (null == ex.getCause()) {
			return null;
		}
		else {
			if (ex.getCause().getClass().getName().equals(clazzName)) {
				return ex.getCause();
			}
			else {
				return findCause(ex.getCause(), clazzName);
			}
		}
	}
}
