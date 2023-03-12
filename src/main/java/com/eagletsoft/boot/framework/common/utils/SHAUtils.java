package com.eagletsoft.boot.framework.common.utils;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SHAUtils {
    private static String CODE = "HmacSHA512";

    public static String generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(CODE);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] key = secretKey.getEncoded();
        return Hex.encodeHexString(key);
    }

    public static String digest(String content, String key) throws Exception {
        SecretKey restoreSecretKey = new SecretKeySpec(Hex.decodeHex(key),CODE);	//还原密钥
        Mac mac = Mac.getInstance(restoreSecretKey.getAlgorithm());		//实例化mac
        mac.init(restoreSecretKey);					//初始化mac
        byte[] digestBytes = mac.doFinal(content.getBytes());
        return Hex.encodeHexString(digestBytes);
    }

    public static class MessageDigester {
        private StringBuffer content = new StringBuffer();
        private String key;
        private long timestamp;

        public MessageDigester add(String message) {
            content.append(";").append(message);
            return this;
        }

        public MessageDigester setKey(String key) {
            this.key = key;
            return this;
        }

        public MessageDigester setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public String build() throws Exception {
            content.append(";").append(timestamp);
            return SHAUtils.digest(content.toString(), key);
        }
    }
}
