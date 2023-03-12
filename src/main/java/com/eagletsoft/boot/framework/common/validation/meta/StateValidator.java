package com.eagletsoft.boot.framework.common.validation.meta;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class StateValidator implements ConstraintValidator<State, String> {
	private State state;

	@Override
	public void initialize(State arg) {
		this.state = arg;
	}

	@Override
	public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
		if (StringUtils.isEmpty(str)) {
			return state.nullable();
		}
		Field[] fields = state.value().getDeclaredFields();
		try {
			for (Field field : fields) {
				if (field.get("").equals(str)) {
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}