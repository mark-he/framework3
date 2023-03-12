package com.eagletsoft.boot.framework.data.fulltext;

import com.eagletsoft.boot.framework.data.entity.general.LongEntity;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class FulltextSearch extends LongEntity {
	private String type;
	private String refNo;
	private String title;
	private String body;
	private Object raw;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getRefNo() {
		return refNo;
	}
	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Object getRaw() {
		return raw;
	}
	public void setRaw(Object raw) {
		this.raw = raw;
	}
}
