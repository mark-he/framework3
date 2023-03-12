package com.eagletsoft.boot.framework.data.json.load;

import com.eagletsoft.boot.framework.data.json.meta.Many;
import com.eagletsoft.boot.framework.data.json.meta.Many2Many;
import com.eagletsoft.boot.framework.data.json.meta.One;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ILoader {
	Object loadOne(Object value, One one);
	List<?> loadMany(Object value, Many many);
	Map<Object, List> loadManyInBatch(Collection<Object> batch, Many many);
	Map<Object, Object> loadOneInBatch(Collection<Object> batch, One one);
	List<?> loadMany2Many(Object value, Many2Many m2m);
	Map<Object, List> loadMany2ManyInBatch(Collection<Object> batch, Many2Many m2m);
}
