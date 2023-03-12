package com.eagletsoft.boot.framework.data.fulltext.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Fulltext {
	String title();
	String[] fields() default {};
	String[] dataFields() default {};
}

/*

CREATE TABLE fulltext_search (
     idm BIGINT(20) AUTO_INCREMENT NOT NULL PRIMARY KEY,
     type VARCHAR(20),
     ref_no BIGINT(20),
     title VARCHAR(200),
     body TEXT,
     raw TEXT,
	 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	 `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
     FULLTEXT (title,body) WITH PARSER ngram,
	  
  	 UNIQUE KEY `DATA_UNIQUE` (`type`, `ref_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

 */