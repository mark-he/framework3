package com.eagletsoft.boot.framework.data.json.meta;

import com.eagletsoft.boot.framework.data.json.load.impl.JPALoader;

import java.lang.annotation.*;

@Repeatable(value = Manys.class)  
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Many {
	String src() default "";
	String value() default "ext";
	String ref() default "";
	Class<?> target(); 
	Class wrapper() default Void.class;
	String[] fieldset() default {};
	String filter() default "";
	String[] option() default {};
	int size() default 500;
	boolean batch() default true;
	Class<?> loader() default JPALoader.class;
	String[] groups() default {"default"};
}
