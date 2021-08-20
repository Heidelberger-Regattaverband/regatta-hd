package de.regatta_hd.aquarius.db.impl;

import java.util.List;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDB;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

abstract class AbstractDAOImpl {

	@Inject
	private AquariusDB aquariusDb;

	protected CriteriaBuilder getCriteriaBuilder() {
		return this.aquariusDb.getCriteriaBuilder();
	}

	protected <T> CriteriaQuery<T> createCriteriaQuery(Class<T> entityClass) {
		return getCriteriaBuilder().createQuery(entityClass);
	}

	protected <T> List<T> getEntities(Class<T> entityClass) {
		CriteriaQuery<T> query = createCriteriaQuery(entityClass);
		Root<T> from = query.from(entityClass);
		query.select(from);
		return createTypedQuery(query).getResultList();
	}

	protected <T> TypedQuery<T> createTypedQuery(CriteriaQuery<T> criteriaQuery) {
		return this.aquariusDb.getEntityManager().createQuery(criteriaQuery);
	}

	protected <T> T getEntity(Class<T> entityClass, Object id) {
		return this.aquariusDb.getEntityManager().find(entityClass, id);
	}

	protected void merge(Object entity) {
		this.aquariusDb.getEntityManager().merge(entity);
	}

	protected void persist(Object entity) {
		this.aquariusDb.getEntityManager().persist(entity);
	}
}
