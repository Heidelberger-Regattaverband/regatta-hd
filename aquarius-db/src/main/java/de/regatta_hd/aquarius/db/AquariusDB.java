package de.regatta_hd.aquarius.db;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

public interface AquariusDB {

	void close();

	CriteriaBuilder getCriteriaBuilder();

	EntityManager getEntityManager();

	boolean isOpen();

	void open(ConnectionData connectionData);

	void open(String hostName, String dbName, String userName, String password);
}
