package com.eagletsoft.boot.framework.cache;

public interface ICache {
    CacheData get(String sector, String key, Object query);
    boolean set(String sector, String key, Object query, Object value, int secondsToLive);
    void expiredAll();
}
