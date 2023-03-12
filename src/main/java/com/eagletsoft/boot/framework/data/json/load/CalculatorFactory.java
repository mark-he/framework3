package com.eagletsoft.boot.framework.data.json.load;

import com.eagletsoft.boot.framework.common.utils.ApplicationUtils;

public class CalculatorFactory {
    public static <T> T create(Class clazz) {
        return (T)ApplicationUtils.getBean(clazz);
    }
}
