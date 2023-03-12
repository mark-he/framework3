package com.eagletsoft.boot.framework.data.entity;

import java.util.Date;

public interface Audit<S> {
	
	Date getCreatedTime();
	Date getUpdatedTime();
	
	void setCreatedTime(Date createTime);
	void setUpdatedTime(Date updateTime);
	
	
	S getUpdatedBy();
	S getCreatedBy();
	
	void setUpdatedBy(S by);
	void setCreatedBy(S by);
}
