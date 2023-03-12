package com.eagletsoft.boot.framework.data.entity.uuid;

import com.eagletsoft.boot.framework.data.entity.BaseEntity;
import com.eagletsoft.boot.framework.data.entity.general.LongEntity;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@EntityListeners({UuidEntityListener.class})
public class UuidEntity extends LongEntity {
	@Column(name = "uuid", updatable=false)
	protected String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public int compareTo(BaseEntity o) {
		UuidEntity other = (UuidEntity)o;
		return this.getId().compareTo(other.getId());
	}
}
