package com.eagletsoft.boot.framework.data.filter.impl;

import com.eagletsoft.boot.framework.data.filter.IndexFinder;
import com.eagletsoft.boot.framework.data.filter.meta.Filtered;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;

public class DefaultIndexFinder implements IndexFinder {
    private static String[] DEFAULT_FILTERED = new String[]{"deleted", "createdTime", "updatedTime", "createdBy", "updatedBy", "id", "uuid"};

    @Override
    public boolean isIndex(Class root, String fieldName) {
        try {
            if (!ArrayUtils.contains(DEFAULT_FILTERED, fieldName)) {
                Filtered f = AnnotationUtils.getAnnotation(root.getDeclaredField(fieldName), Filtered.class);
                if (null == f) {
                    return false;
                }
            }
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }
}
