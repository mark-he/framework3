package com.eagletsoft.boot.framework.data.constraint.meta;

import com.eagletsoft.boot.framework.data.json.meta.ExtViewports;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target( {java.lang.annotation.ElementType.FIELD })  
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)  
@Documented
@Repeatable(value = Uniques.class)
public @interface Unique {  
    String message() default "{com.eagletsoft.validation.Unique.message}";
    String value() default  ALONE;
    String ALONE = "ALONE";
    String[] with() default {};
}  