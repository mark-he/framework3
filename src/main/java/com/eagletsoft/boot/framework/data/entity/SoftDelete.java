package com.eagletsoft.boot.framework.data.entity;

public interface SoftDelete {
    String FIELD = "deleted";


    long getDeleted();

    void setDeleted(long deleted);
}
