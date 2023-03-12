package com.eagletsoft.boot.framework.cache.local;

import java.util.HashMap;
import java.util.Map;

public class ThreadStore {
	private final static String STORE_TIME = "STORE_TIME";
	private final static int TIMEOUT = 60 * 1000;
	private final static ThreadLocal<Map<String, Object>> STORE = new ThreadLocal<>();
	
	private static Map<String, Object> getStore() {
		Map<String, Object> cache = STORE.get();
		if (null == cache) {
			cache = new  HashMap<String, Object>();
			cache.put(STORE_TIME, System.currentTimeMillis());
			STORE.set(cache);
		}
		else { //firewall
			Long time = (Long)cache.get(STORE_TIME);
			if (System.currentTimeMillis() - time >= TIMEOUT) {
				cache.clear();
				cache.put(STORE_TIME, System.currentTimeMillis());
			}
		}
		return cache;
	}
	
	public static void add(String key, Object data) {
		getStore().put(key, data);
	}
	
	public static Object get(String key) {
		return getStore().get(key);
	}
	
	public static void clearup() {
		STORE.set(null);
	}
}
