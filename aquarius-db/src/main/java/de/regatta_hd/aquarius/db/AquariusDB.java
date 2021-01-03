package de.regatta_hd.aquarius.db;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

public interface AquariusDB {

	boolean isOpen();

	void open(String hostName, String dbName, String userName, String password);

	EntityManager getEntityManager();

	void close();
	
	CriteriaBuilder getCriteriaBuilder();
}
