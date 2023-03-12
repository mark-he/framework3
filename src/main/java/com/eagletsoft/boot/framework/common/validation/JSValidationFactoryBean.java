package com.eagletsoft.boot.framework.common.validation;

import org.springframework.beans.BeanUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JSValidationFactoryBean extends LocalValidatorFactoryBean {
	
	protected ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
	
	@Override
	public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		// TODO Auto-generated method stub
		BeanDescriptor bd = super.getConstraintsForClass(clazz);
		return bd;
	}

	public List<Constraint> findConstraints(Class clazz) {
		List<Constraint> ret = (List<Constraint>)cache.get(clazz.getName());
		if (null == ret) {
			ret = this.loadConstraints(clazz);
			cache.put(clazz.getName(), ret);
		}
		return ret;
	}

	protected List<Constraint> loadConstraints(Class clazz) {
		List<Constraint> constraints = new ArrayList<>();
		BeanDescriptor bd = this.getConstraintsForClass(clazz);
		Set<PropertyDescriptor> pds = bd.getConstrainedProperties();
		for (PropertyDescriptor pd : pds) {
			Set<ConstraintDescriptor<?>> cds = pd.getConstraintDescriptors();
			for (ConstraintDescriptor cd : cds) {
				Constraint c = new Constraint();
				c.name = pd.getPropertyName();
				c.type = cd.getAnnotation().annotationType().getSimpleName();
				c.atts.putAll(cd.getAttributes());

				String message = (String)c.atts.get("message");
				if (message.startsWith("{") && message.endsWith("}")) {
					message = message.substring(1, message.length() - 1);
				}
				c.message = message;

				c.atts.remove("message");
				constraints.add(c);
			}
		}
		return constraints;
	}

	private Map<String, String> getTypeMap(Class clazz) {
		Map<String, String> typeMap = new HashMap<>();
		java.beans.PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(clazz);
		
		for (java.beans.PropertyDescriptor pd : pds) {
			typeMap.put(pd.getName(), pd.getPropertyType().getName());
		}
		return typeMap;
	}
	
	public static class Constraint {
		private String type;
		private String name;
		private String message;
		private Map atts = new HashMap();
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public Map getAtts() {
			return atts;
		}
		public void setAtts(Map atts) {
			this.atts = atts;
		}
	}
}
