package com.eagletsoft.boot.framework.data.json.load.impl;

import com.eagletsoft.boot.framework.cache.CacheData;
import com.eagletsoft.boot.framework.cache.ICache;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.eagletsoft.boot.framework.data.json.meta.One;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;

public class JPACache {
    @Autowired
    private ICache cache;

    public String makeKey(One one) {
        return one.target().getCanonicalName() + "/" + JsonUtils.writeValue(one.fieldset());
    }

    public void set(Collection<Object> batch, One one, Object value) {
        try {
            if (one.lifetime() > 0) {
                if (null != value || one.cacheNull()) {
                    cache.set("eagletsoft", makeKey(one), JsonUtils.writeValue(batch), value, one.lifetime());
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public Object get(Collection<Object> batch, One one) {
        try {
            if (one.lifetime() > 0) {
                CacheData data = cache.get("eagletsoft", makeKey(one), batch);
                if (null != data) {
                    return data.getData();
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void set(Object param, One one, Object value) {
        try {
            if (one.lifetime() > 0) {
                if (null != value || one.cacheNull()) {
                    cache.set("eagletsoft", makeKey(one), param, value, one.lifetime());
                }
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public Object get(Object param, One one) {
        try {
            if (one.lifetime() > 0) {
                CacheData data = cache.get("eagletsoft", makeKey(one), param);
                if (null != data) {
                    return data.getData();
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
