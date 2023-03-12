package com.eagletsoft.boot.framework.data.fulltext;

import com.eagletsoft.boot.framework.data.entity.BaseEntity;
import com.eagletsoft.boot.framework.data.entity.SoftDelete;
import com.eagletsoft.boot.framework.data.fulltext.meta.Fulltext;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashMap;
import java.util.Map;


public class DefaultFulltextSearchEngine implements IFulltextSearchEngine {
	private Logger LOG = LoggerFactory.getLogger(DefaultFulltextSearchEngine.class);
	
	private FulltextStore store;

	private Fulltext findFulltext(Class clazz) {
		return AnnotationUtils.getAnnotation(clazz, Fulltext.class);
	}
	
	private Object fetchRaw(BaseEntity obj, Fulltext fulltext) throws Exception {
		if (fulltext.dataFields().length == 0) {
			return obj;
		}
		else {
			Map map = new HashMap<>();
			
			for (String f : fulltext.dataFields()) {
				map.put(f, PropertyUtils.getProperty(obj, f));
			}
			return map;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.eagletsoft.framework.data.fulltext.IFulltextSearchEngine#updateIndex(com.eagletsoft.framework.data.BaseEntity)
	 */
	@Override
	public void updateIndex(BaseEntity obj) {
		
		Fulltext fulltext = this.findFulltext(obj.getClass());
		if (null == fulltext) {
			return;
		}
		
		if (obj instanceof SoftDelete) {
			if (((SoftDelete)obj).getDeleted() > 0) {
				removeIndex(obj);
			}
		}
		
		String[] fields = fulltext.fields();
		
		StringBuffer text = new StringBuffer();
		String title = null;
		try {
			title = String.valueOf(PropertyUtils.getProperty(obj, fulltext.title()));
			for (String f : fields) {
				Object value = PropertyUtils.getProperty(obj, f.trim());
				if (null != value) {
					if (text.length() > 0) {
						text.append(" ");
					}
					text.append(value);
				}
			}
			FulltextSearch entity = null;
			if(obj.getId() instanceof String) {
				entity = store.find(obj.getClass().getSimpleName(), (String)obj.getId());
			} else {
				entity = store.find(obj.getClass().getSimpleName(), ((Long)obj.getId()).toString());
			}
			
			
			if (null == entity) {
				entity = store.create();
			}
			entity.setType(obj.getClass().getSimpleName());
			entity.setRefNo(obj.getId().toString());
			entity.setTitle(title);
			entity.setBody(text.toString());
			entity.setRaw(this.fetchRaw(obj, fulltext));
			store.save(entity);
		}
		catch (Exception ex) {
			LOG.debug("Error in building index", ex);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.eagletsoft.framework.data.fulltext.IFulltextSearchEngine#removeIndex(com.eagletsoft.framework.data.BaseEntity)
	 */
	@Override
	public void removeIndex(BaseEntity obj) {
		Fulltext fulltext = this.findFulltext(obj.getClass());
		if (null == fulltext) {
			return;
		}
		FulltextSearch entity = store.find(obj.getClass().getSimpleName(), obj.getId().toString());
		
		if (null != entity) {
			store.delete(entity);
		}
	}

	public FulltextStore getStore() {
		return store;
	}

	public void setStore(FulltextStore store) {
		this.store = store;
	}
}
