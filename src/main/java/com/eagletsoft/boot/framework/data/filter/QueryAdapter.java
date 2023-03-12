package com.eagletsoft.boot.framework.data.filter;

import javax.persistence.Query;

public interface QueryAdapter {
    void applyTransform(Query query, Class clazz);
}
