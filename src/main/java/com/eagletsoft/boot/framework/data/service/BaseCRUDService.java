package com.eagletsoft.boot.framework.data.service;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.common.utils.PropertyCopyUtil;
import com.eagletsoft.boot.framework.data.constraint.ConstraintChecker;
import com.eagletsoft.boot.framework.data.entity.Entity;
import com.eagletsoft.boot.framework.data.filter.PageResult;
import com.eagletsoft.boot.framework.data.filter.PageSearch;
import com.eagletsoft.boot.framework.data.repo.IRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Transactional(rollbackFor = {Exception.class})
public abstract class BaseCRUDService<T extends Entity> implements ICRUDService<T> {

	protected abstract IRepo<T> getRepo();

	@Autowired
	protected ConstraintChecker cc;
	
	@Override
	public T create(Object data) {
		T entity = createEntity(data);
		doInternalCreate(entity, data);
		return entity;
	}

	public void doInternalCreate(T entity, Object data) {
		onCreate(entity);
		entity = save(entity);
		afterCreated(entity);
		moreAfterCreated(entity, data);
	}

	public T createEntity(Object data) {
		T entity = getRepo().newEntity();
		mergeData(data, entity);
		return entity;
	}

	public T updateEntity(Serializable id, Object data) {
		T entity = getRepo().findOne(id);
		if (null == entity) {
			throw new ServiceException(StandardErrors.INTERNAL_ERROR.getStatus(), "error.framework.db.find", id);
		}
		mergeData(data, entity);
		return entity;
	}

	public void mergeData(Object src, T target) {
		try {
			PropertyCopyUtil.getInstance().copyPropertiesWithIgnores(target, src, "id", "createdTime", "createdBy", "updatedTime", "updatedBy", "deleted");
		}
		catch (Exception ex) {
			throw new ServiceException(StandardErrors.INTERNAL_ERROR.getStatus(), ex.getMessage());
		}
	}
	
	protected void onCreate(T entity) {
		
	}
	
	protected void afterCreated(T entity) {
		
	}

	protected void moreAfterCreated(T entity, Object data) {

	}

	protected void onUpdate(T entity) {
		
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
	
	@Override
	public T find(Serializable id) {
		T entity = getRepo().findOne(id);;
		onRead(entity);
		return entity;
	}
	
	@Override
	public T update(Serializable id, Object data) {
		T entity = this.updateEntity(id, data);
		this.doInternalUpdate(entity, data);
		return entity;
	}

	public void doInternalUpdate(T entity, Object data) {
		onUpdate(entity);
		entity = this.save(entity);
		afterUpdated(entity);
		moreAfterUpdated(entity, data);
	}

	@Override
	public T save(T entity) {
		cc.checkUnique(entity);
		onSave(entity);
		entity = getRepo().save(entity);
		afterSaved(entity);
		return entity;
	}

	public T createOrUpdate(T entity) {
		if (null == entity.getId()) {
			return this.create(entity);
		}
		else {
			return this.save(entity);
		}
	}

	@Override
	public void delete(Serializable id) {
		onDelete(id);
		getRepo().deleteById(id);
	}

	@Override
	public List<T> readAll() {
		return getRepo().findAll();
	}

	@Override
	public PageResult<T> search(PageSearch ps) {
		return getRepo().search(ps);
	}
}
