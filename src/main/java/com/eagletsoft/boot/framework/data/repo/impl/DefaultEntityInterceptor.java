package com.eagletsoft.boot.framework.data.repo.impl;

import com.eagletsoft.boot.framework.common.session.UserSession;
import com.eagletsoft.boot.framework.common.utils.UuidUtils;
import com.eagletsoft.boot.framework.data.entity.Audit;
import com.eagletsoft.boot.framework.data.entity.uuid.UuidEntity;
import org.hibernate.EmptyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class DefaultEntityInterceptor extends EmptyInterceptor {
	private static final long serialVersionUID = 1L;
	private Logger LOG = LoggerFactory.getLogger(DefaultEntityInterceptor.class);

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
			org.hibernate.type.Type[] types) {
		boolean auditable = entity instanceof Audit;
		boolean uuid = entity instanceof UuidEntity;
		if (auditable || uuid) {
			return audit(true, state, propertyNames, auditable, uuid);
		}
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, org.hibernate.type.Type[] types) {
		boolean auditable = entity instanceof Audit;
		boolean uuid = entity instanceof UuidEntity;
		if (auditable || uuid) {
			return audit(null == id, currentState, propertyNames, auditable, uuid);
		}
		return false;
	}

	private boolean audit(boolean isNew, Object[] currentState, String[] propertyNames, boolean autable, boolean uuid) {
		int update = 0;
		Date current = Calendar.getInstance().getTime();
		for (int i = 0; i < propertyNames.length; i++) {
			if (autable) {
				if (isNew) {
					if ("createdTime".equals(propertyNames[i])) {
						currentState[i] = current;
						update++;
						continue;
					} else if ("createdBy".equals(propertyNames[i])) {
						if (UserSession.getAuthorize() != null) {
							currentState[i] = UserSession.getAuthorize().getUserId();
							update++;
						}
						continue;
					}
				}
				if ("updatedTime".equals(propertyNames[i])) {
					currentState[i] = current;
					update++;
					continue;
				} else if ("updatedBy".equals(propertyNames[i])) {
					if (UserSession.getAuthorize() != null) {
						currentState[i] = UserSession.getAuthorize().getUserId();
						update++;
					}
					continue;
				}
			}

			if (uuid && isNew) {
				if ("uuid".equals(propertyNames[i])) {
					if (null == currentState[i]) {
						currentState[i] = UuidUtils.getUUID();
						update++;
					}
					continue;
				}
			}
		}
		return update > 0;
	}

}
