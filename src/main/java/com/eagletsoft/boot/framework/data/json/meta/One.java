package com.eagletsoft.boot.framework.data.json.meta;

import com.eagletsoft.boot.framework.data.json.load.impl.JPALoader;

import java.lang.annotation.*;

@Repeatable(value = Ones.class)  
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface One {

	int lifetime() default 0;
	boolean cacheNull() default true;
	String src() default "";
	String value() default "ext";
	String ref() default "id";
	Class<?> target(); 
	String[] fieldset() default {};
	Class wrapper() default Void.class;
	String[] option() default {}; //{"key", "value"}
	String filter() default "";
	Class<?> loader() default JPALoader.class;
	boolean batch() default true;
	String[] groups() default {"default"};
	String[] mapping() default {};
}
