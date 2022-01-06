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
	protected AquariusDB db;

	protected CriteriaBuilder getCriteriaBuilder() {
		return this.db.getCriteriaBuilder();
	}

	protected <T> List<T> getEntities(Class<T> entityClass) {
		CriteriaQuery<T> query = getCriteriaBuilder().createQuery(entityClass);
		Root<T> from = query.from(entityClass);
		query.select(from);
		return createQuery(query).getResultList();
	}

	protected <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return this.db.getEntityManager().createQuery(criteriaQuery);
	}
}
