package com.eagletsoft.boot.framework.data.runtime;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class SystemSetting {
	private String tableName;

	@PersistenceContext
	private EntityManager entityManager;
	private Properties props = new Properties();
	public Object put(Object key, Object value) {
		return props.put(key, value);
	}

	public Object remove(Object key) {
		return props.remove(key);
	}

	public Set<Object> keySet() {
		return props.keySet();
	}

	public Object getOrDefault(Object key, Object defaultValue) {
		return props.getOrDefault(key, defaultValue);
	}

	@PostConstruct
	public void init() {
		this.sync();
	}

	public void sync() {
		List<?> rets = null;
		try {
			String test = "SELECT id, value FROM " + this.tableName;
			Query query = entityManager.createNativeQuery(test);
			rets = query.getResultList();
			Properties props = new Properties();
			for (Object ret : rets) {
				Object[] vs = (Object[])ret;
				if (null != vs[0] && null != vs[1]) {
					props.put(vs[0], vs[1]);
				}
			}
			this.props = props;
		}
		catch (Exception ex) {
			throw ex;
		}
		finally {
			entityManager.close();
		}
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
