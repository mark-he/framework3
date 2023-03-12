package com.eagletsoft.boot.framework.data.repo.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;
import java.io.Serializable;

public class EntityRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable>
		extends JpaRepositoryFactoryBean<R, T, I> {

	public EntityRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
		super(repositoryInterface);
	}

	protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
		return new EntityRepositoryFactory(em);
	}

	private static class EntityRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {

		private final EntityManager em;

		public EntityRepositoryFactory(EntityManager em) {

			super(em);
			this.em = em;
		}

		@Override
		protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
			Class clazz = information.getDomainType();
			JpaRepositoryImplementation<?, ?> ret = super.getTargetRepository(information, entityManager);
			((BaseEntityDaoImpl<T>)ret).setDomainClass(clazz);
			return ret;
		}

		protected Object getTargetRepository(RepositoryMetadata metadata) {
			return new BaseEntityDaoImpl<T>((Class<T>) metadata.getDomainType(), em);
		}

		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
			return BaseEntityDaoImpl.class;
		}
	}
}