package com.eagletsoft.boot.framework.data.fulltext;

import com.eagletsoft.boot.framework.common.utils.ApplicationUtils;
import com.eagletsoft.boot.framework.data.entity.BaseEntity;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;


public class HibernateIntegrator implements Integrator {

	private IFulltextSearchEngine engine;
	
	private IFulltextSearchEngine getEngine() {
		if (null == engine) {
			engine = ApplicationUtils.getBean(IFulltextSearchEngine.class, false);
		}
		return engine;
	}
	
	@Override
	public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
		
		if (null == engine) {
			return;
		}
		
		final EventListenerRegistry eventRegistry = serviceRegistry.getService(EventListenerRegistry.class);
		eventRegistry.appendListeners(EventType.POST_UPDATE, new PostUpdateEventListener() {
			@Override
			public void onPostUpdate(PostUpdateEvent event) {
				if (event.getEntity() instanceof BaseEntity) {
					getEngine().updateIndex((BaseEntity)event.getEntity());
				}
			}

			@Override
			public boolean requiresPostCommitHanding(EntityPersister persister) {
				return false;
			}
			
		});

		eventRegistry.appendListeners(EventType.POST_INSERT, new PostInsertEventListener() {
			@Override
			public void onPostInsert(PostInsertEvent event) {
				if (event.getEntity() instanceof BaseEntity) {
					getEngine().updateIndex((BaseEntity)event.getEntity());
				}
			}

			@Override
			public boolean requiresPostCommitHanding(EntityPersister persister) {
				return false;
			}
		});

		eventRegistry.appendListeners(EventType.POST_DELETE, new PostDeleteEventListener() {
			@Override
			public void onPostDelete(PostDeleteEvent event) {
				if (event.getEntity() instanceof BaseEntity) {
					getEngine().removeIndex((BaseEntity)event.getEntity());
				}				
			}

			@Override
			public boolean requiresPostCommitHanding(EntityPersister persister) {
				return false;
			}
		});
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

	}
}
