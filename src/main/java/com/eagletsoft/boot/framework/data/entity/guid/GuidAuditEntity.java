package com.eagletsoft.boot.framework.data.entity.guid;

import com.eagletsoft.boot.framework.data.entity.Audit;
import com.eagletsoft.boot.framework.data.filter.meta.Filtered;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public class GuidAuditEntity extends GuidEntity implements Audit<String> {

	@Filtered
	@CreatedDate
	protected Date createdTime;
	@Filtered
	@CreatedBy
	protected String createdBy;

	@Filtered
	@LastModifiedDate
	protected Date updatedTime;
	@Filtered
	@LastModifiedBy
	protected String updatedBy;

	@Override
	public Date getCreatedTime() {
		return createdTime;
	}

	@Override
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Date getUpdatedTime() {
		return updatedTime;
	}

	@Override
	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	@Override
	public String getUpdatedBy() {
		return updatedBy;
	}

	@Override
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}
