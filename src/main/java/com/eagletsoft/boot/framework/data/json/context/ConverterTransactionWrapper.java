package com.eagletsoft.boot.framework.data.json.context;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Type;

@Component
@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public class ConverterTransactionWrapper {
    public void write(ExtJsonMessageConverter converter, Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        try {
            converter.write(object, type, outputMessage);
        } finally {
            ExtViewContext.destroy();
        }
    }
}
