package com.eagletsoft.boot.framework.data.json.load.impl;

import com.eagletsoft.boot.framework.data.filter.SimpleQuery;
import com.eagletsoft.boot.framework.data.json.ExtViewHelper;
import com.eagletsoft.boot.framework.data.json.load.ILoader;
import com.eagletsoft.boot.framework.data.json.meta.Many;
import com.eagletsoft.boot.framework.data.json.meta.Many2Many;
import com.eagletsoft.boot.framework.data.json.meta.One;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Array;
import java.util.*;

public class JPALoader implements ILoader {
	@Autowired
	private SimpleQuery query;

	@Autowired(required = false)
	private JPACache cache;
	
	protected Object produceJSONObject(Object obj, String refId, String[] fieldset, Class wrapper) {
		Object ret = obj;
		if (null != obj && obj.getClass().isArray()) {
			Map<String, Object> objMap = new HashMap<>();
			Object ref = Array.get(obj, 0);
			objMap.put(refId, ref);
			int i = 1;
			for (String f :fieldset) {
				objMap.put(f, Array.get(obj, i++));
			}
			ret = objMap;
		}

		if (null != wrapper && !Void.class.equals(wrapper)) {
			return ExtViewHelper.convert(ret, wrapper);
		}
		else {
			return ret;
		}
	}
	
	@Override
	public Map<Object, Object> loadOneInBatch(Collection<Object> batch, One one) {
		if (batch.isEmpty()) {
			return MapUtils.EMPTY_MAP;
		}

		Map<Object, Object> map = null;
		if (null != cache) {
			map = (Map<Object, Object>)cache.get(batch, one);
		}

		if (null == map) {
			map = new HashMap<>();

			StringBuffer hql = new StringBuffer();

			String fieldset = " a ";
			if (one.fieldset().length > 0) {
				fieldset = one.ref();
				for (String f : one.fieldset()) {
					fieldset += ", " + f;
				}
			}
			String where = " WHERE " + one.ref() + " IN (:refs) ";
			if (one.filter().length() > 0) {
				where = where + one.filter();
			}

			hql.append("SELECT distinct ").append(fieldset).append(" FROM ").append(one.target().getName()).append(" a ").append(where);
			Map<String, Object> params = new HashMap<>();
			params.put("refs", batch);

			List<?> ret = query.query(hql.toString(), params, false);

			try {
				for (Object obj : ret) {
					Object jsonObj = produceJSONObject(obj, one.ref(), one.fieldset(), one.wrapper());
					map.put(PropertyUtils.getProperty(jsonObj, one.ref()), jsonObj);
				}
				if (null != cache) {
					cache.set(batch, one, map);
				}

			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		return map;
	}
	
	@Override
	public Object loadOne(Object value, One one) {

		Object ret = null;
		if (null != cache) {
			ret = cache.get(value, one);
		}

		if (null == ret) {

			StringBuffer hql = new StringBuffer();

			String fieldset = " a ";
			if (one.fieldset().length > 0) {
				fieldset = one.ref();
				for (String f : one.fieldset()) {
					fieldset += ", " + f;
				}
			}
			String where = " WHERE " + one.ref() + " = :ref ";
			if (one.filter().length() > 0) {
				where = where + one.filter();
			}

			hql.append("SELECT ").append(fieldset).append(" FROM ").append(one.target().getName()).append(" a ").append(where);
			Map<String, Object> params = new HashMap<>();
			params.put("ref", value);

			ret = query.queryOne(hql.toString(), params, false);
			if (null != cache) {
				cache.set(value, one, ret);
			}
		}
		return produceJSONObject(ret, one.ref(), one.fieldset(), one.wrapper());
	}

	@Override
	public List<?> loadMany(Object value, Many many) {

		StringBuffer hql = new StringBuffer();
		
		String fieldset = " a ";
		if (many.fieldset().length > 0) {
			fieldset = many.ref();
			for (String f : many.fieldset()) {
				fieldset += ", " + f;
			}
		}
		String where = " WHERE " + many.ref() + " = :ref ";
		if (many.filter().length() > 0) {
			where = where + many.filter();
		}

		hql.append("SELECT ").append(" a ").append(" FROM ").append(many.target().getName()).append(" a ").append(where);
		Map<String, Object> params = new HashMap<>();
		params.put("ref", value);
		
		List<?> list = query.query(hql.toString(), params, 0, many.size(), false);
		
		List ret = new ArrayList<>(list.size());
		for (Object obj : list) {
			ret.add(produceJSONObject(obj, many.ref(), many.fieldset(), many.wrapper()));
		}
		return ret;
	}

	@Override
	public Map<Object, List> loadManyInBatch(Collection<Object> batch, Many many) {
		return this.loadManyInBatch(batch, many.ref(), many.fieldset(), many.filter(), many.target(), many.size(), many.wrapper());
	}

	@Override
	public List<?> loadMany2Many(Object value, Many2Many m2m) {

		StringBuffer hql = new StringBuffer();
		
		String fieldset = " a ";
		if (m2m.fieldset().length > 0) {
			fieldset = "id";
			for (String f : m2m.fieldset()) {
				fieldset += ", " + f;
			}
		}
		String mediatorFilter = m2m.mediatorFilter();

		String where = " WHERE EXISTS (SELECT 1 FROM " + m2m.mediator().getName() + " m WHERE m." + m2m.targetRef()+ "= a.id AND m." + m2m.ref() + " = :ref" + mediatorFilter +")";
		if (m2m.filter().length() > 0) {
			where = where + m2m.filter();
		}

		hql.append("SELECT ").append(fieldset).append(" FROM ").append(m2m.target().getName()).append(" a ").append(where);
		Map<String, Object> params = new HashMap<>();
		params.put("ref", value);
		
		List<?> list = query.query(hql.toString(), params, 0, m2m.size(), false);
		
		List ret = new ArrayList<>(list.size());
		for (Object obj : list) {
			ret.add(produceJSONObject(obj, m2m.ref(), m2m.fieldset(), m2m.wrapper()));
		}
		return ret;
	}

	@Override
	public Map<Object, List> loadMany2ManyInBatch(Collection<Object> batch, Many2Many m2m) {
		if (batch.isEmpty()) {
			return MapUtils.EMPTY_MAP;
		}
		Map<Object, List> ret = new HashMap();
		try {

			String mediatorFilter = m2m.mediatorFilter();

			String hql = "SELECT a FROM " + m2m.mediator().getName() + " a WHERE " +
					" a." + m2m.ref() + " IN (:refs) " + mediatorFilter;
			Map<String, Object> params = new HashMap<>();
			params.put("refs", batch);

			List<?> list = query.query(hql, params,  0, m2m.size() * batch.size(), false);

			Map<Object, List> idMap = new HashMap<>();
			Set targetIds = new HashSet();

			for (Object obj : list) {
				Object srcId = PropertyUtils.getProperty(obj, m2m.ref());
				Object targetId = PropertyUtils.getProperty(obj, m2m.targetRef());
				List sub = idMap.get(srcId);
				if (null == sub) {
					sub = new ArrayList();
					idMap.put(srcId, sub);
				}
				if (sub.size() < m2m.size()) {
					sub.add(targetId);
					targetIds.add(targetId);
				}
			}

			Map<Object, List> manyRet = this.loadManyInBatch(batch, "id", m2m.fieldset(), m2m.filter(), m2m.target(), m2m.size(), m2m.wrapper());
			for (Object srcId: batch) {
				List mapIds = idMap.get(srcId);
				if (null != mapIds) {
					for (Object mapId : mapIds) {
						List sub = manyRet.get(mapId);
						if (null != sub) {
							List fullList = ret.get(srcId);
							if (null == fullList) {
								fullList = new ArrayList();
								ret.put(srcId, fullList);
							}
							fullList.addAll(sub);
						}
					}
				}
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return ret;
	}

	private Map<Object, List> loadManyInBatch(Collection<Object> batch, String ref, String[] fieldsetArr, String filter, Class target, int size, Class wrapper) {
		if (batch.isEmpty()) {
			return MapUtils.EMPTY_MAP;
		}
		Map<Object, List> ret = new HashMap();

		try {
			StringBuffer hql = new StringBuffer();

			String fieldset = " a ";
			if (fieldsetArr.length > 0) {
				fieldset = ref;
				for (String f : fieldsetArr) {
					fieldset += ", " + f;
				}
			}
			String where = " WHERE " + ref + " IN (:ref) ";
			if (StringUtils.isNotEmpty(filter)) {
				where = where + filter;
			}

			hql.append("SELECT distinct ").append(" a ").append(" FROM ").append(target.getName()).append(" a ").append(where);
			Map<String, Object> params = new HashMap<>();
			params.put("ref", batch);

			List<?> list = query.query(hql.toString(), params, 0, size * batch.size(), false);

			for (Object obj : list) {
				Object refValue = PropertyUtils.getProperty(obj, ref);
				List sub = ret.get(refValue);
				if (null == sub) {
					sub = new ArrayList();
					ret.put(refValue, sub);
				}
				if (sub.size() < size) {
					sub.add(produceJSONObject(obj, ref, fieldsetArr, wrapper));
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return ret;
	}


}
