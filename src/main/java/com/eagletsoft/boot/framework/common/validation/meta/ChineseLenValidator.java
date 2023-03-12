package com.eagletsoft.boot.framework.common.validation.meta;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class ChineseLenValidator implements ConstraintValidator<ChineseLength, String> {
	private ChineseLength length;

	@Override
	public void initialize(ChineseLength arg) {
		this.length = arg;
	}

	@Override
	public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
		if (null == str) {
			return true;
		}
		int chineseLen = 0;
		try {
			chineseLen = getChineseLength(str, "UTF-8");
		}
		catch (Exception ex) {
			throw new ServiceException(StandardErrors.INTERNAL_ERROR.getStatus(), "Failed to parse Chinese text");
		}
		
		if (chineseLen <= length.value() && chineseLen >= length.min()) {
			return true;
		}
		return false;
	}


	public static int getChineseLength(String name, String endcoding) throws Exception {
		if (StringUtils.isEmpty(name)) {
			return 0;
		}

		int len = 0; // 定义返回的字符串长度
		int j = 0;
		// 按照指定编码得到byte[]
		byte[] b_name = name.getBytes(endcoding);
		while (true) {
			short tmpst = (short) (b_name[j] & 0xF0);
			if (tmpst >= 0xB0) {
				if (tmpst < 0xC0) {
					j += 2;
					len += 2;
				} else if ((tmpst == 0xC0) || (tmpst == 0xD0)) {
					j += 2;
					len += 2;
				} else if (tmpst == 0xE0) {
					j += 3;
					len += 2;
				} else if (tmpst == 0xF0) {
					short tmpst0 = (short) (((short) b_name[j]) & 0x0F);
					if (tmpst0 == 0) {
						j += 4;
						len += 2;
					} else if ((tmpst0 > 0) && (tmpst0 < 12)) {
						j += 5;
						len += 2;
					} else if (tmpst0 > 11) {
						j += 6;
						len += 2;
					}
				}
			} else {
				j += 1;
				len += 1;
			}
			if (j > b_name.length - 1) {
				break;
			}
		}
		return len;
	}
}