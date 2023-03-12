package com.eagletsoft.boot.framework.data.json.meta;

import java.lang.annotation.*;

@Repeatable(value = ExtViewports.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ExtViewport {
    Class value();
    String[] groups() default { "default" };
}
