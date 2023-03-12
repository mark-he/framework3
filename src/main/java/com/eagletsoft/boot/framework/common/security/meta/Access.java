package com.eagletsoft.boot.framework.common.security.meta;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Access {
	String value() default "";
	boolean disabled() default false;
}