package com.eagletsoft.boot.framework.data.service;

import com.eagletsoft.boot.framework.data.entity.Entity;
import com.eagletsoft.boot.framework.data.filter.PageResult;
import com.eagletsoft.boot.framework.data.filter.PageSearch;

import java.io.Serializable;
import java.util.List;

public abstract class VariantCRUDService <T extends Entity> implements ICRUDService<T> {

    protected abstract BaseCRUDService<T> getInner();

    @Override
    public T create(Object data) {
        T entity = getInner().createEntity(data);
        onCreate(entity);
        onSave(entity);
        getInner().doInternalCreate(entity, data);
        afterSaved(entity);
        afterCreated(entity);
        moreAfterCreated(entity, data);
        return entity;
    }

    @Override
    public T save(T data) {
        onSave(data);
        T entity = getInner().save(data);
        afterSaved(entity);
        return entity;
    }

    @Override
    public T find(Serializable id) {
        T entity =  getInner().find(id);
        onRead(entity);

        return entity;
    }

    @Override
    public T update(Serializable id, Object data) {
        T entity = this.getInner().updateEntity(id, data);
        onUpdate(entity);
        onSave(entity);
        this.getInner().doInternalUpdate(entity, data);
        afterSaved(entity);
        afterUpdated(entity);
        moreAfterUpdated(entity, data);
        return entity;
    }

    @Override
    public void delete(Serializable id) {
        onDelete(id);
        getInner().delete(id);
    }

    @Override
    public List<T> readAll() {
        return getInner().readAll();
    }

    @Override
    public PageResult<T> search(PageSearch ps) {
        return getInner().search(ps);
    }

    protected void onCreate(T data) {

    }

    protected void afterCreated(T entity) {

    }

    protected void moreAfterCreated(T entity, Object data) {

    }

    protected void onUpdate(T data) {

    }

    protected void afterUpdated(T entity) {

    }

    protected void moreAfterUpdated(T entity, Object data) {

    }

    protected void onDelete(Serializable id) {

    }

    protected void onSave(T entity) {

    }

    protected void afterSaved(T entity) {

    }

    protected void onRead(T entity) {

    }
}
