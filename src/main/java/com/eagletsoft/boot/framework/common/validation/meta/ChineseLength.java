package com.eagletsoft.boot.framework.common.validation.meta;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = {ChineseLenValidator.class})
@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChineseLength {  
    String message() default "{com.eagletsoft.validation.ChineseLength.message}";
    int value() default 100;
    int min() default 0;
    Class<?>[] groups() default {};  
    Class<? extends Payload>[] payload() default {};  
}  