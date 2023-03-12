package com.eagletsoft.boot.framework.common.validation.meta;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Constraint(validatedBy = StateValidator.class)  
@Target( {java.lang.annotation.ElementType.FIELD })  
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)  
@Documented  
public @interface State {  
    String message() default "{validation.state.incorrect}";
    Class<?> value();
    boolean nullable() default true;
    Class<?>[] groups() default {};  
    Class<? extends Payload>[] payload() default {};  
}  