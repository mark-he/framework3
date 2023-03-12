package com.eagletsoft.boot.framework.data.entity.general;

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
//@EntityListeners({AuditingEntityListener.class})
public class AuditEntity extends LongEntity implements Audit<Long> {

	@Filtered
	//@CreatedDate
	protected Date createdTime;
	@Filtered
	//@CreatedBy
	protected Long createdBy;

	@Filtered
	//@LastModifiedDate
	protected Date updatedTime;
	@Filtered
	//@LastModifiedBy
	protected Long updatedBy;

	@Override
	public Date getCreatedTime() {
		return createdTime;
	}

	@Override
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	@Override
	public Long getCreatedBy() {
		return createdBy;
	}

	@Override
	public void setCreatedBy(Long createdBy) {
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
	public Long getUpdatedBy() {
		return updatedBy;
	}

	@Override
	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}
}
