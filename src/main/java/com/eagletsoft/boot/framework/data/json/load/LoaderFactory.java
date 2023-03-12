package com.eagletsoft.boot.framework.data.json.load;

import com.eagletsoft.boot.framework.common.utils.ApplicationUtils;

import java.util.HashMap;
import java.util.Map;

public class LoaderFactory {
	private static LoaderFactory INSTANCE = new LoaderFactory();
	private Map<String, ILoader> loaders = new HashMap<>();

	public static LoaderFactory getInstance() {
		return INSTANCE;
	}
	
	private LoaderFactory () {
	}
	
	public ILoader createLoader(Class clazz) {
		ILoader loader = loaders.get(clazz.getName());
		if (null == loader) {
			try
			{
				loader = (ILoader) ApplicationUtils.getBean(clazz);
				loaders.put(clazz.getName(), loader);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return loader;
	}

	public Map<String, ILoader> getLoaders() {
		return loaders;
	}

	public void setLoaders(Map<String, ILoader> loaders) {
		this.loaders = loaders;
	}
	
}
