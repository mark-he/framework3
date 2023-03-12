package com.eagletsoft.boot.framework.cache.local;

import com.eagletsoft.boot.framework.cache.CacheData;
import com.eagletsoft.boot.framework.cache.ICache;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class LocalCache implements ICache {
    private Logger LOG = LoggerFactory.getLogger(LocalCache.class);
    private CacheManager manager;
    public LocalCache(String path) {
        URL url = getClass().getResource(path);
        manager = CacheManager.create(url);
    }

    @Override
    public CacheData get(String sector, String key, Object query) {
        try {
            Cache cache = manager.getCache(sector);
            String cacheKey = makeCacheId(sector, key, query);
            Element element = cache.get(cacheKey);
            if (null == element || element.isExpired()) {
                return null;
            }
            else {
                return (CacheData)element.getObjectValue();
            }
        } catch (Exception ex) {
            LOG.error("Error happened in cache", ex);
            return null;
        }
    }

    @Override
    public boolean set(String sector, String key, Object query, Object value, int secondsToLive) {
        try {
            Cache cache = manager.getCache(sector);
            String cacheKey = makeCacheId(sector, key, query);

            Element element = new Element(cacheKey, value);
            element.setTimeToLive(secondsToLive);
            cache.put(element);
            return true;
        } catch (Exception ex) {
            LOG.error("Error happened in cache", ex);
            return false;
        }
    }

    @Override
    public void expiredAll() {
        manager.clearAll();

    }

    public String makeCacheId(String sector, String key, Object query)
    {
        StringBuffer cacheId = new StringBuffer("cache://");
        cacheId.append(sector).append("/").append(key).append("/");
        if (null != query)
        {
            ObjectMapper mapper = JsonUtils.createMapper();
            try
            {
                cacheId.append(mapper.writeValueAsString(query));
            }
            catch(Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return cacheId.toString();
    }
}
