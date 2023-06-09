package com.eagletsoft.boot.framework.common.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class PasswordUtils {
	public static void main(String[] args) {
		System.out.println(encript("111111"));
	}

	public static String encript(String plainText) {
		if (StringUtils.isEmpty(plainText)) {
			return "";
		}
		try {
	        return DigestUtils.shaHex(plainText.getBytes("UTF-8"));
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String createRandomNumber(int len) {
		//35是因为数组是从0开始的，26个字母+10个数字
		final int  maxNum = 36;
		int i;  //生成的随机数
		int count = 0; //生成的密码的长度
		char[] str = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

		StringBuffer pwd = new StringBuffer("");
		Random r = new Random();
		while(count < len){
			//生成随机数，取绝对值，防止生成负数，

			i = Math.abs(r.nextInt(maxNum));  //生成的数最大为36-1

			if (i >= 0 && i < str.length) {
				pwd.append(str[i]);
				count ++;
			}
		}
		return pwd.toString();
	}

	public static String createRandom(int len) {
		//35是因为数组是从0开始的，26个字母+10个数字  
		  final int  maxNum = 36;  
		  int i;  //生成的随机数  
		  int count = 0; //生成的密码的长度  
		  char[] str = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',  
		    'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',  
		    'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };  
		    
		  StringBuffer pwd = new StringBuffer("");  
		  Random r = new Random();  
		  while(count < len){  
		   //生成随机数，取绝对值，防止生成负数，  
		     
		   i = Math.abs(r.nextInt(maxNum));  //生成的数最大为36-1  
		     
		   if (i >= 0 && i < str.length) {  
		    pwd.append(str[i]);  
		    count ++;  
		   }  
		  }  
		  return pwd.toString();  
	}

}
