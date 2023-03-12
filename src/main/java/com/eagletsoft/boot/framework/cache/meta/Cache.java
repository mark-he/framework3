package com.eagletsoft.boot.framework.cache.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Cache {
	String sector() default "eagletsoft";
	int value() default 60;
	String key() default "";
	boolean nullable() default true;
	Class parametrizedClass() default Void.class;
	Class[] parameterClass() default {};
}