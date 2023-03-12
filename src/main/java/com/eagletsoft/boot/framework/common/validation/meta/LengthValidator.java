package com.eagletsoft.boot.framework.common.validation.meta;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LengthValidator implements ConstraintValidator<Length, String> {
	private Length length;

	@Override
	public void initialize(Length arg) {
		this.length = arg;
	}

	@Override
	public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
		if (null == str) {
			return true;
		}
		if (str.length() < this.length.min() || str.length() > this.length.max()) {
			return false;
		}
		return true;
	}
}