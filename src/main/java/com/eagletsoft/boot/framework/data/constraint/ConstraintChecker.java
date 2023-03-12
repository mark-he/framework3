package com.eagletsoft.boot.framework.data.constraint;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.data.constraint.meta.Unique;
import com.eagletsoft.boot.framework.data.entity.Entity;
import com.eagletsoft.boot.framework.data.entity.SoftDelete;
import com.eagletsoft.boot.framework.data.filter.SimpleQuery;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;

@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
public class ConstraintChecker {
	@Autowired
	private SimpleQuery query;
	
	public boolean checkUnique(Entity entity, String... fields) {
		StringBuffer hql = new StringBuffer();
		hql.append("SELECT a FROM ").append(entity.getClass().getName()).append(" a ");

		Map<String, Object> params = new HashMap<>();
		
		try {
			if (fields.length > 0 && entity instanceof SoftDelete) {
				fields = ArrayUtils.add(fields, SoftDelete.FIELD);
			}
			for (int i = 0; i < fields.length; i++) {
				Object obj = PropertyUtils.getProperty(entity, fields[i]);
				if (null != obj && !StringUtils.isEmpty(obj.toString())) {
					if (i > 0) {
						hql.append(" AND ");
					}
					else {
						hql.append(" WHERE ");
					}
					hql.append(String.format("%s = :%s", fields[i], fields[i]));
					params.put(fields[i], obj);
				}
				else {
					//skip for any nulls
					return true;
				}
			}

			List<Entity> existings = (List<Entity>)query.query(hql.toString(), params, false);
			if (existings.size() == 0) {
				return true;
			} else if (existings.size() == 1) {
				return null != entity.getId() && existings.get(0).getId().equals(entity.getId());
			} else {
				return false;
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void checkUnique(Entity entity) {
		Collection<UniqueObj> list = this.findUniques(entity);
		if (null != list) {
			for (UniqueObj uniqueObj : list) {
				if (!this.checkUnique(entity, uniqueObj.getFields().toArray(new String[]{}))) {
					throw new ServiceException(StandardErrors.INTERNAL_ERROR.getStatus(), uniqueObj.message, uniqueObj.getPropertyPath());
				}
			}
		}
	}
	
	private Collection<UniqueObj> findUniques(Entity entity) {
		Field[] fields = entity.getClass().getDeclaredFields();
		
		Map<String, UniqueObj> map = null;
		Collection<UniqueObj> ret = null;

		for (Field f : fields) {
			Unique uq = AnnotationUtils.findAnnotation(f, Unique.class);
			if (null != uq) {
				if (null == ret) {
					ret = new ArrayList<>();
				}
				if (Unique.ALONE.equals(uq.value())) {
					UniqueObj obj = new UniqueObj();
					obj.setPropertyPath(entity.getClass().getSimpleName() + "." + f.getName());
					
					obj.getFields().add(f.getName());
					if (null != uq.with()) {
						obj.getFields().addAll(Arrays.asList(uq.with()));
					}
					obj.setMessage(uq.message());
					ret.add(obj);
					
				}
				else {
					if (null == map) {
						map = new HashMap<>();
					}
					UniqueObj obj = map.get(uq.value());
					if (null == obj) {
						obj = new UniqueObj();
						obj.setPropertyPath(entity.getClass().getSimpleName() + "." + f.getName());
					}
					obj.getFields().add(f.getName());
					if (StringUtils.isEmpty(obj.getMessage())) {
						obj.setMessage(uq.message());
					}
				}
			}
		}
		
		if (null != map) {
			ret.addAll(map.values());
		}
		return ret;
	}
	
	public static class UniqueObj {

		private String propertyPath;
		private List<String> fields = new ArrayList<>();
		private String message;
		public List<String> getFields() {
			return fields;
		}
		public void setFields(List<String> fields) {
			this.fields = fields;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getPropertyPath() {
			return propertyPath;
		}

		public void setPropertyPath(String propertyPath) {
			this.propertyPath = propertyPath;
		}
	}
}
