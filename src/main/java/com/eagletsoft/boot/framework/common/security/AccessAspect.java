package com.eagletsoft.boot.framework.common.security;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.common.security.meta.Access;
import com.eagletsoft.boot.framework.common.session.UserSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.LinkedList;
import java.util.List;

@Aspect
@Order(100)
public class AccessAspect {
	@Around(value = "@annotation(access)") 
	public Object around(ProceedingJoinPoint point, Access access) throws Throwable {
		if (access.disabled()) {
			throw new ServiceException(StandardErrors.AUTHORIZED_WRONG.getStatus(), "error.access.disabled");
		}
		Access classAccess = point.getTarget().getClass().getAnnotation(Access.class);
		String value = access.value();
		String p = null;
		boolean skipCheck = false;
		if (access.value().startsWith(".") || access.value().startsWith(":")) {
			if (null != classAccess) {
				String[] clsAccessArr = classAccess.value().split(",", -1);
				for (int i = 0; i < clsAccessArr.length; i++) {
					value = clsAccessArr[i].trim() + access.value();
					p = UserSession.findPermission(value);
					if (null != p) {
						break;
					}
				}
			} else {
				skipCheck = true;
			}
		} else {
			p = UserSession.findPermission(value);
		}

		if (null == p && !skipCheck)
		{
			throw new ServiceException(StandardErrors.AUTHORIZED_WRONG.getStatus(), "error.access.permit", value);
		}

		Object ret = point.proceed();

		return ret;
	}
}