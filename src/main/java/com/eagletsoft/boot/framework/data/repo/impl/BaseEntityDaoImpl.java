package com.eagletsoft.boot.framework.data.repo.impl;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.data.entity.SoftDelete;
import com.eagletsoft.boot.framework.data.filter.Criteria;
import com.eagletsoft.boot.framework.data.filter.PageResult;
import com.eagletsoft.boot.framework.data.filter.PageSearch;
import com.eagletsoft.boot.framework.data.filter.ValueFilter;
import com.eagletsoft.boot.framework.data.repo.IRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Optional;

public class BaseEntityDaoImpl<T>  extends SimpleJpaRepository<T, Serializable> implements IRepo<T> {

	private Class<T> domainClass;
	public BaseEntityDaoImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
	}

	public BaseEntityDaoImpl(Class<T> domainClass, EntityManager em) {
		super(domainClass, em);
		this.domainClass = domainClass;
	}

	public void setDomainClass(Class<T> domainClass) {
		this.domainClass = domainClass;
	}

	@Override
	public T newEntity() {
		try {
			return domainClass.newInstance();
		}
		catch (Exception ex) {
			throw new ServiceException(ex);
		}
	}

	@Override
	public PageResult<T> search(PageSearch search, Criteria.CriteriaModifier... cm) {
		PageRequest req = new PageRequest(search.getPage(), search.getSize());
		if (SoftDelete.class.isAssignableFrom(this.getDomainClass())) {
			search.getFilters().add(new ValueFilter(SoftDelete.FIELD, ValueFilter.OP_EQ, 0L));
		}
		Page<T> page = this.findAll(new Criteria<T>(search, cm), req);

		return PageResult.from(page);
	}

	@Override
	public PageResult<T> search(PageSearch search) {
		return this.search(search, new Criteria.CriteriaModifier[0]);
	}

	public void deleteInIdBatch(Iterable<Serializable> ids)
	{
		for (Serializable id : ids)
		{
			super.deleteById(id);
		}
	}

	@Override
	public void deleteById(Serializable id) {
		T entity = this.findOne(id);
		if (null != entity) {
			this.delete(entity);
		}
	}

	@Override
	public void delete(T entity) {
		if (entity instanceof SoftDelete) {
			((SoftDelete) entity).setDeleted(System.currentTimeMillis());
			this.save(entity);
		}
		else {
			super.delete(entity);
		}
	}

	@Override
	public T findOne(Serializable id) {
		Optional<T> optional = this.findById(id);
		return optional.isPresent()? optional.get() : null;
	}

	@Override
	public T getOne(Serializable id) {
		Optional<T> optional = this.findById(id);
		if (!optional.isPresent()) {
			throw new ServiceException(StandardErrors.CLIENT_ERROR.getStatus(), "error.entity.missed").setNamespace(domainClass.getSimpleName());
		}
		return optional.get();
	}
}
