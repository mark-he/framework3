package com.eagletsoft.boot.framework.cache;

import com.eagletsoft.boot.framework.cache.meta.Cache;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.fasterxml.jackson.databind.JavaType;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class CacheAspect {
	private static final Logger LOG = LoggerFactory.getLogger(CacheAspect.class);
	private ICache cache;

	@Around(value = "@annotation(ann)")
    public Object aroundMethod(final ProceedingJoinPoint pjd, Cache ann) throws Throwable {

		String key = ann.key();
		Signature sig = pjd.getSignature();
        MethodSignature msig = (MethodSignature) sig;
        Object target = pjd.getTarget();
        final Class<?> cls = msig.getReturnType();
		if (StringUtils.isEmpty(key)) {
	        key = new StringBuffer().append(target.getClass().getName()).append(':').append(msig.getName()).toString();
		}
		
		CacheData cacheData = null;
		try {
			if (null != ann.parametrizedClass() && Void.class != ann.parametrizedClass()) {
				if (ann.parameterClass().length > 0) {
					JavaType type = JsonUtils.makeParametrizedClass(ann.parametrizedClass(), ann.parameterClass());
					cacheData = cache.get(ann.sector(), key, pjd.getArgs());
				}
				else {
					cacheData = cache.get(ann.sector(), key, pjd.getArgs());
				}
			} else {
				cacheData = cache.get(ann.sector(), key, pjd.getArgs());
			}
		}
		catch (Exception ex) {
			LOG.debug("Failed on loading data from CACHE: " + key);
		}
		Object result;
		if (null == cacheData) {
			result = pjd.proceed();

			cacheData = new CacheData<>();
			cacheData.setData(result);
			cache.set(ann.sector(), key, pjd.getArgs(), cacheData, ann.value());
		}
		else {
			LOG.debug("Loading data from CACHE: " + key);
			result = cacheData.getData();
		}
        return result;
	}

	public ICache getCache() {
		return cache;
	}

	public void setCache(ICache cache) {
		this.cache = cache;
	}
}
