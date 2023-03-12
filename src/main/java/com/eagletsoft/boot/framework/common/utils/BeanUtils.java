package com.eagletsoft.boot.framework.common.utils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

public class BeanUtils {

    private static Map<String, List<Field>> FIELDS_LIST = new HashMap<>();
    private static Map<String, Map<String, Field>> FIELDS_MAP = new HashMap<>();

    public static class Comparison {
        private Map<String, Object> before = new HashMap<>();
        private Map<String, Object> after = new HashMap<>();

        public Map<String, Object> getBefore() {
            return before;
        }

        public void setBefore(Map<String, Object> before) {
            this.before = before;
        }

        public Map<String, Object> getAfter() {
            return after;
        }

        public void setAfter(Map<String, Object> after) {
            this.after = after;
        }
    }

    public static Comparison compare(Object before, Object modification, String... fields) {
        Map<String, Object> beforeMap = BeanUtils.toMap(before);
        Map<String, Object> modifyMap = BeanUtils.toMap(modification);

        if (ArrayUtils.isEmpty(fields)) {
            fields = modifyMap.keySet().toArray(new String[] {});
        }

        Comparison comparison = new Comparison();
        for (String key: fields) {
            if (modifyMap.containsKey(key)) {
                Object beforeValue = beforeMap.get(key);
                Object afterValue = modifyMap.get(key);

                if (null == beforeValue && null == afterValue) {

                } else if (null != beforeValue && compare(beforeValue, afterValue)) {

                } else if (null != afterValue && compare(afterValue, beforeValue)) {

                } else {
                    comparison.before.put(key, beforeValue);
                    comparison.after.put(key, afterValue);
                }
            }
        }
        return comparison;
    }

    private static boolean compare(Object obj1, Object obj2) {
        if (null == obj2) {
            return false;
        }
        if (obj1 instanceof BigDecimal) {
            return ((BigDecimal) obj1).compareTo((BigDecimal)obj2) == 0;
        } if (obj1 instanceof Date) {
            return ((Date) obj1).getTime() == ((Date) obj2).getTime();
        }
        else {
            return obj1.equals(obj2);
        }
    }

    public static <T> T create(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setProperty(Object bean, String name, Object value) {
        try {
            PropertyUtils.setProperty(bean, name, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Object getProperty(Object bean, String name) {
        try {
            return PropertyUtils.getProperty(bean, name);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, Object> toMap(Object obj) {
        if (obj instanceof Map) {
            return (Map)obj;
        }
        Map<String, Object> map = new HashMap<>();
        if (null != obj) {
            List<Field> fields = findAllDeclaredFields(obj.getClass());
            for (Field f : fields) {
                Object value = BeanUtils.getProperty(obj, f.getName());
                map.put(f.getName(), value);
            }
        }
        return map;
    }

    public static List<Field> findAllDeclaredFields(Class clazz) {
        List<Field> fields = FIELDS_LIST.get(clazz.getName());
        if (null == fields) {
            fields = new ArrayList<>();
            fields.addAll(findAllDeclaredFieldMap(clazz).values());
            FIELDS_LIST.put(clazz.getName(), fields);
        }
        return fields;
    }

    public static Map<String, Field> findAllDeclaredFieldMap(Class clazz) {
        Map<String, Field> fieldMap = FIELDS_MAP.get(clazz.getName());
        if (null == fieldMap) {
            fieldMap = new HashMap<>();

            if (null != clazz.getSuperclass()) {
                fieldMap.putAll(findAllDeclaredFieldMap(clazz.getSuperclass()));
            }

            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    fieldMap.put(f.getName(), f);
                }
            }
            FIELDS_MAP.put(clazz.getName(), fieldMap);
        }
        return fieldMap;
    }

    public static List<Method> findAllDeclaredMethods(Class clazz) {
        List<Method> methodList = new ArrayList<>();
        if (null != clazz.getSuperclass()) {
            methodList.addAll(findAllDeclaredMethods(clazz.getSuperclass()));
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (!Modifier.isStatic(m.getModifiers())) {
                methodList.add(m);
            }
        }
        return methodList;
    }


    public static Class getSuperClassGenricType(Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 通过反射,获得定义Class时声明的父类的范型参数的类型.
     * 如public BookManager extends GenricManager<Book>
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     */
    public static Class getSuperClassGenricType(Class clazz, int index) throws IndexOutOfBoundsException {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }

    public static Object findAnnotationProperty(Annotation ann, String property, Object defaultValue) {
        Object value = null;
        try {
            Method method = ann.getClass().getDeclaredMethod(property);
            method.setAccessible(true);

            value = method.invoke(ann);
        } catch(Exception ex) {

        }
        if (null == value) {
            value = defaultValue;
        }
        return value;
    }

    public static Annotation[] findAnnotations(AnnotatedElement annotatedElement) {
        List<Annotation> ret = new ArrayList<>();

        Annotation[] annotations = AnnotationUtils.getAnnotations(annotatedElement);
        for (Annotation ann : annotations) {
            Object value = findAnnotationProperty(ann, "value", null);
            if (null != value && value instanceof Annotation[]) {
                for (Annotation o : (Annotation[])value) {
                    ret.add(o);
                }
            } else {
                ret.add(ann);
            }
        }
        return ret.toArray(new Annotation[0]);
    }
}
