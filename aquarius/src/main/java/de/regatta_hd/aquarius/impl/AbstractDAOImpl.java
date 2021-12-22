package de.regatta_hd.aquarius.impl;

import java.util.List;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

abstract class AbstractDAOImpl {

	@Inject
	protected AquariusDB aquariusDb;

	protected CriteriaBuilder getCriteriaBuilder() {
		return this.aquariusDb.getCriteriaBuilder();
	}

	protected <T> List<T> getEntities(Class<T> entityClass) {
		CriteriaQuery<T> query = getCriteriaBuilder().createQuery(entityClass);
		Root<T> from = query.from(entityClass);
		query.select(from);
		return createQuery(query).getResultList();
	}

	protected <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return this.aquariusDb.getEntityManager().createQuery(criteriaQuery);
	}

	protected <T> T find(Class<T> entityClass, Object id) {
		return this.aquariusDb.getEntityManager().find(entityClass, id);
	}

	protected void merge(Object entity) {
		this.aquariusDb.getEntityManager().merge(entity);
	}

	protected void persist(Object entity) {
		this.aquariusDb.getEntityManager().persist(entity);
	}
}
