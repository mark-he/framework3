package com.eagletsoft.boot.framework.common.validation.meta;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Pattern(regexp = "")
public @interface ChinaMobile {
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @OverridesAttribute(constraint = Pattern.class, name = "regexp")

    String regexp() default  "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";

    @OverridesAttribute(constraint = Pattern.class, name = "message")
    String message() default "{com.eagletsoft.validation.Chinamobile.message}";

}