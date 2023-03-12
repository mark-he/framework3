package com.eagletsoft.boot.framework.common.validation;

import com.eagletsoft.boot.framework.common.utils.PropertyCopyUtil;
import com.eagletsoft.boot.framework.common.validation.meta.ValidRef;
import org.apache.commons.beanutils.PropertyUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Aspect
public class ValidRefAspect {
	@Autowired
	private JSValidationFactoryBean validator;
	
	@Around(value = "@annotation(validRef)") 
	public Object around(ProceedingJoinPoint point, ValidRef validRef) throws Throwable {
		Parameter[] params = ((MethodSignature)point.getSignature()).getMethod().getParameters();
		int i = 0;
		Set<ConstraintViolation<Object>> allset = new HashSet<>();
		for (Parameter p : params) {
			Valid v = p.getAnnotation(Valid.class);
			if (null != v) {
				Object target = validRef.value().newInstance();
				Object obj = point.getArgs()[i];
				PropertyCopyUtil.getInstance().copyPropertiesWithIgnores(target, obj);
				
				if (obj instanceof Map) {
					for (Object key : ((Map) obj).keySet()) {
						if (PropertyUtils.isReadable(target, key.toString())) {
							Set<ConstraintViolation<Object>> set = validator.validateProperty(target, key.toString());
							if (set.size() > 0) {
								allset.addAll(set);
							}
						}
					}
				}
				else {
					Set<ConstraintViolation<Object>> set = validator.validate(target);
					if (set.size() > 0) {
						allset.addAll(set);
					}
				}
				break;
			}
			i++;
		}
		if (allset.size() > 0) {
			throw new ConstraintViolationException(allset);
		}
		return point.proceed();
	}
}