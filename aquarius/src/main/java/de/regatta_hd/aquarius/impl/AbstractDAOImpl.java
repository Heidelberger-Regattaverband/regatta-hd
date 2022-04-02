package de.regatta_hd.aquarius.impl;

import java.util.List;

import com.google.inject.Inject;

import de.regatta_hd.commons.db.DBConnection;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

abstract class AbstractDAOImpl {

	@Inject
	protected DBConnection db;

	protected <T> List<T> getEntities(Class<T> entityClass) {
		CriteriaQuery<T> query = this.db.getEntityManager().getCriteriaBuilder().createQuery(entityClass);
		Root<T> from = query.from(entityClass);
		query.select(from);
		return this.db.getEntityManager().createQuery(query).getResultList();
	}
}
