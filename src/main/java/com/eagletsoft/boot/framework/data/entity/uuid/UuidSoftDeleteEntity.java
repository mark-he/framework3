package com.eagletsoft.boot.framework.data.entity.uuid;

import com.eagletsoft.boot.framework.data.entity.SoftDelete;
import com.eagletsoft.boot.framework.data.filter.meta.Filtered;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLDeleteAll;
import org.hibernate.annotations.Where;

import javax.persistence.MappedSuperclass;

//below SQL injection must be declared in real entity class.
@SQLDelete(
		sql = "UPDATE #{entityName} SET deleted = CURRENT_TIMESTAMP WHERE id = ?"
)
@SQLDeleteAll(
		sql = "UPDATE #{entityName} SET deleted = CURRENT_TIMESTAMP WHERE id = ?"
)
@Where(clause = "deleted = 0")
@MappedSuperclass
public class UuidSoftDeleteEntity extends UuidAuditEntity implements SoftDelete {
	@Filtered
	@JsonIgnore
	private long deleted;

	public long getDeleted() {
		return deleted;
	}

	public void setDeleted(long deleted) {
		this.deleted = deleted;
	}
}
