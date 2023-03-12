package com.eagletsoft.boot.framework.data.service;

import com.eagletsoft.boot.framework.data.filter.PageResult;
import com.eagletsoft.boot.framework.data.filter.PageSearch;

import java.io.Serializable;
import java.util.List;

public interface ICRUDService<T> {
	T create(Object data);
	T save(T data);
	T find(Serializable id);
	T update(Serializable id, Object data);
	void delete(Serializable id);
	List<T> readAll();
	PageResult<T> search(PageSearch ps);
}
