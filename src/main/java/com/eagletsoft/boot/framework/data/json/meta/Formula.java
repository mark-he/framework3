package com.eagletsoft.boot.framework.data.json.meta;

import java.lang.annotation.*;

@Repeatable(value = Formulas.class) 
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Formula {
	String value() default "ret";
	String expression() default "";
	Class calc() default Void.class;
	boolean batch() default true;
	String[] groups() default {"default"};
}
