package com.eagletsoft.boot.framework.common.utils;

import ognl.Ognl;

public class ExpressionUtils {
    public static Object readValue(String expression, Object root)  {
        try {
            Object ret = Ognl.getValue(expression, root);
            return ret;
        }
        catch (Exception ex) {
            return null;
        }
    }
}
