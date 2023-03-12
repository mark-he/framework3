package com.eagletsoft.boot.framework.data.constraint.meta;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Uniques {
	Unique[] value();
}
