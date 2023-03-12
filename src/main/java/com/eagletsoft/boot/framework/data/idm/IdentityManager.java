package com.eagletsoft.boot.framework.data.idm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;

public class IdentityManager {
	private String tableName;

	@Autowired
	private AbstractEntityManagerFactoryBean emf;
	
	public long next(String id) {
		return findNIncrease(id);
	}

	public String next(String id, int size) {
		long newId = next(id);
		return StringUtils.leftPad("" + newId, size, '0');
	}
	
	private long findNIncrease(String id) {
		EntityManager em = null;
		List<?> rets = null;
		try {
			id = id.toUpperCase();
			
			em = emf.getObject().createEntityManager();
			em.getTransaction().begin();
			String test = "SELECT value FROM " + tableName + " WHERE id = :id FOR UPDATE";

			Query query = em.createNativeQuery(test);
			//query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
			query.setParameter("id", id);
			rets = query.getResultList();
			Long ret = null;
			if (rets.size() == 0) {
				test = "INSERT INTO " + tableName + " (id, value) VALUES(:id, 1)";
				ret = 1L;
			}
			else {
				test = "UPDATE " + tableName + " SET value = value + 1 WHERE id = :id";
				ret = ((BigInteger)rets.get(0)).longValue() + 1;
			}
			query = em.createNativeQuery(test);
			query.setParameter("id", id);
			query.executeUpdate();
			em.getTransaction().commit();
			return ret;
		}
		catch (Exception ex) {
			em.getTransaction().rollback();
			throw ex;
		}
		finally {
			em.close();
		}
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
