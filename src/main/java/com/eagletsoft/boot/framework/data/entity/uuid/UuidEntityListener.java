package com.eagletsoft.boot.framework.data.entity.uuid;

import com.eagletsoft.boot.framework.common.utils.UuidUtils;

import javax.persistence.PrePersist;

public class UuidEntityListener {
    @PrePersist
    public void prePersist(Object object) {
        UuidEntity entity = (UuidEntity)object;
        if (null == entity.getUuid()) {
            entity.setUuid(UuidUtils.getUUID());
        }
    }
}
