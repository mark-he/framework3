package com.eagletsoft.boot.framework.data.fulltext;

import com.eagletsoft.boot.framework.data.entity.BaseEntity;

public interface IFulltextSearchEngine {

	void updateIndex(BaseEntity obj);

	void removeIndex(BaseEntity obj);

}