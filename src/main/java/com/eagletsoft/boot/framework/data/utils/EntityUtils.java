package com.eagletsoft.boot.framework.data.utils;

import com.eagletsoft.boot.framework.data.entity.BaseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntityUtils {
	public static Collection getIds(Collection<? extends BaseEntity> col) {
		Collection ret = new ArrayList<>();
		for (BaseEntity entity : col) {
			ret.add(entity.getId());
		}
		return ret;
	}

	public static Map getIdMap(Collection<? extends BaseEntity> col) {
		Map ret = new HashMap();
		for (BaseEntity entity : col) {
			ret.put(entity.getId(), entity);
		}
		return ret;

	}
}
