package de.regatta_hd.aquarius.db.impl;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.regatta_hd.aquarius.db.AquariusDB;

abstract class AbstractDAOImpl {

	@Inject
	private AquariusDB aquariusDb;
	
	protected <T> List<T> getEntities(Class<T> entityClass){
		CriteriaQuery<T> query = this.aquariusDb.getCriteriaBuilder().createQuery(entityClass);
		Root<T> from = query.from(entityClass);
		query.select(from);
		return this.aquariusDb.getEntityManager().createQuery(query).getResultList();
	}
	
	protected <T> T getEntity(Class<T> entityClass, Object id) {
		return this.aquariusDb.getEntityManager().getReference(entityClass, id);
	}
}
