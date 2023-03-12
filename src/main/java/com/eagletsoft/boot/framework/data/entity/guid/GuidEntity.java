package com.eagletsoft.boot.framework.data.entity.guid;

import com.eagletsoft.boot.framework.data.entity.BaseEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class GuidEntity extends BaseEntity<String> {

	@Id
	@GenericGenerator(name = "system-uuid", strategy = "com.eagletsoft.boot.framework.data.entity.guid.DualGenerator")
	@GeneratedValue(generator = "system-uuid")
	@Column(name = "ID")
	protected String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(BaseEntity o) {
		GuidEntity other = (GuidEntity)o;
		return this.getId().compareTo(other.getId());
	}
}
