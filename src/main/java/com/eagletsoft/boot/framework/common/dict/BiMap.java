package com.eagletsoft.boot.framework.common.dict;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BiMap<K, V> implements Serializable {

    private static final long serialVersionUID = 1L;
    private Map<K, V> map1 = new HashMap<K, V>();
    private Map<V, K> map2 = new HashMap<V, K>();

    public V byKey(K key)
    {
        return map1.get(key);
    }

    public K byValue(V value)
    {
        return map2.get(value);
    }

    public void put(K key, V value)
    {
        map1.put(key, value);
        map2.put(value, key);
    }

    public void clear()
    {
        map1.clear();
        map2.clear();
    }
}

