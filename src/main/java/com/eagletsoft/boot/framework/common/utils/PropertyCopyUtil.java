package com.eagletsoft.boot.framework.common.utils;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.ArrayUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class PropertyCopyUtil extends PropertyUtilsBean {

	private final static PropertyCopyUtil INSTANCE = new PropertyCopyUtil();

	public static PropertyCopyUtil getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void copyProperties(Object dest, Object orig) {
		this.copyPropertiesWithIgnores(dest, orig, "");
	}

	public void copyPropertiesWithNames(Object dest, Object orig, String... fields) {
		try {
			if (orig instanceof DynaBean) {
				DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass().getDynaProperties();
				for (int i = 0; i < origDescriptors.length; i++) {
					String name = origDescriptors[i].getName();
					if (isReadable(orig, name) && isWriteable(dest, name)
							&& ArrayUtils.contains(fields, name)) {

						Object value = ((DynaBean) orig).get(name);
						if (dest instanceof DynaBean) {
							((DynaBean) dest).set(name, value);
						} else {
							setSimpleProperty(dest, name, value);
						}
					}
				}
			} else if (orig instanceof Map) {
				Set<String> keySet = ((Map) orig).keySet();
				for (String name : keySet) {
					Object value = ((Map) orig).get(name);
					if (isWriteable(dest, name) && ArrayUtils.contains(fields, name)) {

						if (dest instanceof DynaBean) {
							((DynaBean) dest).set(name, value);
						} else {
							setSimpleMapProperty(dest, name, (Map) orig, value);
						}
					}
				}
			} else /* if (orig is a standard JavaBean) */ {
				PropertyDescriptor[] origDescriptors = getPropertyDescriptors(orig);
				for (int i = 0; i < origDescriptors.length; i++) {
					String name = origDescriptors[i].getName();
					if (isReadable(orig, name) && isWriteable(dest, name)
							&& ArrayUtils.contains(fields, name)) {

						Object value = getSimpleProperty(orig, name);
						if (dest instanceof DynaBean) {
							((DynaBean) dest).set(name, value);
						} else {
							setSimpleProperty(dest, name, value);
						}
					}
				}
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void copyPropertiesWithIgnores(Object dest, Object orig, String... ignores) {
		try {

			if (orig instanceof DynaBean) {
				DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass().getDynaProperties();
				for (int i = 0; i < origDescriptors.length; i++) {
					String name = origDescriptors[i].getName();
					if (isReadable(orig, name) && isWriteable(dest, name)
							&& (null == ignores || !ArrayUtils.contains(ignores, name))) {

						Object value = ((DynaBean) orig).get(name);
						if (dest instanceof DynaBean) {
							((DynaBean) dest).set(name, value);
						} else {
							setSimpleProperty(dest, name, value);
						}
					}
				}
			} else if (orig instanceof Map) {
				Set<String> keySet = ((Map) orig).keySet();
				for (String name : keySet) {
					Object value = ((Map) orig).get(name);
					if (isWriteable(dest, name) && (null == ignores || !ArrayUtils.contains(ignores, name))) {

						if (dest instanceof DynaBean) {
							((DynaBean) dest).set(name, value);
						} else {
							setSimpleMapProperty(dest, name, (Map) orig, value);
						}
					}
				}
			} else /* if (orig is a standard JavaBean) */ {
				PropertyDescriptor[] origDescriptors = getPropertyDescriptors(orig);
				for (int i = 0; i < origDescriptors.length; i++) {
					String name = origDescriptors[i].getName();
					if (isReadable(orig, name) && isWriteable(dest, name)
							&& (null == ignores || !ArrayUtils.contains(ignores, name))) {

						Object value = getSimpleProperty(orig, name);
						if (dest instanceof DynaBean) {
							((DynaBean) dest).set(name, value);
						} else {
							setSimpleProperty(dest, name, value);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}


	public void setSimpleMapProperty(Object bean, String name, Map map, Object value) {
		try {
			if (bean == null) {
				throw new IllegalArgumentException("No bean specified");
			}
			if (name == null) {
				throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
			}

			// Validate the syntax of the property name
			if (this.getResolver().hasNested(name)) {
				throw new IllegalArgumentException("Nested property names are not allowed: Property '" + name
						+ "' on bean class '" + bean.getClass() + "'");
			} else if (this.getResolver().isIndexed(name)) {
				throw new IllegalArgumentException("Indexed property names are not allowed: Property '" + name
						+ "' on bean class '" + bean.getClass() + "'");
			} else if (this.getResolver().isMapped(name)) {
				throw new IllegalArgumentException("Mapped property names are not allowed: Property '" + name
						+ "' on bean class '" + bean.getClass() + "'");
			}

			// Handle DynaBean instances specially
			if (bean instanceof DynaBean) {
				DynaProperty descriptor = ((DynaBean) bean).getDynaClass().getDynaProperty(name);
				if (descriptor == null) {
					throw new RuntimeException(
							"Unknown property '" + name + "' on dynaclass '" + ((DynaBean) bean).getDynaClass() + "'");
				}
				((DynaBean) bean).set(name, value);
				return;
			}

			// Retrieve the property setter method for the specified property
			PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
			if (descriptor == null) {
				throw new RuntimeException("Unknown property '" + name + "' on class '" + bean.getClass() + "'");
			}
			Method writeMethod = getWriteMethod(bean.getClass(), descriptor);
			if (writeMethod == null) {
				throw new RuntimeException(
						"Property '" + name + "' has no setter method in class '" + bean.getClass() + "'");
			}

			// Call the property setter method
			Object[] values = new Object[1];
			try {
				value = valueTransform(descriptor, value);
			}
			catch (Throwable t) {
				throw new IllegalArgumentException("Failed in transforming property '" + name
						+ "' on bean class '" + bean.getClass() + "'", t);
			}

			values[0] = value;

			invokeMethod(writeMethod, bean, values);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public Method getWriteMethod(Class clazz, PropertyDescriptor descriptor) {
		return (MethodUtils.getAccessibleMethod(clazz, descriptor.getWriteMethod()));
	}

	private Object invokeMethod(Method method, Object bean, Object[] values) {
		if (bean == null) {
			throw new IllegalArgumentException(
					"No bean specified " + "- this should have been checked before reaching this method");
		}

		try {
			return method.invoke(bean, values);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Object valueTransform(PropertyDescriptor descriptor, Object value) throws Throwable {
		Class type = descriptor.getPropertyType();
		if (null == value) {
			if (type.isPrimitive()) {
				if ("long".equals(type.getName())) {
					return 0L;
				}
				else if ("int".equals(type.getName())) {
					return 0;
				}
				else if ("short".equals(type.getName())) {
					return 0;
				}
				else if ("double".equals(type.getName())) {
					return 0d;
				}
				else if ("float".equals(type.getName())) {
					return 0f;
				}
				else if ("boolean".equals(type.getName())) {
					return false;
				}
			}
		}
		else {
			if (Long.class.equals(type) || "long".equals(type.getName())) {
				return valueToNumber(value).longValue();
			}
			else if (Integer.class.equals(type) || "int".equals(type.getName())) {
				return valueToNumber(value).intValue();
			}
			else if (Short.class.equals(type) || "short".equals(type.getName())) {
				return valueToNumber(value).shortValue();
			}
			else if (Double.class.equals(type) || "double".equals(type.getName())) {
				return valueToNumber(value).doubleValue();
			}
			else if (Float.class.equals(type) || "float".equals(type.getName())) {
				return valueToNumber(value).floatValue();
			}
			else if (BigDecimal.class.equals(type)) {
				return valueToBigDecimal(value);
			}
			else if (Boolean.class.equals(type) || "boolean".equals(type.getName())) {
				return valueToBoolean(value);
			}
			else if (Date.class.equals(type)) {
				return valueToDate(value);
			}
		}
		return value;
	}

	private Number valueToNumber(Object value) {
		if (value instanceof Number) {
			return (Number)value;
		}
		else {
			return Double.parseDouble(value.toString());
		}
	}
	
	private Boolean valueToBoolean(Object value) {
		if (value instanceof Number) {
			return ((Number)value).intValue() == 1;
		}
		else if (value.toString().equalsIgnoreCase("true") || 
				value.toString().equalsIgnoreCase("on") || 
				value.toString().equalsIgnoreCase("checked") ||
				value.toString().equalsIgnoreCase("1")) {
			return true;
		}
		return false;
	}
	
	private Date valueToDate(Object value) throws Throwable {
		if (value instanceof Number) {
			return new Date(valueToNumber(value).longValue());
		}
		else {
			return DateUtils.parse(value.toString(), DateUtils.DATETIME_FORMAT);
		}
	}
	
	private BigDecimal valueToBigDecimal(Object value) {
		return BigDecimal.valueOf((valueToNumber(value)).doubleValue());
	}
}
