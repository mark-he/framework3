package com.eagletsoft.boot.framework.data.json;

import com.eagletsoft.boot.framework.common.utils.BeanUtils;
import com.eagletsoft.boot.framework.data.json.context.ExtViewContext;
import com.eagletsoft.boot.framework.data.json.context.KeyValue;
import com.eagletsoft.boot.framework.data.json.load.Calculator;
import com.eagletsoft.boot.framework.data.json.load.CalculatorFactory;
import com.eagletsoft.boot.framework.data.json.meta.Formula;
import ognl.Ognl;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FormulaDedutor {
	public static void deduce(Object bean, Object viewport, Collection<KeyValue> extendObjs) {
		Set<Formula> formulas = AnnotationUtils.getDeclaredRepeatableAnnotations(viewport.getClass(), Formula.class);
		try {
			boolean inside = ExtViewContext.get().isInsideCollection();
			for (Formula formula : formulas) {
				if (ExtViewContext.get().isInGroup(formula.groups()) &&
						(!inside || formula.batch())) {
					Object value = null;
					if (StringUtils.isNotEmpty(formula.expression())) {
						value = Ognl.getValue(formula.expression(), bean);
					}
					else {
						Class<Calculator> clazz = (Class<Calculator>)formula.calc();
						if (null != clazz) {
							Calculator calc = CalculatorFactory.create(clazz);
							value = calc.calc(bean, extendObjs);
						}
					}
					if (null != value) {
						ExtViewContext.addExtend(bean, formula.value(), value);
					}
				}
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
