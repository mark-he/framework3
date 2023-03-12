package com.eagletsoft.boot.framework.data.json.load;

import com.eagletsoft.boot.framework.data.json.context.KeyValue;

import java.util.Collection;

public interface Calculator<T> {
	Object calc(T bean, Collection<KeyValue> exts);
}
