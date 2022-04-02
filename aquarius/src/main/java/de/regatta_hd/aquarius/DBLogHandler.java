package de.regatta_hd.aquarius;

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Handler;

import org.apache.commons.lang3.ArrayUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.commons.ListenerManager;
import de.regatta_hd.commons.db.DBConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

@Singleton
public class DBLogHandler extends Handler {

	private static final String[] FILTERED_CLASSES = { DBLogHandler.class.getName(), LogRecord.class.getName() };

	private final DBConnection db;

	private final Deque<LogRecord> logRecords = new LinkedList<>();

	@Inject
	@Named("hostName")
	private String hostName;

	@Inject
	@Named("hostAddress")
	private String hostAddress;

	@Inject
	DBLogHandler(DBConnection db, ListenerManager manager) {
		this.db = db;
		setFilter(logRecord -> {
			boolean contains = ArrayUtils.contains(FILTERED_CLASSES, logRecord.getSourceClassName());
			return !contains;
		});
		manager.addListener(DBConnection.StateChangedEventListener.class, event -> {
			if (event.getDBConnection().isOpen()) {
				persist();
			}
		});
	}

	@Override
	public void publish(java.util.logging.LogRecord logRecord) {
		if (isLoggable(logRecord)) {
			LogRecord dbRecord = new LogRecord(this.hostName, this.hostAddress, logRecord);

			if (this.db.isOpen()) {
				persist(dbRecord);
			} else {
				this.logRecords.addLast(dbRecord);
			}
		}
	}

	@Override
	public void flush() {
		// nothing to flush here
	}

	@Override
	public void close() {
		// nothing to do yet
	}

	private void persist(LogRecord logRecord) {
		this.db.getExecutor().execute(() -> {
			EntityManager entityManager = this.db.getEntityManager();
			EntityTransaction transaction = entityManager.getTransaction();
			transaction.begin();
			try {
				this.db.getEntityManager().persist(logRecord);
				entityManager.flush();
				transaction.commit();
			} catch (PersistenceException e) {
				transaction.rollback();
				e.printStackTrace();
			}
		});
	}

	private void persist() {
		this.db.getExecutor().execute(() -> {
			EntityManager entityManager = this.db.getEntityManager();
			EntityTransaction transaction = entityManager.getTransaction();
			transaction.begin();
			try {
				while (!this.logRecords.isEmpty()) {
					entityManager.persist(this.logRecords.pop());
				}
				entityManager.flush();
				transaction.commit();
			} catch (PersistenceException e) {
				transaction.rollback();
				e.printStackTrace();
			}
		});
	}
}
