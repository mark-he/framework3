package com.eagletsoft.boot.framework.data.json;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.data.json.context.ExtViewContext;
import com.eagletsoft.boot.framework.data.json.meta.*;
import org.apache.commons.collections.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExtViewHelper {
    public static void setGroup(String group, int depth) {
        ExtViewContext.get().setGroup(group);
        ExtViewContext.get().setMaxDepth(depth);
    }

    public static Map writeMap(Object obj, String group, int depth) {
        ExtViewContext.Context context = ExtViewContext.saveAndInit();
        try {
            String json = writeValueAsString(obj, group, depth);
            return ExtMapperFactory.create().readValue(json, HashMap.class);
        } catch (Exception ex) {
            throw new ServiceException(StandardErrors.CLIENT_ERROR.getStatus(), ex.getMessage());
        } finally {
            ExtViewContext.destroyAndRestore(context);
        }
    }

    public static <T> T convert(Object obj, Class<T> clazz) {
        ExtViewContext.Context context = ExtViewContext.saveAndInit();
        try {
            T ret = ExtMapperFactory.create().convertValue(obj, clazz);
            return ret;
        } finally {
            ExtViewContext.destroyAndRestore(context);
        }
    }

    public static String writeValueAsString(Object value, String group, int depth) {
        ExtViewContext.Context context = ExtViewContext.saveAndInit();
        try {
            setGroup(group, depth);
            return ExtMapperFactory.create().writeValueAsString(value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            ExtViewContext.destroyAndRestore(context);
        }
    }

    public static Collection<ExtendAnnotation> findExtendAnnotations(Class value) {
        Collection<ExtendAnnotation> refs = null;
        Annotation[] clazzAs = value.getDeclaredAnnotations();
        refs = buildExtendAnnotations(refs, clazzAs, value.getSimpleName());

        Field[] fields = value.getDeclaredFields();
        for (Field f : fields) {
            Annotation[] as = f.getDeclaredAnnotations();
            refs = buildExtendAnnotations(refs, as, f.getName());
        }
        if (null == refs) {
            refs = CollectionUtils.EMPTY_COLLECTION;
        }
        return refs;
    }

    private static Collection<ExtendAnnotation> buildExtendAnnotations(Collection<ExtendAnnotation> refs, Annotation[] as, String name) {
        for (Annotation a : as) {
            if (a instanceof One || a instanceof Many || a instanceof Many2Many) {
                if (refs == null) {
                    refs = new ArrayList<>();
                }
                refs.add(new ExtendAnnotation(true, name, a));
            } else if (a instanceof Ones) {
                if (refs == null) {
                    refs = new ArrayList<>();
                }
                Ones ones = (Ones)a;
                for (One o : ones.value()) {
                    refs.add(new ExtendAnnotation(true, name, o));
                }
            }
            else if (a instanceof Manys) {
                if (refs == null) {
                    refs = new ArrayList<>();
                }
                Manys manys = (Manys)a;
                for (Many o : manys.value()) {
                    refs.add(new ExtendAnnotation(true, name, o));
                }
            }
        }
        return refs;
    }

}
