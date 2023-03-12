package com.eagletsoft.boot.framework.data.repo;

import com.eagletsoft.boot.framework.data.filter.Criteria;
import com.eagletsoft.boot.framework.data.filter.PageResult;
import com.eagletsoft.boot.framework.data.filter.PageSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface IRepo<T> extends JpaRepository<T, Serializable>, JpaSpecificationExecutor<T> {
	PageResult<T> search(PageSearch search);
	PageResult<T> search(PageSearch search, Criteria.CriteriaModifier... cm);
	T findOne(Serializable id);
	T getOne(Serializable id);
	T newEntity();
}
