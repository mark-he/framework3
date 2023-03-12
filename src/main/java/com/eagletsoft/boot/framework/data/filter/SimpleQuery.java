package com.eagletsoft.boot.framework.data.filter;

import com.eagletsoft.boot.framework.data.utils.BeanTransformer;
import org.omg.CORBA.OBJECT_NOT_EXIST;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleQuery {
	private EntityManager entityManager;
	private QueryAdapter adapter = new QueryAdapter() {
		@Override
		public void applyTransform(Query query, Class clazz) {
			new BeanTransformer(clazz).apply(query);
		}
	};
	public EntityManager getEntityManager() {
		return this.entityManager;
	}
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return entityManager.find(entityClass, primaryKey);
	}

	private Map<String, Object> filterParams(String hql, Map<String, Object> parameters) {
		Map<String, Object> filtered = new HashMap<>();

		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			if (ifMatchKey(hql, entry.getKey())) {
				filtered.put(entry.getKey(), entry.getValue());
			}
		}
		return filtered;
	}

	public int update(String hql, Map<String, Object> parameters, boolean isNative) {
		Query query = createQuery(hql, parameters, isNative);
		return query.executeUpdate();
	}
	
	public Long count(String hql, Map<String, Object> parameters, boolean isNative)
	{
		Query query = createQuery(hql, parameters, isNative);
		return getLongValue(query.getSingleResult());
	}

	private long getLongValue(Object obj) {
		if (obj instanceof BigInteger) {
			return ((BigInteger) obj).longValue();
		} else {
			return (Long)obj;
		}
	}

	public Query createQuery(String hql, Map<String, Object> parameters, boolean isNative)
	{
		if (null != parameters) {
			parameters = filterParams(hql, parameters);
		}

		Query query = null;
		if (isNative) {
			query = entityManager.createNativeQuery(hql);
			for (String key: parameters.keySet())
			{
				query.setParameter(key, parameters.get(key));
			}
		} else {
			query = entityManager.createQuery(hql);
			for (String key: parameters.keySet())
			{
				query.setParameter(key, parameters.get(key));
			}
		}

		return query;
	}

	public Query createQuery(String hql, Map<String, Object> parameters,  int from, int size, boolean isNative)
	{
		Query query = createQuery(hql, parameters, isNative);
		if (from >= 0) {
			query.setFirstResult(from);
		}
		if (size > 0) {
			query.setMaxResults(size);
		}
		return query;
	}

	public Object queryOne(String hql, Map<String, Object> params, boolean isNative) {
		return this.queryOne(hql, params, isNative, null);
	}

	public <T> T queryOne(String hql, Map<String, Object> params, boolean isNative, Class<T> clazz) {
		List list = query(hql, params, 0, 1, isNative, clazz);
		if (list.isEmpty()) {
			return null;
		}
		else {
			return (T)list.get(0);
		}
	}

	public List query(String hql, Map<String, Object> params, boolean isNative) {
		return this.query(hql, params, isNative, null);
	}

	public <T> List<T> query(String hql, Map<String, Object> params, boolean isNative, Class<T> clazz) {
		Query query = this.createQuery(hql, params, isNative);
		if (null != clazz) {
			this.adapter.applyTransform(query, clazz);
		}
		return query.getResultList();
	}

	public List query(String hql, Map<String, Object> params, int from, int size, boolean isNative) {
		return this.query(hql, params, from, size, isNative, null);
	}

	public List query(String hql, Map<String, Object> params, int from, int size, boolean isNative, Class clazz) {
		Query query = this.createQuery(hql, params, from, size, isNative);
		if (null != clazz) {
			this.adapter.applyTransform(query, clazz);
		}
		return query.getResultList();
	}

	public PageResult queryPage(String hql, Map<String, Object> params, int page, int size, boolean isNative) {
		return this.queryPage(hql, params, page, size, isNative, null);
	}

	public PageResult queryPage(String hql, Map<String, Object> params, int page, int size, boolean isNative, Class clazz) {
		PageResult result = new PageResult();
		result.setPage(page);
		result.setSize(size);

		String upperHQL = hql.toUpperCase();
		int index = findFROM(hql);
		String countHql = "SELECT COUNT(*) " + hql.substring(index);
		Long count = this.count(countHql, params, isNative);
		result.setTotal(count);

		List rows = query(hql, params, size * page, size, isNative, clazz);
		result.setRows(rows);

		return result;
	}

	private static int findFROM(String hql) {
		String upperHQL = hql.toUpperCase();
		int index = upperHQL.indexOf("FROM");
		int i = 0;
		int braceCount = 0;
		for (; i < hql.length(); i++) {
			char c = hql.charAt(i);
			if (c == '(') {
				if (braceCount == 0 && i > index) {
					return index;
				}
				braceCount++;
			} else if (c == ')') {
				braceCount --;
				index = upperHQL.indexOf("FROM", i);
			}
		}

		return index;
	}

	private static boolean ifMatchKey(String hql, String key) {
		return hql.indexOf(":" + key) + key.length() + 1== hql.length() ||
				hql.indexOf(":" + key + " ") > -1 ||
				hql.indexOf(":" + key + ";") > -1 ||
				hql.indexOf(":" + key + ")") > -1;
	}

	public void setAdapter(QueryAdapter adapter) {
		this.adapter = adapter;
	}
}
