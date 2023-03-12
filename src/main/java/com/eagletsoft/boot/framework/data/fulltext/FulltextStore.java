package com.eagletsoft.boot.framework.data.fulltext;

import java.util.List;

public interface FulltextStore<T extends FulltextSearch> {
	T find(String type, String refNo);
	T create();
	void save(T search);
	void delete(T search);
	List<T> search(String keywords, int maxResults);
	List<T> search(String type, String keywords, int maxResults);
}
