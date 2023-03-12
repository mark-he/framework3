package com.eagletsoft.boot.framework.data.entity.guid;

import com.eagletsoft.boot.framework.data.entity.BaseEntity;
import com.eagletsoft.boot.framework.data.filter.meta.Filtered;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
//@EntityListeners({AuditingEntityListener.class})
public abstract class GuidEntity2 extends BaseEntity<String> {
    @Filtered
    //@CreatedDate
    protected Date createdTime;
    @Filtered
    //@CreatedBy
    protected String createdBy;

    @Id
    @GenericGenerator(name = "system-uuid", strategy = "com.eagletsoft.boot.framework.data.entity.guid.DualGenerator")
    @GeneratedValue(generator = "system-uuid")
    @Column(name = "ID")
    protected String id;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(BaseEntity o) {
        GuidEntity2 other = (GuidEntity2)o;
        return this.getId().compareTo(other.getId());
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
