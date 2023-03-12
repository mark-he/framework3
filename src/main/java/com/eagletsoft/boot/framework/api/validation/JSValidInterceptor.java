package com.eagletsoft.boot.framework.api.validation;

import com.eagletsoft.boot.framework.api.utils.ApiResponse;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.eagletsoft.boot.framework.common.validation.JSValidationFactoryBean;
import com.eagletsoft.boot.framework.common.validation.meta.ValidRef;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSValidInterceptor implements HandlerInterceptor {
	private JSValidationFactoryBean validator;

	public JSValidInterceptor(JSValidationFactoryBean validator) {
		this.validator = validator;
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception arg3)
			throws Exception {
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler, ModelAndView mv) throws Exception {
	}

	private Type findClassType(Class bean, TypeVariable<?> var) {
		Map<TypeVariable<?>, Type> map = TypeUtils.getTypeArguments((ParameterizedType)bean.getGenericSuperclass());
		return map.get(var);
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
		if(handler instanceof HandlerMethod)
		{
			if (null != request.getQueryString() && request.getQueryString().contains("_valid=1")) {
				List constraints = new ArrayList();
				Class clazz = null;
				ValidRef vr = ((HandlerMethod) handler).getMethodAnnotation(ValidRef.class);
				if (null != vr) {
					clazz = vr.value();
				}

				if (null != clazz) {
					constraints.addAll(this.validator.findConstraints(clazz));
				}
				else {
					MethodParameter[] params = ((HandlerMethod) handler).getMethodParameters();
					for (MethodParameter p : params) {
						Object v = p.getParameterAnnotation(Validated.class);
						if (null == v) {
							v = p.getParameterAnnotation(Valid.class);
						}
						if (null != v) {
							Type t = p.getParameterType();
							if (t instanceof Class) {
								clazz = Class.forName(t.getTypeName());
							}
							else if (t instanceof TypeVariable){
								Type t2 = this.findClassType(((HandlerMethod) handler).getBean().getClass(), (TypeVariable)t);
								try {
									clazz = Class.forName(t2.getTypeName());	
								}
								catch (Exception ex) {
									
								}
							}
							if (null != clazz) {
								constraints.addAll(this.validator.findConstraints(clazz));
								clazz = null;
							}
						}
					}
				}
				
				response.getWriter().write(JsonUtils.writeValue(ApiResponse.make(constraints)));
				return false;
			}
		}
		return true;
	}
}
