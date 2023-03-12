package com.eagletsoft.boot.framework.data.json;

import com.eagletsoft.boot.framework.common.utils.PropertyCopyUtil;
import com.eagletsoft.boot.framework.data.json.context.ExtViewContext;
import com.eagletsoft.boot.framework.data.json.load.ILoader;
import com.eagletsoft.boot.framework.data.json.load.LoaderFactory;
import com.eagletsoft.boot.framework.data.json.meta.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ExtRetriever {
	
	private static ExtRetriever INSTANCE = new ExtRetriever();
	
	private ExtRetriever() {
	}
	
	public static ExtRetriever getInstance() {
		return INSTANCE;
	}
	
	private Object findValue(Object obj, boolean raw, String name) {
		try {
			if (raw) {
				return this.findFieldValue(obj, name);
			}
			else {
				return this.findMethodValue(obj, name);
			}
		}
		catch (Throwable ex) {
			return null;
		}
	}
	
	private Object findFieldValue(Object obj, String fieldName) {
		try {
			return PropertyUtils.getProperty(obj, fieldName);
		}
		catch (Throwable ex) {
			return null;
		}
	}

	private Object findMethodValue(Object obj, String methodName) {
		try {
			Method m = obj.getClass().getDeclaredMethod(methodName);
			return m.invoke(obj);
		}
		catch (Throwable ex) {
			return null;
		}
	}
	
	public void retrieve(Object bean, Object viewport) throws Exception {
		if (null != viewport) {
			if (!ExtViewContext.isTrack(bean)) {
				Collection<ExtendAnnotation> refs = ExtViewHelper.findExtendAnnotations(viewport.getClass());
				if (refs.isEmpty()) {
					return;
				}
				else {
					boolean inside = ExtViewContext.get().isInsideCollection();
					for (ExtendAnnotation f: refs) {
						if (f.getMapping() instanceof One) {
							One one = (One)f.getMapping();
							if (ExtViewContext.get().isInGroup(one.groups()) &&
									(!inside || one.batch())) {
								if (!isOkForOption(one.option(), bean)) {
									continue;
								}

								Object v =this.findValue(bean, f.raw, StringUtils.isEmpty(one.src()) ? f.getName() : one.src());
								if (null == v) {
									continue;
								}
								ILoader loader = LoaderFactory.getInstance().createLoader(one.loader());
								Object r = loader.loadOne(v, one);
								processMapping(bean, one, r);
							}
						}
						else if (f.getMapping() instanceof Many) {
							Many many = (Many)f.getMapping();

							if (ExtViewContext.get().isInGroup(many.groups()) &&
									(!inside || many.batch())) {
								if (!isOkForOption(many.option(), bean)) {
									continue;
								}

								Object v =this.findValue(bean, f.raw, StringUtils.isEmpty(many.src()) ? f.getName() : many.src());
								if (null == v) {
									continue;
								}
								ILoader loader = LoaderFactory.getInstance().createLoader(many.loader());

								List rs = loader.loadMany(v, many);
								ExtViewContext.addExtend(bean, many.value(), rs);
							}
						}
						else if (f.getMapping() instanceof Many2Many) {
							Many2Many m2m = (Many2Many)f.getMapping();
							if (ExtViewContext.get().isInGroup(m2m.groups()) &&
									(!inside || m2m.batch())) {
								if (!isOkForOption(m2m.option(), bean)) {
									continue;
								}

								Object v =this.findValue(bean, f.raw, StringUtils.isEmpty(m2m.src()) ? f.getName() : m2m.src());
								if (null == v) {
									continue;
								}
								ILoader loader = LoaderFactory.getInstance().createLoader(m2m.loader());

								List rs = loader.loadMany2Many(v, m2m);
								ExtViewContext.addExtend(bean, m2m.value(), rs);
							}
						}
					}
				}
			}
		}
	}
	
	public void retrieve(Collection col) throws Exception {
		ExtViewContext.get().addLevel();
		if (ExtViewContext.get().getLevel() <= ExtViewContext.get().getMaxDepth()) {
			if (!col.isEmpty()) {
				Object first = col.iterator().next();
				Object viewport = ExtViewportManager.getInstance().genViewportObject(first);
				if (null == viewport) {
					viewport = first;
				}
				Collection<ExtendAnnotation> annotations = ExtViewHelper.findExtendAnnotations(viewport.getClass());

				for (Object bean : col) {
					ExtViewContext.track(bean);
				}

				for (ExtendAnnotation f : annotations) {
					if (f.getMapping() instanceof One) {
						One one = (One)f.getMapping();
						ILoader loader = LoaderFactory.getInstance().createLoader(one.loader());

						if (ExtViewContext.get().isInGroup(one.groups()) && one.batch()) {
							Set<Object> values = new HashSet<>();

							for (Object bean : col) {
								if (isOkForOption(one.option(), bean)) {
									Object v = this.findValue(bean, f.raw, StringUtils.isEmpty(one.src()) ? f.getName() : one.src());
									if (null != v) {
										values.add(v);
									}
								}
							}
							Map batch = loader.loadOneInBatch(values, one);
							for (Object bean : col) {
								if (isOkForOption(one.option(), bean)) {
									Object v = this.findValue(bean, f.raw, StringUtils.isEmpty(one.src()) ? f.getName() : one.src());
									if (null != v) {
										Object r = batch.get(v);
										if (null != r) {
											processMapping(bean, one, r);
										}
									}
								}
							}
						}
					}
					else if (f.getMapping() instanceof Many) {
						Many many = (Many)f.getMapping();
						if (ExtViewContext.get().isInGroup(many.groups()) && many.batch()) {
							ILoader loader = LoaderFactory.getInstance().createLoader(many.loader());
							Set<Object> values = new HashSet<>();

							for (Object bean : col) {
								if (isOkForOption(many.option(), bean)) {
									Object v = this.findValue(bean, f.raw, StringUtils.isEmpty(many.src()) ? f.getName() : many.src());
									if (null != v) {
										values.add(v);
									}
								}
							}
							Map<Object, List> batch = loader.loadManyInBatch(values, many);
							for (Object bean : col) {
								if (isOkForOption(many.option(), bean)) {
									Object v = this.findValue(bean, f.raw, StringUtils.isEmpty(many.src()) ? f.getName() : many.src());
									if (null != v) {
										List list = batch.get(v);
										if (null != list) {
											ExtViewContext.addExtend(bean, many.value(), list);
										}
									}
								}
							}
						}
					}
					else if (f.getMapping() instanceof Many2Many) {
						Many2Many m2m = (Many2Many)f.getMapping();
						if (ExtViewContext.get().isInGroup(m2m.groups()) && m2m.batch()) {
							ILoader loader = LoaderFactory.getInstance().createLoader(m2m.loader());
							Set<Object> values = new HashSet<>();

							for (Object bean : col) {
								if (isOkForOption(m2m.option(), bean)) {
									Object v = this.findValue(bean, f.raw, StringUtils.isEmpty(m2m.src()) ? f.getName() : m2m.src());
									if (null != v) {
										values.add(v);
									}
								}
							}
							Map<Object, List> batch = loader.loadMany2ManyInBatch(values, m2m);
							for (Object bean : col) {
								if (isOkForOption(m2m.option(), bean)) {
									Object v = this.findValue(bean, f.raw, StringUtils.isEmpty(m2m.src()) ? f.getName() : m2m.src());
									if (null != v) {
										List list = batch.get(v);
										if (null != list) {
											ExtViewContext.addExtend(bean, m2m.value(), list);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		ExtViewContext.get().subLevel();
	}

	private Collection<ExtendAnnotation> buildExtendAnnotations(Collection<ExtendAnnotation> refs, Annotation[] as, String name) {
		for (Annotation a : as) {
			if (a instanceof One || a instanceof Many || a instanceof Many2Many) {
				if (refs == null) {
					refs = new ArrayList<>();
				}
				refs.add(new ExtendAnnotation(true, name, a));
			} else if (a instanceof Ones) {
				if (refs == null) {
					refs = new ArrayList<>();
				}
				Ones ones = (Ones)a;
				for (One o : ones.value()) {
					refs.add(new ExtendAnnotation(true, name, o));
				}
			}
			else if (a instanceof Manys) {
				if (refs == null) {
					refs = new ArrayList<>();
				}
				Manys manys = (Manys)a;
				for (Many o : manys.value()) {
					refs.add(new ExtendAnnotation(true, name, o));
				}
			}
		}
		return refs;
	}

	private boolean isOkForOption(String[] option, Object bean) throws Exception {
		if (option.length == 2) {
			String key = option[0];
			String value = option[1];

			Object property = PropertyUtils.getProperty(bean, key);
			if (!value.equals(property)) {
				return false;
			}
		}
		return true;
	}

	private void processMapping(Object bean, One one, Object value) throws  Exception {
		if (one.mapping().length == 0) {
			ExtViewContext.addExtend(bean, one.value(), value);
		} else {
			for (Object m : one.mapping()) {
				String[] ms = m.toString().split("=");
				if (ms.length > 1) {
					ExtViewContext.addExtend(bean, ms[0], PropertyUtils.getProperty(value, ms[1]));
				} else {
					ExtViewContext.addExtend(bean, ms[0], PropertyUtils.getProperty(value, ms[0]));
				}
			}
		}
	}
}
