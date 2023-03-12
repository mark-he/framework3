package com.eagletsoft.boot.framework.data.json;

import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.eagletsoft.boot.framework.data.json.context.ExtViewContext;
import com.eagletsoft.boot.framework.data.json.meta.ExtViewport;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExtViewportManager {

    private static ExtViewportManager INSTANCE = new ExtViewportManager();

    public static ExtViewportManager getInstance() {
        return INSTANCE;
    }

    private Map<String, Class> BEAN_GROUP_VIEWPORT = new HashMap<>();

    public void register(Class beanClass, String group, Class viewportClass) {
        BEAN_GROUP_VIEWPORT.put(beanClass.getName() + ":" + group, viewportClass);
    }

    public Collection<ExtendAnnotation> findExtendAnnotations(Class clazz) {
        String group = ExtViewContext.get().getGroup();
        Class matchedClass = this.genViewportClass(clazz, group);
        if (null != matchedClass) {
            return ExtViewHelper.findExtendAnnotations(matchedClass);
        } else {
            return ExtViewHelper.findExtendAnnotations(clazz);
        }
    }

    public Class genViewportClass(Class clazz, String group) {
        Set<ExtViewport> ports = AnnotationUtils.getRepeatableAnnotations(clazz, ExtViewport.class);
        Class matchedClass = null;
        for (ExtViewport viewport : ports) {
            if (ArrayUtils.contains(viewport.groups(), group)) {
                matchedClass = viewport.value();
                break;
            }
        }

        if (null == matchedClass) {
            matchedClass = BEAN_GROUP_VIEWPORT.get(clazz.getName() + ":" + group);
        }
        return matchedClass;
    }

    public Object genViewportObject(Object bean) {
        String group = ExtViewContext.get().getGroup();
        Class matchedClass = this.genViewportClass(bean.getClass(), group);

        if (null != matchedClass) {
            return JsonUtils.createMapper().convertValue(bean, matchedClass);
        } else {
            return null;
        }
    }
}
