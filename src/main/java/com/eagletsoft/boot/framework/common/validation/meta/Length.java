package com.eagletsoft.boot.framework.common.validation.meta;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Constraint(validatedBy = LengthValidator.class)
@Target( {java.lang.annotation.ElementType.FIELD })
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Documented
public @interface Length {
    String message() default "{validation.length.incorrect}";
    int min();
    int max();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}