package com.eagletsoft.boot.framework.data.fulltext;

import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class DefaultFulltextSearchStore implements FulltextStore<FulltextSearch> {

	@Autowired
	protected AbstractEntityManagerFactoryBean emf;

	@Override
	public FulltextSearch find(String type, String refNo) {
		EntityManager em = null;
		List<?> rets = null;
		try {
			em = emf.getObject().createEntityManager();
			String test = "SELECT id FROM " + "fulltext_search" + " WHERE type = :type AND ref_no = :refNo";
			Query query = em.createNativeQuery(test);
			query.setParameter("type", type);
			query.setParameter("refNo", refNo);
			query.setMaxResults(1);
			rets = query.getResultList();
			
			if (!rets.isEmpty()) {
				FulltextSearch fs = new FulltextSearch();
				fs.setId(((BigInteger)rets.get(0)).longValue());
				fs.setType(type);
				fs.setRefNo(refNo);
				return fs;
			}
		}
		catch (Exception ex) {
			throw ex;
		}
		finally {
			em.close();
		}
		return null;
	}

	@Override
	public FulltextSearch create() {
		return new FulltextSearch();
	}

	@Override
	public void save(FulltextSearch search) {
		EntityManager em = null;
		try {
			em = emf.getObject().createEntityManager();
			em.getTransaction().begin();
			Query query = null;
			if (null == search.getId()) {
				query = em.createNativeQuery("INSERT INTO " + "fulltext_search" + " SET type = :type, ref_no = :refNo, title = :title, body = :body, raw =:raw, create_time = :now, update_time = :now ");
				query.setParameter("type", search.getType());
				query.setParameter("refNo", search.getRefNo());
				query.setParameter("title", search.getTitle());
				query.setParameter("body", search.getBody());
				if (null != search.getRaw()) {
					query.setParameter("raw", JsonUtils.writeValue(search.getRaw()));
				}
				else {
					query.setParameter("raw", null);
				}
				query.setParameter("now", new Date());
			}
			else {
				query = em.createNativeQuery("UPDATE " + "fulltext_search" + " SET type = :type, ref_no = :refNo, title = :title, body = :body, raw =:raw, update_time = :now "
						+ " WHERE id = :id");
				query.setParameter("type", search.getType());
				query.setParameter("refNo", search.getRefNo());
				query.setParameter("title", search.getTitle());
				query.setParameter("body", search.getBody());
				if (null != search.getRaw()) {
					query.setParameter("raw", JsonUtils.writeValue(search.getRaw()));
				}
				else {
					query.setParameter("raw", null);
				}
				query.setParameter("now", new Date());
				query.setParameter("id", search.getId());
			}
			query.executeUpdate();
			em.getTransaction().commit();
		}
		catch (Exception ex) {
			em.getTransaction().rollback();
			throw ex;
		}
		finally {
			em.close();
		}
	}

	@Override
	public void delete(FulltextSearch search) {
		EntityManager em = null;
		try {
			em = emf.getObject().createEntityManager();
			em.getTransaction().begin();
			Query query = null;
			query = em.createNativeQuery("DELETE FROM " + "fulltext_search" + " WHERE id = :id");
			query.setParameter("id", search.getId());
			query.executeUpdate();
			em.getTransaction().commit();
		}
		catch (Exception ex) {
			em.getTransaction().rollback();
			throw ex;
		}
		finally {
			em.close();
		}
	}

	@Override
	public List<FulltextSearch> search(String type, String keywords, int maxResults) {
		List<FulltextSearch> list = new LinkedList<>();
		EntityManager em = null;
		List<?> rets = null;
		try {
			em = emf.getObject().createEntityManager();
			String whereAppend = "";
			if (!StringUtils.isEmpty(type)) {
				whereAppend = " AND type = :type";
			}
			String test = "SELECT type, ref_no, title, body, raw,"
			+ "MATCH (title, body) AGAINST "
			+ "  (:keywords IN NATURAL LANGUAGE MODE) AS score "
			+ "  FROM fulltext_search WHERE MATCH (title, body) AGAINST "
			+ "  (:keywords IN NATURAL LANGUAGE MODE)"
			+ whereAppend
			+ " ORDER BY score DESC";
			
			Query query = em.createNativeQuery(test);
			query.setParameter("keywords", keywords);
			if (!StringUtils.isEmpty(type)) {
				query.setParameter("type", type);
			}
			query.setMaxResults(maxResults);
			rets = query.getResultList();
			
			for (Object data : rets) {
				Object[] objs = (Object[])data;
				
				FulltextSearchScore search = new FulltextSearchScore();
				search.setType(String.valueOf(objs[0]));
				search.setRefNo((String) objs[1]);
				search.setTitle(String.valueOf(objs[2]));
				search.setBody(String.valueOf(objs[3]));
				if (null != objs[4]) {
					search.setRaw(JsonUtils.readValue(String.valueOf(objs[4]), HashMap.class));
				}
				search.setScore((Double)objs[5]);
				
				list.add(search);
			}
		}
		catch (Exception ex) {
			throw ex;
		}
		finally {
			em.close();
		}
		
		return list;
	}

	@Override
	public List<FulltextSearch> search(String keywords, int maxResults) {
		List<FulltextSearch> list = new LinkedList<>();
		EntityManager em = null;
		List<?> rets = null;
		try {
			em = emf.getObject().createEntityManager();
			
			String test = "SELECT type, ref_no, title, body, raw,"
			+ "MATCH (title, body) AGAINST "
			+ "  (:keywords IN NATURAL LANGUAGE MODE) AS score "
			+ "  FROM fulltext_search WHERE MATCH (title, body) AGAINST "
			+ "  (:keywords IN NATURAL LANGUAGE MODE) ORDER BY score DESC";
			
			Query query = em.createNativeQuery(test);
			query.setParameter("keywords", keywords);
			query.setMaxResults(maxResults);
			rets = query.getResultList();
			
			for (Object data : rets) {
				Object[] objs = (Object[])data;
				
				FulltextSearchScore search = new FulltextSearchScore();
				search.setType(String.valueOf(objs[0]));
				search.setRefNo((String) objs[1]);
				search.setTitle(String.valueOf(objs[2]));
				search.setBody(String.valueOf(objs[3]));
				if (null != objs[4]) {
					search.setRaw(JsonUtils.readValue(String.valueOf(objs[4]), HashMap.class));
				}
				search.setScore((Double)objs[5]);
				
				list.add(search);
			}
		}
		catch (Exception ex) {
			throw ex;
		}
		finally {
			em.close();
		}
		
		return list;
	}
	
	public static class FulltextSearchScore extends FulltextSearch {
		private double score;

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}
	}
}
