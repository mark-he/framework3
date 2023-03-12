package com.eagletsoft.boot.framework.data.json.context;

import com.eagletsoft.boot.framework.common.utils.ApplicationUtils;
import com.eagletsoft.boot.framework.data.json.ExtMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;

public class ExtJsonMessageConverter extends MappingJackson2HttpMessageConverter {
	private static Logger LOG = LoggerFactory.getLogger(ExtJsonMessageConverter.class);

	public ExtJsonMessageConverter() {
		super(ExtMapperFactory.create());
	}

	public ExtJsonMessageConverter(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	public void write (Object object, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		try {
			super.writeInternal(object, type, outputMessage);
		} finally {
			ExtViewContext.destroy();
		}
	}

	@Override
	protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		long timestamp = System.currentTimeMillis();

		ConverterTransactionWrapper wrapper = ApplicationUtils.getBean(ConverterTransactionWrapper.class);
		wrapper.write(this, object, type, outputMessage);

		LOG.debug("CONVERTER WRITE: " + (System.currentTimeMillis() - timestamp));
	}
}
