package com.eagletsoft.boot.framework.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalUtils {
    public static BigDecimal toScale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
