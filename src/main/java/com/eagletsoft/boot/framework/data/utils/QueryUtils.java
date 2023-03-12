package com.eagletsoft.boot.framework.data.utils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

public class QueryUtils {

    public static Predicate or(CriteriaBuilder cb, Predicate p, Predicate p2) {
        if (null == p || null == p2) {
            if (null == p) {
                return p2;
            }
            else {
                return p;
            }
        }
        return cb.or(p, p2);
    }

    public static Predicate and(CriteriaBuilder cb, Predicate p, Predicate p2) {
        if (null == p || null == p2) {
            if (null == p) {
                return p2;
            }
            else {
                return p;
            }
        }

        return cb.and(p, p2);
    }
}
